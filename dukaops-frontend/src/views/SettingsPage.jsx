import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Building2, UserRound, ShieldCheck, SlidersHorizontal, Bell, CreditCard } from 'lucide-react'
import { api, getStoredCredentials, saveCredentials } from '../services/api'
import { useFeedback } from '../components/ui/FeedbackProvider'
import '../styles/settings-page.css'

const sections = [ ['business', 'Business Profile', Building2], ['profile', 'My Profile', UserRound], ['security', 'Security', ShieldCheck], ['preferences', 'Business Preferences', SlidersHorizontal], ['notifications', 'Notifications', Bell], ['subscription', 'Subscription & Billing', CreditCard] ]

export default function SettingsPage() {
  const [section, setSection] = useState('business')
  const session = useQuery({ queryKey: ['session'], queryFn: api.currentUser })
  return <section className="settings-page"><header><h1>Settings</h1><p>Manage your business, account and system preferences.</p></header><div className="settings-layout"><nav aria-label="Settings sections">{sections.filter(([key]) => key !== 'subscription' || session.data?.role === 'OWNER').map(([key, label, Icon]) => <button key={key} className={section === key ? 'active' : ''} onClick={() => setSection(key)}><Icon size={16}/>{label}</button>)}</nav><SettingsPanel key={section} section={section}/></div></section>
}

function SettingsPanel({ section }) {
  const feedback = useFeedback()
  const client = useQueryClient()
  const [draft, setDraft] = useState(null)
  const [saving, setSaving] = useState(false)
  const query = useQuery({ queryKey: ['settings', section], queryFn: () => api.settings(section), enabled: section !== 'security' })
  const session = useQuery({ queryKey: ['session'], queryFn: api.currentUser })
  const business = useQuery({ queryKey: ['settings', 'business'], queryFn: () => api.settings('business'), enabled: section === 'preferences' })
  const branches = useQuery({ queryKey: ['settings-branches', business.data?.tenantId], queryFn: () => api.branches(business.data.tenantId), enabled: section === 'preferences' && Boolean(business.data?.tenantId) })
  const values = draft || query.data || {}
  const admin = ['OWNER', 'ADMIN'].includes(session.data?.role)
  const editable = section === 'profile' || section === 'security' || admin
  const title = sections.find(([key]) => key === section)[1]
  const change = (key, value) => setDraft({ ...values, [key]: value })
  const save = async (event) => {
    event.preventDefault()
    if (section === 'security' && values.newPassword !== values.confirmPassword) return feedback.error('The new passwords do not match.')
    let payload
    if (section === 'business') payload = { businessEmail: values.businessEmail || '', businessPhone: values.businessPhone || '', businessAddress: values.businessAddress || '', currency: values.currency, timezone: values.timezone, dateFormat: values.dateFormat, defaultBranchId: values.defaultBranchId || null }
    if (section === 'preferences') payload = { currency: values.currency, timezone: values.timezone, dateFormat: values.dateFormat, defaultBranchId: values.defaultBranchId || null }
    if (section === 'profile') payload = { fullName: values.fullName }
    if (section === 'notifications') payload = { lowStockAlerts: values.lowStockAlerts, inAppNotifications: values.inAppNotifications, emailNotifications: values.emailNotifications }
    if (section === 'security') payload = { currentPassword: values.currentPassword, newPassword: values.newPassword }
    setSaving(true)
    try {
      const result = await api.saveSettings(section === 'security' ? 'change-password' : section, payload)
      if (section === 'security') {
        const credentials = getStoredCredentials()
        if (credentials) saveCredentials(credentials.email, values.newPassword)
        setDraft({})
      } else { client.setQueryData(['settings', section], result); setDraft(null) }
      await client.invalidateQueries({ queryKey: ['settings'] })
      await client.invalidateQueries({ queryKey: ['session'] })
      feedback.success(section === 'security' ? 'Password updated successfully.' : 'Settings saved successfully.')
    } catch (error) { feedback.error(error.message) } finally { setSaving(false) }
  }
  if (section !== 'security' && query.isPending) return <div className="settings-panel" role="status">Loading settings…</div>
  if (query.isError) return <div className="settings-panel" role="alert"><p>{query.error.message}</p><button onClick={() => query.refetch()}>Try again</button></div>
  if (section === 'subscription') return <div className="settings-panel"><h2>{title}</h2><p>{values.message || 'Subscription information is unavailable.'}</p><div className="settings-notice">Plans, trial dates and billing details will be available when subscriptions are enabled.</div></div>
  return <form className="settings-panel" onSubmit={save}><div className="settings-panel-head"><div><h2>{title}</h2><p>{section === 'security' ? 'Keep your account secure by updating your password.' : 'Update your information and preferences.'}</p></div><button className="settings-save" disabled={!editable || saving}>{saving ? 'Saving…' : section === 'security' ? 'Update Password' : 'Save Changes'}</button></div>{!editable && <p className="settings-notice">Only owners and administrators can update business-wide settings.</p>}<fieldset disabled={!editable || saving}><div className="settings-fields">
    {section === 'business' && <><div className="settings-avatar"><Building2 size={32}/><strong>{values.businessName}</strong><small>Business identity</small></div><div className="settings-inputs"><Field label="Business name" value={values.businessName} disabled/><Field label="Business email" type="email" value={values.businessEmail} onChange={value => change('businessEmail', value)}/><Field label="Business phone" value={values.businessPhone} onChange={value => change('businessPhone', value)}/><Field label="Address" multiline value={values.businessAddress} onChange={value => change('businessAddress', value)}/><small>Business name and logo updates are currently unavailable.</small></div></>}
    {section === 'profile' && <><div className="settings-avatar"><UserRound size={32}/><strong>{values.fullName}</strong><small>{values.role}</small></div><div className="settings-inputs"><Field label="Full name" required maxLength={150} value={values.fullName} onChange={value => change('fullName', value)}/><Field label="Email address" value={values.email} disabled/><Field label="Account status" value={values.status} disabled/></div></>}
    {section === 'security' && <><div className="settings-inputs"><Field label="Current password" type="password" required value={values.currentPassword} onChange={value => change('currentPassword', value)}/><Field label="New password" type="password" required minLength={8} maxLength={72} value={values.newPassword} onChange={value => change('newPassword', value)}/><Field label="Confirm new password" type="password" required value={values.confirmPassword} onChange={value => change('confirmPassword', value)}/></div><div className="settings-notice"><h3>Password requirements</h3><p>Use at least 8 characters. Choose a password different from your current password.</p><p>Two-factor authentication is currently unavailable.</p></div></>}
    {section === 'preferences' && <><div className="settings-inputs"><Select label="Currency" value={values.currency} onChange={value => change('currency', value)} options={[['TZS','TZS — Tanzanian Shilling'],['USD','USD — US Dollar'],['KES','KES — Kenyan Shilling']]}/><Field label="Timezone" required value={values.timezone} onChange={value => change('timezone', value)}/><Select label="Date format" value={values.dateFormat} onChange={value => change('dateFormat', value)} options={['dd/MM/yyyy','MM/dd/yyyy','yyyy-MM-dd'].map(value => [value,value])}/></div><div className="settings-inputs"><Select label="Default branch" value={values.defaultBranchId || ''} onChange={value => change('defaultBranchId', value)} options={[['','No default branch'],...(branches.data || []).map(branch => [branch.id, branch.name])]}/>{branches.isError && <p role="alert">{branches.error.message}</p>}<div className="settings-notice">Receipt customization is currently unavailable.</div></div></>}
    {section === 'notifications' && <><div className="settings-inputs"><h3>Inventory alerts</h3><Toggle label="Low-stock alerts" detail="Uses the reorder level configured for each inventory item." checked={values.lowStockAlerts} onChange={value => change('lowStockAlerts', value)}/><div className="settings-notice">These preferences are stored for your business. Automated alert generation is not yet enabled.</div></div><div className="settings-inputs"><h3>Notification channels</h3><Toggle label="In-app notifications" detail="Your business notification preference." checked={values.inAppNotifications} onChange={value => change('inAppNotifications', value)}/><Toggle label="Email notifications" detail="Preference only. Email delivery is currently unavailable." checked={values.emailNotifications} onChange={value => change('emailNotifications', value)}/></div></>}
  </div></fieldset></form>
}
function Field({ label, value, onChange, multiline, ...props }) { return <label className="settings-field"><span>{label}</span>{multiline ? <textarea value={value || ''} onChange={event => onChange?.(event.target.value)} {...props}/> : <input value={value || ''} onChange={event => onChange?.(event.target.value)} {...props}/>}</label> }
function Select({ label, value, onChange, options }) { return <label className="settings-field"><span>{label}</span><select value={value || ''} onChange={event => onChange(event.target.value)}>{options.map(([key,text]) => <option key={key} value={key}>{text}</option>)}</select></label> }
function Toggle({ label, detail, checked, onChange }) { return <label className="settings-toggle"><div><strong>{label}</strong><small>{detail}</small></div><input type="checkbox" role="switch" checked={Boolean(checked)} onChange={event => onChange(event.target.checked)}/></label> }

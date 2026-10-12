import { useState } from 'react'
import { Building2, CheckCircle2, LoaderCircle, LockKeyhole, Mail, UserRound } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import { registerBusiness, saveCredentials } from '../services/api'
import PublicLayout from '../components/layout/PublicLayout'

export default function AuthPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ businessName: '', businessSlug: '', fullName: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const update = (field) => (event) => setForm((current) => ({ ...current, [field]: event.target.value }))
  async function submit(event) {
    event.preventDefault(); setError(''); setSubmitting(true)
    try { await registerBusiness(form); saveCredentials(form.email, form.password); navigate('/dashboard', { replace: true }); window.location.reload() } catch (requestError) { setError(requestError.message); setSubmitting(false) }
  }
  return <PublicLayout><div className="auth-page public-auth"><div className="auth-card"><div className="auth-intro"><p className="eyebrow">GET STARTED</p><h1>Create your workspace</h1><p>Set up your business account to connect the dashboard to live inventory data.</p></div><form onSubmit={submit} className="auth-form"><label><span>Business name</span><div className="auth-input"><Building2 size={16} /><input required value={form.businessName} onChange={update('businessName')} placeholder="e.g. Amos Traders" /></div></label><label><span>Business slug</span><div className="auth-input"><Building2 size={16} /><input required pattern="[a-z0-9]+(?:-[a-z0-9]+)*" value={form.businessSlug} onChange={update('businessSlug')} placeholder="amos-traders" /></div></label><label><span>Your full name</span><div className="auth-input"><UserRound size={16} /><input required value={form.fullName} onChange={update('fullName')} placeholder="Amos Developer" /></div></label><label><span>Email address</span><div className="auth-input"><Mail size={16} /><input required type="email" value={form.email} onChange={update('email')} placeholder="you@example.com" /></div></label><label><span>Password <small>(minimum 12 characters)</small></span><div className="auth-input"><LockKeyhole size={16} /><input required minLength={12} type="password" value={form.password} onChange={update('password')} placeholder="Create a secure password" /></div></label>{error && <div className="auth-error">{error}</div>}<button className="auth-submit" disabled={submitting}>{submitting ? <><LoaderCircle size={16} className="spin" /> Creating workspace...</> : 'Create workspace'}</button></form><p className="auth-switch">Already have an account? <Link to="/login">Log in</Link></p><div className="auth-note"><CheckCircle2 size={15} /> Credentials are stored only in this browser.</div></div></div></PublicLayout>
}

import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '../../services/api'
import { useFeedback } from '../ui/FeedbackProvider'
import '../../styles/customer-sms.css'

export default function CustomerSmsPanel() {
  const feedback = useFeedback()
  const client = useQueryClient()
  const [customerId, setCustomerId] = useState('')
  const [message, setMessage] = useState('')
  const [authorizedContact, setAuthorizedContact] = useState(false)
  const [requestKey, setRequestKey] = useState(() => crypto.randomUUID())
  const [busy, setBusy] = useState(false)
  const [page, setPage] = useState(0)
  const [result, setResult] = useState(null)
  const configuration = useQuery({ queryKey: ['sms-configuration'], queryFn: api.smsConfiguration })
  const customers = useQuery({ queryKey: ['sms-customers'], queryFn: () => api.contacts('customers', { status: 'ACTIVE' }) })
  const history = useQuery({ queryKey: ['sms-history', page], queryFn: () => api.smsHistory(page) })
  const change = (setter, value) => { setter(value); setRequestKey(crypto.randomUUID()); setResult(null) }
  const send = async event => {
    event.preventDefault()
    if (busy) return
    setBusy(true)
    try {
      const response = await api.sendCustomerSms({ customerId, message, requestKey, authorizedContact })
      setResult(response)
      if (['failed', 'undelivered', 'unknown', 'submitting'].includes(response.status)) feedback.error('Delivery is not confirmed. Check the message status before sending again.')
      else feedback.success('Message submitted to Twilio. Delivery is not yet guaranteed.')
      await client.invalidateQueries({ queryKey: ['sms-history'] })
    } catch (error) { feedback.error(error.message) } finally { setBusy(false) }
  }
  const refresh = async id => {
    setBusy(true)
    try { await api.refreshSms(id); await client.invalidateQueries({ queryKey: ['sms-history'] }) }
    catch (error) { feedback.error(error.message) } finally { setBusy(false) }
  }
  const rows = history.data || []
  return <div className="settings-panel customer-sms"><h2>Customer SMS</h2><p>Send customer-service messages to Tanzanian mobile numbers through Twilio.</p>
    {configuration.isPending ? <p role="status">Checking SMS configuration…</p> : configuration.isError ? <p role="alert">{configuration.error.message}</p> : <div className="settings-notice">{configuration.data.configured ? 'Twilio is configured. Sending SMS may incur charges.' : 'SMS is disabled. Your system administrator must configure Twilio on the backend.'} Trial accounts and Tanzania sender registration restrictions may apply.</div>}
    <form onSubmit={send} className="sms-form"><fieldset disabled={busy || !configuration.data?.configured}><label className="settings-field"><span>Customer</span><select required value={customerId} onChange={e => change(setCustomerId, e.target.value)}><option value="">Select a customer</option>{(customers.data?.content || []).map(customer => <option key={customer.id} value={customer.id}>{customer.name} — {customer.phone || 'No phone number'}</option>)}</select></label>
      {customers.isPending && <p role="status">Loading customers…</p>}{customers.isError && <p role="alert">{customers.error.message}</p>}
      <label className="settings-field"><span>Message</span><textarea required maxLength={480} value={message} onChange={e => change(setMessage, e.target.value)} placeholder="Write a customer-service message…"/></label><small>{message.length}/480 characters. Longer messages and Unicode may cost multiple SMS segments.</small>
      <label className="sms-consent"><input type="checkbox" required checked={authorizedContact} onChange={e => setAuthorizedContact(e.target.checked)}/>I am authorized to contact this customer. This is not an unsolicited marketing message.</label>
      <button className="settings-save" disabled={busy || Boolean(result) || !customerId || !message.trim() || !authorizedContact}>{busy ? 'Processing…' : 'Send SMS'}</button>
    </fieldset></form>
    {result && <div className="settings-notice" role="status">Message status: {result.status}. {result.status === 'unknown' ? 'The provider response was uncertain. Check Twilio Console before trying another message to avoid duplicate charges.' : 'Use Refresh status below to check delivery.'}</div>}
    <h3>Message history</h3>{history.isPending ? <p role="status">Loading messages…</p> : history.isError ? <div role="alert"><p>{history.error.message}</p><button onClick={() => history.refetch()}>Try again</button></div> : <><div className="sms-table"><table><thead><tr><th>Recipient</th><th>Message</th><th>Status</th><th>Created</th><th>Action</th></tr></thead><tbody>{rows.map(row => <tr key={row.id}><td>{row.recipient}</td><td className="sms-body">{row.body}</td><td>{row.status}</td><td>{new Date(row.createdAt).toLocaleString()}</td><td><button type="button" disabled={busy} onClick={() => refresh(row.id)}>Refresh status</button></td></tr>)}</tbody></table>{!rows.length && <p>No messages yet.</p>}</div><div className="sms-pagination"><button disabled={!page || busy} onClick={() => setPage(page - 1)}>Previous</button><span>Page {page + 1}</span><button disabled={rows.length < 10 || busy} onClick={() => setPage(page + 1)}>Next</button></div></>}
  </div>
}

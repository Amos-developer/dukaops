import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, KeyRound } from 'lucide-react'
import { api, clearCredentials } from '../services/api'
import '../styles/password-recovery.css'

export default function PasswordRecoveryPage() {
  const location = useLocation()
  const reset = location.pathname === '/reset-password'
  const [token] = useState(() => new URLSearchParams(window.location.hash.slice(1)).get('token') || '')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const client = useQueryClient()
  useEffect(() => { if (reset && window.location.hash) window.history.replaceState(null, '', window.location.pathname) }, [reset])
  async function submit(event) {
    event.preventDefault(); setError('')
    if (reset && password !== confirmation) { setError('The passwords do not match.'); return }
    setBusy(true)
    try {
      if (reset) {
        await api.resetPassword(token, password)
        clearCredentials(); client.clear(); setPassword(''); setConfirmation('')
        setMessage('Your password has been updated. Sign in with your new password.')
      } else { const result = await api.requestPasswordReset(email); setMessage(result.message) }
    } catch (failure) { setError(reset ? 'We could not reset your password. The link may have expired or already been used. Request a new link, or check your connection and try again.' : failure.message) }
    finally { setBusy(false) }
  }
  return <main className="recovery-page"><section className="recovery-card"><Link to="/" className="login-logo"><span>D</span><strong>Duka<span>Ops</span></strong></Link><span className="recovery-icon"><KeyRound size={24}/></span><h1>{reset ? 'Choose a new password' : 'Forgot your password?'}</h1><p>{reset ? 'Use a strong password you haven’t used for this account.' : 'Enter your account email and we’ll send you a secure reset link.'}</p>{message ? <div className="recovery-success" role="status">{message}</div> : reset && !/^[a-f0-9]{64}$/.test(token) ? <div className="auth-error" role="alert">This reset link is incomplete. Request a new link.</div> : <form className="login-form" onSubmit={submit}>{reset ? <><label htmlFor="reset-password"><span>New password</span><div className="login-input"><input id="reset-password" type="password" autoComplete="new-password" minLength={12} maxLength={64} required value={password} onChange={event => setPassword(event.target.value)}/></div><small>Use 12–64 characters.</small></label><label htmlFor="confirm-reset"><span>Confirm password</span><div className="login-input"><input id="confirm-reset" type="password" autoComplete="new-password" minLength={12} maxLength={64} required value={confirmation} onChange={event => setConfirmation(event.target.value)}/></div></label></> : <label htmlFor="recovery-email"><span>Email address</span><div className="login-input"><input id="recovery-email" type="email" autoComplete="email" maxLength={254} required placeholder="juma@example.com" value={email} onChange={event => setEmail(event.target.value)}/></div></label>}{error && <div className="auth-error" role="alert">{error}</div>}<button className="login-submit" disabled={busy}>{busy ? 'Please wait…' : reset ? 'Reset password' : 'Send reset link'}</button></form>}{reset && !message && <Link className="recovery-link" to="/forgot-password">Request a new reset link</Link>}<Link className="recovery-link" to="/login"><ArrowLeft size={14}/> Back to login</Link></section></main>
}

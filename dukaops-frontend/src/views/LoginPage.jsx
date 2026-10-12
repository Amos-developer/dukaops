import { useState } from 'react'
import { ArrowRight, LockKeyhole, Mail } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import { api, saveCredentials } from '../services/api'
import PublicLayout from '../components/layout/PublicLayout'

export default function LoginPage() {
  const navigate = useNavigate(); const [email, setEmail] = useState(''); const [password, setPassword] = useState(''); const [error, setError] = useState(''); const [loading, setLoading] = useState(false)
  async function submit(event) { event.preventDefault(); setError(''); setLoading(true); saveCredentials(email, password); try { await api.currentUser(); navigate('/dashboard') } catch (requestError) { localStorage.removeItem('dukaops.credentials'); setError(requestError.message); setLoading(false) } }
  return <PublicLayout><div className="auth-page public-auth"><div className="auth-card"><div className="auth-intro"><p className="eyebrow">WELCOME BACK</p><h1>Log in to DukaOps</h1><p>Access your inventory workspace and see what needs attention.</p></div><form onSubmit={submit} className="auth-form"><label><span>Email address</span><div className="auth-input"><Mail size={16} /><input required type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="you@example.com" /></div></label><label><span>Password</span><div className="auth-input"><LockKeyhole size={16} /><input required type="password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Your password" /></div></label>{error && <div className="auth-error">{error}</div>}<button className="auth-submit" disabled={loading}>{loading ? 'Signing in...' : <>Continue to dashboard <ArrowRight size={15} /></>}</button></form><p className="auth-switch">New to DukaOps? <Link to="/register">Create a workspace</Link></p></div></div></PublicLayout>
}

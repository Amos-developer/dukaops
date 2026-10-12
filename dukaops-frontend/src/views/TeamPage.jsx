import { useState } from 'react'
import { useTeam } from '../hooks/useModuleData'
import { Building2, ChevronRight, Mail, MoreHorizontal, ShieldCheck, UserPlus, Users } from 'lucide-react'
import StatusBadge from '../components/ui/StatusBadge'
import CrudModal from '../components/ui/CrudModal'
import { useFeedback } from '../components/ui/FeedbackProvider'

export default function TeamPage() {
  const { data, isLoading, isError } = useTeam()
  const feedback = useFeedback()
  const [inviteOpen, setInviteOpen] = useState(false)
  const user = data?.user
  const branches = data?.branches || []
  const unavailable = () => feedback.error('Team management will be available when staff API endpoints are enabled.')
  return <section className="team-page">
    <div className="team-heading"><div><p className="eyebrow">WORKSPACE ACCESS</p><h1><Users size={20} /> Team &amp; Users</h1><p>Manage staff accounts, roles, and branch access from one workspace.</p></div><button className="team-primary" onClick={() => setInviteOpen(true)}><UserPlus size={15} /> Invite member</button></div>
    <div className="team-metrics"><div><Users size={18} /><span><small>Team members</small><strong>{user ? 1 : 0}</strong><em>Current account</em></span></div><div><ShieldCheck size={18} /><span><small>Workspace role</small><strong>{user?.role || '—'}</strong><em>Authenticated access</em></span></div><div><Building2 size={18} /><span><small>Branch coverage</small><strong>{branches.length}</strong><em>Available locations</em></span></div></div>
    <div className="team-notice"><ShieldCheck size={17} /><span><strong>Access controls</strong><small>Role and branch permissions are scoped to your authenticated workspace.</small></span><button onClick={unavailable}>Manage access <ChevronRight size={14} /></button></div>
    <div className="team-toolbar"><div><h2>Team members</h2><p>People who can access this business workspace.</p></div><button className="team-filter" onClick={unavailable}>All roles <ChevronRight size={14} /></button></div>
    <div className="team-table-card">{isLoading && <div className="table-loading">Loading team access...</div>}{isError && !isLoading && <div className="table-loading">We couldn’t load team access right now. Please refresh and try again.</div>}{!isLoading && !isError && <div className="table-responsive"><table className="table team-table"><thead><tr><th>Member</th><th>Role</th><th>Branch access</th><th>Status</th><th>Last access</th><th /></tr></thead><tbody><tr><td><div className="team-member"><span>{user?.email?.slice(0, 2).toUpperCase() || 'ME'}</span><strong><b>{user?.email || 'Current user'}</b><small>{user?.email || '—'}</small></strong></div></td><td><span className="team-role"><ShieldCheck size={13} /> {user?.role || 'OWNER'}</span></td><td><div className="branch-access">{branches.slice(0, 2).map((branch) => <span key={branch.id}><Building2 size={12} /> {branch.name}</span>)}{branches.length > 2 && <small>+{branches.length - 2} more</small>}</div></td><td><StatusBadge>Active</StatusBadge></td><td>Current session</td><td><button className="icon-btn" onClick={unavailable} aria-label="Member actions"><MoreHorizontal size={17} /></button></td></tr></tbody></table></div>}</div>
    <div className="team-branch-summary"><div><h2>Branch access overview</h2><p>Branches currently available to this workspace.</p></div><div className="team-branch-grid">{branches.map((branch) => <div key={branch.id}><span className="team-branch-icon"><Building2 size={16} /></span><span><strong>{branch.name}</strong><small>{branch.code || 'Branch'} · {branch.isMain ? 'Main branch' : 'Operational branch'}</small></span><ChevronRight size={15} /></div>)}</div></div>
    {inviteOpen && <CrudModal title="Invite team member" description="Staff invitations require the team management API." onClose={() => setInviteOpen(false)} onSubmit={(event) => { event.preventDefault(); setInviteOpen(false); unavailable() }} submitLabel="Request API access"><div className="crud-field"><label htmlFor="team-email">Member email</label><input id="team-email" type="email" placeholder="staff@example.com" disabled /></div><p className="team-modal-help">The current backend does not yet expose invitation, role assignment, or branch-access endpoints.</p></CrudModal>}
  </section>
}

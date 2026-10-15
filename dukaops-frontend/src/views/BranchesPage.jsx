import { useNavigate } from 'react-router-dom'
import PaginatedTable from '../components/ui/PaginatedTable'
import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { MapPin, MoreHorizontal, Plus, Store } from 'lucide-react'
import { useBranches } from '../hooks/useModuleData'
import StatusBadge from '../components/ui/StatusBadge'
import CrudModal from '../components/ui/CrudModal'
import PageToolbar from '../components/ui/PageToolbar'
import { api } from '../services/api'
import { useFeedback } from '../components/ui/FeedbackProvider'

export default function BranchesPage() {
  const { data, isLoading } = useBranches()
  const queryClient = useQueryClient()
  const feedback = useFeedback()
  const session = useQuery({ queryKey: ['session'], queryFn: api.currentUser })
  const owner = session.data?.role === 'OWNER'
  const [editing, setEditing] = useState(null)
  const navigate = useNavigate()
  const [statusBranch, setStatusBranch] = useState(null)
  const openEdit = branch => { setEditing(branch); setForm({ name: branch.name, code: branch.code, isMain: branch.isMain }); setError(''); setModal(true) }
  const changeStatus = async event => { event.preventDefault(); setSaving(true); try { await api.setBranchStatus(session.data.tenantId, statusBranch.id, statusBranch.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE'); await queryClient.invalidateQueries(); setStatusBranch(null); feedback.success('Branch status updated.') } catch (failure) { setError(failure.message); feedback.error(failure.message) } finally { setSaving(false) } }
  const [search, setSearch] = useState('')
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState({ name: '', code: '', isMain: false })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const branches = (data?.branches || []).filter((branch) => branch.name.toLowerCase().includes(search.toLowerCase()))
  const allBranches = data?.branches || []
  const activeBranches = allBranches.filter((branch) => branch.status === 'ACTIVE').length
  const openCreate = () => { setEditing(null); setForm({ name: '', code: '', isMain: false }); setError(''); setModal(true) }
  const submit = async (event) => { event.preventDefault(); setSaving(true); setError(''); try { const user = await api.currentUser(); if (editing) await api.updateBranch(user.tenantId, editing.id, { name: form.name, code: form.code }); else await api.createBranch(user.tenantId, form); await queryClient.invalidateQueries({ queryKey: ['branches'] }); setModal(false); feedback.success(editing ? 'Branch updated successfully.' : 'Branch created successfully.') } catch (requestError) { setError(requestError.message); feedback.error(requestError.message) } finally { setSaving(false) } }

  return <section className="branch-page">
    <div className="branch-heading"><div><p className="eyebrow">LOCATIONS</p><h1><Store size={20} /> Branches</h1><p>Manage your stores and keep branch operations visible in one place.</p></div><button className="branch-primary" disabled={!owner} onClick={openCreate}><Plus size={15} /> Add branch</button></div>
    <div className="branch-metrics"><div><Store size={18} /><span><small>Total branches</small><strong>{allBranches.length}</strong><em>Live locations</em></span></div><div><MapPin size={18} /><span><small>Main branch</small><strong>{allBranches.filter((branch) => branch.isMain).length}</strong><em>Primary location</em></span></div><div><span className="branch-metric-symbol">✓</span><span><small>Active branches</small><strong>{activeBranches}</strong><em>Ready for operations</em></span></div></div>
    <PageToolbar search={search} onSearch={setSearch} onFilter={() => {}} filter="All branches" />
    <div className="branch-table-card">{isLoading ? <div className="table-loading">Loading branches...</div> : <div className="table-responsive"><PaginatedTable className="table branch-table"><thead><tr><th>Branch</th><th>Code</th><th>Type</th><th>Status</th><th>Created</th><th /></tr></thead><tbody>{branches.map((branch) => <tr key={branch.id}><td><div className="entity-cell"><div className="entity-icon"><Store size={15} /></div><span><strong>{branch.name}</strong><small>{branch.isMain ? 'Primary location' : 'Operational branch'}</small></span></div></td><td><strong>{branch.code || 'MAIN'}</strong></td><td><span className="location-type"><MapPin size={12} />{branch.isMain ? 'Main branch' : 'Branch'}</span></td><td><StatusBadge>{branch.status || 'ACTIVE'}</StatusBadge></td><td>{branch.createdAt ? new Date(branch.createdAt).toLocaleDateString() : '—'}</td><td className="text-end"><details className="branch-row-menu"><summary aria-label={`Actions for ${branch.name}`}><MoreHorizontal size={17}/></summary><div><button onClick={event => { event.currentTarget.closest('details').open = false; navigate(`/branches/${branch.id}`) }}>View branch</button>{owner && <><button onClick={event => { event.currentTarget.closest('details').open = false; openEdit(branch) }}>Edit branch</button><button onClick={event => { event.currentTarget.closest('details').open = false; setError(''); setStatusBranch(branch) }}>{branch.status === 'ACTIVE' ? 'Deactivate branch' : 'Reactivate branch'}</button></>}</div></details></td></tr>)}</tbody></PaginatedTable></div>}</div>
    {modal && <CrudModal title={editing ? "Edit branch" : "Add branch"} description="Create a branch to organize stock and movements." onClose={() => setModal(false)} onSubmit={submit} submitLabel={editing ? "Save changes" : "Create branch"} busy={saving} error={error}><div className="crud-form-grid"><div className="crud-field"><label htmlFor="branch-name">Branch name</label><input id="branch-name" required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></div><div className="crud-field"><label htmlFor="branch-code">Branch code</label><input id="branch-code" required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value.toUpperCase() })} /></div>{!editing && <label className="crud-check"><input type="checkbox" checked={form.isMain} onChange={(event) => setForm({ ...form, isMain: event.target.checked })} /> Set as main branch</label>}</div></CrudModal>}
    {statusBranch && <CrudModal title={statusBranch.status === 'ACTIVE' ? 'Deactivate branch' : 'Reactivate branch'} onClose={() => setStatusBranch(null)} onSubmit={changeStatus} submitLabel="Confirm" busy={saving} error={error}><p>Change the status of <strong>{statusBranch.name}</strong>?</p><p>Branch records and transaction history will be preserved.</p></CrudModal>}
  </section>
}

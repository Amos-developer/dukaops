import { useState } from 'react'
import { MapPin, MoreHorizontal, Store } from 'lucide-react'
import { useBranches } from '../hooks/useModuleData'
import PageToolbar from '../components/ui/PageToolbar'
import StatusBadge from '../components/ui/StatusBadge'

export default function BranchesPage() {
  const { data, isLoading } = useBranches(); const [search, setSearch] = useState('')
  const branches = (data?.branches || []).filter((branch) => branch.name.toLowerCase().includes(search.toLowerCase()))
  return <section className="module-page"><div className="module-heading"><div><p className="eyebrow">LOCATIONS</p><h1>Branches</h1><p>Manage your stores and keep branch operations visible in one place.</p></div></div><PageToolbar search={search} onSearch={setSearch} onFilter={() => {}} filter="All branches" actionLabel="Add branch" /><div className="data-card">{isLoading ? <div className="table-loading">Loading branches...</div> : <div className="table-responsive"><table className="table module-table"><thead><tr><th>Branch</th><th>Code</th><th>Type</th><th>Status</th><th /></tr></thead><tbody>{branches.map((branch) => <tr key={branch.id}><td><div className="entity-cell"><div className="entity-icon"><Store size={16} /></div><strong>{branch.name}</strong></div></td><td>{branch.code || 'MAIN'}</td><td><span className="location-type"><MapPin size={13} />{branch.isMain ? 'Main branch' : 'Branch'}</span></td><td><StatusBadge>{branch.status || 'ACTIVE'}</StatusBadge></td><td className="text-end"><button className="icon-btn"><MoreHorizontal size={17} /></button></td></tr>)}</tbody></table></div>}</div></section>
}

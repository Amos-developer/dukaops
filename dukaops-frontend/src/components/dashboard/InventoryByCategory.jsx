import { useId, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { api } from '../../services/api'
import PaginatedTable from '../ui/PaginatedTable'
import '../../styles/categories.css'
import { categorySegments } from '../../services/categoryChart'

const number = value => Number(value || 0).toLocaleString(undefined, { maximumFractionDigits: 3 })
export default function InventoryByCategory({ branchId: fixedBranch }) {
  const titleId = useId()
  const session = useQuery({ queryKey: ['session'], queryFn: api.currentUser })
  const branches = useQuery({ queryKey: ['category-branches', session.data?.tenantId], queryFn: () => api.branches(session.data.tenantId), enabled: fixedBranch === undefined && Boolean(session.data?.tenantId) })
  const [branch, setBranch] = useState('')
  const [view, setView] = useState('quantity')
  const selectedBranch = fixedBranch ?? branch
  const query = useQuery({ queryKey: ['category-inventory', selectedBranch, view], queryFn: () => api.categoryInventory({ branchId: selectedBranch, view }), enabled: Boolean(session.data) })
  const data = query.data
  const rows = data?.categories || []
  const total = Number(view === 'quantity' ? data?.totalQuantity : data?.totalValue) || 0
  const segments = categorySegments(data, view)
  return <section className="dashboard-card analytics-card inventory-category-card" aria-labelledby={titleId}><div className="analytics-title"><div><p className="eyebrow">PRODUCT MIX</p><h2 id={titleId}>Inventory by category</h2></div></div>
    <div className="category-chart-controls">{fixedBranch === undefined && <label><span>Branch</span><select value={branch} onChange={event => setBranch(event.target.value)}><option value="">{['OWNER', 'ADMIN'].includes(session.data?.role) ? 'All branches' : 'Assigned branches'}</option>{(branches.data || []).map(item => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>}<label><span>View</span><select value={view} onChange={event => setView(event.target.value)}><option value="quantity">Quantity</option>{session.data?.role !== 'STAFF' && <option value="value">Value at cost (TZS)</option>}</select></label></div>
    {branches.isError && fixedBranch === undefined && <p role="alert">Could not load branches. <button onClick={() => branches.refetch()}>Retry</button></p>}
    {query.isPending ? <p className="category-state" role="status">Loading category inventory…</p> : query.isError ? <div className="category-state" role="alert"><p>{query.error.message}</p><button className="secondary-btn" onClick={() => query.refetch()}>Try again</button></div> : <>
      <div className="category-donut-wrap"><svg className="category-donut" viewBox="0 0 120 120" role="img" aria-label={`Inventory by category: ${number(total)} ${view === 'quantity' ? 'units' : 'TZS at current cost'}`}><circle cx="60" cy="60" r="44" fill="none" stroke="#e9f0f5" strokeWidth="14"/>{segments.filter(row => row.percent > 0).map(row => <circle key={row.categoryId || 'uncategorized'} cx="60" cy="60" r="44" pathLength="100" fill="none" stroke={row.color} strokeWidth="14" strokeDasharray={`${row.percent} ${100 - row.percent}`} strokeDashoffset={-row.offset} transform="rotate(-90 60 60)"><title>{row.name}: {number(row.amount)} ({row.percent.toFixed(1)}%)</title></circle>)}</svg><div className="category-donut-total"><strong>{number(total)}</strong><span>{view === 'quantity' ? 'Units in stock' : 'TZS · known cost'}</span></div></div>
      {total === 0 && <p className="category-state">{view === 'quantity' ? 'No stock available for this branch selection.' : 'No inventory value available. Check stock quantities and product costs.'}</p>}
      {view === 'value' && Number(data?.unvaluedQuantity) > 0 && <p className="category-value-warning" role="status">Partial valuation: {number(data.unvaluedQuantity)} units have no cost price and are excluded from value totals. This is current cost, not historical profit.</p>}
      {rows.length > 0 && <div className="table-responsive"><PaginatedTable className="category-legend"><thead><tr><th>Category</th><th>{view === 'quantity' ? 'Units' : 'TZS at cost'}</th><th>Share</th></tr></thead><tbody>{segments.map(row => <tr key={row.categoryId || 'uncategorized'}><td><i style={{ background: row.color }}/>{row.name}</td><td>{number(row.amount)}</td><td>{row.percent.toFixed(1)}%</td></tr>)}</tbody></PaginatedTable></div>}
      {['OWNER', 'ADMIN'].includes(session.data?.role) && <Link className="category-manage-link" to="/products?manageCategories=1">Manage categories in Products →</Link>}
    </>}
  </section>
}

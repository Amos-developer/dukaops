import PaginatedTable from '../components/ui/PaginatedTable'
import { useMemo, useState } from 'react'
import { ArrowDownToLine, ArrowUpFromLine, Boxes, ChevronDown, Download, Filter, MoreHorizontal, Search, SlidersHorizontal, TrendingUp } from 'lucide-react'
import { useStockMovements } from '../hooks/useModuleData'
import StatusBadge from '../components/ui/StatusBadge'

const filters = [{ key: 'ALL', label: 'All Movements' }, { key: 'RECEIPT', label: 'Stock Received' }, { key: 'ADJUSTMENT', label: 'Adjustments' }]
const number = (value) => Number(value || 0).toLocaleString()
const date = (value) => value ? new Date(value).toLocaleDateString(undefined, { month: 'short', day: '2-digit', year: 'numeric' }) : '—'
const time = (value) => value ? new Date(value).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' }) : ''

function MovementIcon({ type }) { const receipt = type === 'RECEIPT'; return <span className={`movement-page-icon ${receipt ? 'receive' : 'adjust'}`}>{receipt ? <ArrowDownToLine size={16} /> : <TrendingUp size={16} />}</span> }

export default function StockMovementsPage() {
  const { data, isLoading, isError } = useStockMovements()
  const [search, setSearch] = useState('')
  const [filter, setFilter] = useState('ALL')
  const [branch, setBranch] = useState('ALL')
  const movements = data?.movements || []
  const branches = data?.branches || []
  const filtered = useMemo(() => movements.filter((movement) => {
    const query = search.toLowerCase().trim()
    return (!query || `${movement.productName} ${movement.sku} ${movement.reference || ''}`.toLowerCase().includes(query)) && (filter === 'ALL' || movement.movementType === filter) && (branch === 'ALL' || movement.branchId === branch)
  }), [movements, search, filter, branch])
  const received = movements.filter((movement) => movement.movementType === 'RECEIPT').length
  const adjustments = movements.filter((movement) => movement.movementType === 'ADJUSTMENT').length
  const receivedUnits = movements.filter((movement) => movement.movementType === 'RECEIPT').reduce((sum, movement) => sum + Number(movement.quantityChange || 0), 0)

  return <section className="movement-page">
    <div className="movement-heading"><div><p className="eyebrow">STOCK CONTROL</p><h1><TrendingUp size={24} /> Stock Movements</h1><p>Review receipts and adjustments across every branch.</p></div><button className="movement-export"><Download size={15} /> Export Report</button></div>
    <div className="movement-metrics"><div><span className="movement-metric-icon teal"><Boxes size={18} /></span><span><small>Total Movements</small><strong>{number(movements.length)}</strong><em>Live records</em></span></div><div><span className="movement-metric-icon blue"><ArrowDownToLine size={18} /></span><span><small>Stock Received</small><strong>{number(received)}</strong><em>{number(receivedUnits)} units received</em></span></div><div><span className="movement-metric-icon orange"><ArrowUpFromLine size={18} /></span><span><small>Adjustments</small><strong>{number(adjustments)}</strong><em>Recorded corrections</em></span></div><div><span className="movement-metric-icon violet"><TrendingUp size={18} /></span><span><small>Latest Activity</small><strong>{movements.length ? date(movements[0].createdAt) : '—'}</strong><em>{movements.length ? time(movements[0].createdAt) : 'No activity yet'}</em></span></div></div>
    <div className="movement-tabs">{filters.map((item) => <button key={item.key} className={filter === item.key ? 'active' : ''} onClick={() => setFilter(item.key)}>{item.label}{item.key !== 'ALL' && <b>{item.key === 'RECEIPT' ? received : adjustments}</b>}</button>)}</div>
    <div className="movement-toolbar"><label className="movement-search"><Search size={16} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by product, SKU, or reference..." /></label><select value={branch} onChange={(event) => setBranch(event.target.value)}><option value="ALL">All Branches</option>{branches.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select><button><SlidersHorizontal size={14} /> Filters</button></div>
    <div className="movement-table-card">{isLoading && <div className="table-loading">Loading stock movements...</div>}{isError && !isLoading && <div className="table-loading">We couldn’t load stock movements right now. Please refresh and try again.</div>}{!isLoading && !isError && !filtered.length && <div className="table-loading">No stock movements match your filters.</div>}{!isLoading && !isError && filtered.length > 0 && <div className="table-responsive"><PaginatedTable className="table movement-table"><thead><tr><th>Movement</th><th>Product</th><th>SKU</th><th>Branch</th><th>Quantity Change</th><th>Stock After</th><th>Reference</th><th>Date &amp; Time</th><th /></tr></thead><tbody>{filtered.map((movement) => <tr key={movement.id}><td><div className="movement-type"><MovementIcon type={movement.movementType} /><span><strong>{movement.movementType === 'RECEIPT' ? 'Stock received' : 'Stock adjusted'}</strong><small>{movement.notes || 'Inventory movement'}</small></span></div></td><td><strong className="movement-product-name">{movement.productName}</strong></td><td>{movement.sku}</td><td><span className="branch-chip">⌖ {movement.branchName}</span></td><td><strong className={Number(movement.quantityChange) >= 0 ? 'quantity-in' : 'quantity-out'}>{Number(movement.quantityChange) > 0 ? '+' : ''}{number(movement.quantityChange)}</strong></td><td>{number(movement.quantityAfter)}</td><td><span className="movement-reference">{movement.reference || '—'}</span></td><td><span className="movement-date">{date(movement.createdAt)}<small>{time(movement.createdAt)}</small></span></td><td><button className="icon-btn" aria-label="Movement actions"><MoreHorizontal size={17} /></button></td></tr>)}</tbody></PaginatedTable></div>}</div>
    {!isLoading && !isError && <div className="movement-count">Showing 1–{filtered.length} of {movements.length} movements</div>}
  </section>
}

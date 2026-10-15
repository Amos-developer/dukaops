import PaginatedTable from '../components/ui/PaginatedTable'
import { useMemo, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { AlertTriangle, ArrowDownToLine, ArrowUpFromLine, Boxes, ChevronDown, Download, Filter, MoreHorizontal, PackagePlus, Search, SlidersHorizontal, TrendingUp } from 'lucide-react'
import { useInventory } from '../hooks/useModuleData'
import StatusBadge from '../components/ui/StatusBadge'
import CrudModal from '../components/ui/CrudModal'
import { api } from '../services/api'
import { useFeedback } from '../components/ui/FeedbackProvider'

const tabs = [{ key: 'ALL', label: 'All Products' }, { key: 'LOW', label: 'Low Stock' }, { key: 'OUT', label: 'Out of Stock' }]
const number = (value) => Number(value || 0).toLocaleString()
const date = (value) => value ? new Date(value).toLocaleDateString(undefined, { month: 'short', day: '2-digit', year: 'numeric' }) : '—'
const time = (value) => value ? new Date(value).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' }) : ''

function ProductMark() { return <span className="inventory-product-mark"><PackagePlus size={17} /></span> }
function Metric({ icon, label, value, trend, tone = 'teal' }) { return <div className={`inventory-metric ${tone}`}><span className="inventory-metric-icon">{icon}</span><div><span>{label}</span><strong>{value}</strong><small>{trend}</small></div></div> }
function MovementIcon({ type }) { const receiving = type === 'RECEIPT'; return <span className={`movement-icon ${receiving ? 'receive' : 'adjust'}`}>{receiving ? <ArrowDownToLine size={15} /> : <TrendingUp size={15} />}</span> }

export default function InventoryPage() {
  const { data, isLoading, isError } = useInventory()
  const queryClient = useQueryClient()
  const feedback = useFeedback()
  const session = useQuery({ queryKey: ['session'], queryFn: api.currentUser })
  const owner = session.data?.role === 'OWNER'
  const [selected, setSelected] = useState(null)
  const [detailItem, setDetailItem] = useState(null)
  const [reorderItem, setReorderItem] = useState(null)
  const [reorderValue, setReorderValue] = useState('')
  const openRowAction = (type, item) => { openStockModal(type); setSelected(item); setForm(current => ({ ...current, branchId: item.branchId, productId: item.productId, reorderLevel: String(item.reorderLevel) })) }
  const saveReorder = async event => { event.preventDefault(); setSaving(true); try { await api.updateReorderLevel(reorderItem.branchId, reorderItem.productId, Number(reorderValue)); await queryClient.invalidateQueries({ queryKey: ['inventory'] }); await queryClient.invalidateQueries({ queryKey: ['dashboard'] }); setReorderItem(null); feedback.success('Reorder level updated.') } catch (failure) { setFormError(failure.message); feedback.error(failure.message) } finally { setSaving(false) } }
  const [search, setSearch] = useState('')
  const [tab, setTab] = useState('ALL')
  const [branch, setBranch] = useState('ALL')
  const [status, setStatus] = useState('ALL')
  const [modal, setModal] = useState(null)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const [form, setForm] = useState({ branchId: '', productId: '', quantity: '', reorderLevel: '', quantityChange: '', reference: '', notes: '' })
  const stock = data?.stock || []
  const branches = data?.branches || []
  const movements = data?.movements || []

  const filteredStock = useMemo(() => stock.filter((item) => {
    const query = search.trim().toLowerCase()
    const matchesSearch = !query || `${item.productName || ''} ${item.sku || ''}`.toLowerCase().includes(query)
    const matchesTab = tab === 'ALL' || (tab === 'LOW' && item.lowStock && Number(item.quantity) > 0) || (tab === 'OUT' && Number(item.quantity) === 0)
    const matchesBranch = branch === 'ALL' || item.branchId === branch
    const matchesStatus = status === 'ALL' || (status === 'HEALTHY' && !item.lowStock && Number(item.quantity) > 0) || (status === 'LOW' && item.lowStock) || (status === 'OUT' && Number(item.quantity) === 0)
    return matchesSearch && matchesTab && matchesBranch && matchesStatus
  }), [stock, search, tab, branch, status])

  const lowStock = stock.filter((item) => item.lowStock && Number(item.quantity) > 0).length
  const outOfStock = stock.filter((item) => Number(item.quantity) === 0).length
  const totalQuantity = stock.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
  const healthy = stock.filter((item) => !item.lowStock && Number(item.quantity) > 0).length
  const openStockModal = (type) => { setSelected(null); setForm({ branchId: data?.selectedBranch?.id || branches[0]?.id || '', productId: data?.products?.[0]?.id || '', quantity: '', reorderLevel: '', quantityChange: '', reference: '', notes: '' }); setFormError(''); setModal(type) }
  const submitStock = async (event) => { event.preventDefault(); setSaving(true); setFormError(''); try { if (modal === 'receive') { await api.receiveStock({ branchId: form.branchId, productId: form.productId, quantity: Number(form.quantity), reorderLevel: form.reorderLevel === '' ? null : Number(form.reorderLevel), reference: form.reference, notes: form.notes }); feedback.success('Stock received successfully.') } else { await api.adjustStock({ branchId: form.branchId, productId: form.productId, quantityChange: Number(form.quantityChange), reference: form.reference, notes: form.notes }); feedback.success('Stock adjustment saved successfully.') } await queryClient.invalidateQueries({ queryKey: ['inventory'] }); await queryClient.invalidateQueries({ queryKey: ['stock-movements'] }); await queryClient.invalidateQueries({ queryKey: ['dashboard'] }); setModal(null) } catch (error) { setFormError(error.message); feedback.error(error.message) } finally { setSaving(false) } }
  const cycleBranch = () => setBranch((current) => {
    const options = ['ALL', ...branches.map((item) => item.id)]
    return options[(options.indexOf(current) + 1) % options.length]
  })
  const cycleStatus = () => setStatus((current) => {
    const options = ['ALL', 'HEALTHY', 'LOW', 'OUT']
    return options[(options.indexOf(current) + 1) % options.length]
  })

  return <section className="inventory-page">
    <div className="inventory-heading"><div><p className="eyebrow">STOCK CONTROL</p><h1><Boxes size={25} /> Inventory</h1><p>Track stock levels, manage stock movements, and keep your business running smoothly.</p></div><button className="inventory-export"><Download size={15} /> Export Report</button></div>
    <div className="inventory-metrics"><Metric icon={<Boxes size={19} />} label="Total Stock Value" value="TSH —" trend="Valuation coming soon" tone="blue" /><Metric icon={<PackagePlus size={19} />} label="Total Quantity" value={number(totalQuantity)} trend={`${healthy} healthy items`} /><Metric icon={<AlertTriangle size={19} />} label="Low Stock Items" value={number(lowStock)} trend={`${outOfStock} out of stock`} tone="coral" /><Metric icon={<TrendingUp size={19} />} label="Stock Movements" value={number(movements.length)} trend="Recorded movements" tone="violet" /></div>
    <div className="inventory-layout"><div className="inventory-main">
      <div className="inventory-tabs">{tabs.map((item) => <button key={item.key} className={tab === item.key ? 'active' : ''} onClick={() => setTab(item.key)}>{item.label}{item.key === 'LOW' && lowStock > 0 && <b>{lowStock}</b>}{item.key === 'OUT' && outOfStock > 0 && <b>{outOfStock}</b>}</button>)}</div>
      <div className="inventory-toolbar"><label className="inventory-search"><Search size={16} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by product name or SKU..." /></label><button className="inventory-select" onClick={cycleBranch}><span><SlidersHorizontal size={14} /> {branch === 'ALL' ? 'All Branches' : branches.find((item) => item.id === branch)?.name}</span><ChevronDown size={14} /></button><button className="inventory-select" onClick={cycleStatus}><span><Filter size={14} /> {status === 'ALL' ? 'All Status' : status === 'HEALTHY' ? 'Healthy' : status === 'LOW' ? 'Low Stock' : 'Out of Stock'}</span><ChevronDown size={14} /></button><button className="inventory-filter" onClick={cycleStatus}><Filter size={14} /> Filters</button></div>
      <div className="inventory-filter-mobile"><select value={branch} onChange={(event) => setBranch(event.target.value)}><option value="ALL">All Branches</option>{branches.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select><select value={status} onChange={(event) => setStatus(event.target.value)}><option value="ALL">All Status</option><option value="HEALTHY">Healthy</option><option value="LOW">Low Stock</option><option value="OUT">Out of Stock</option></select></div>
      <div className="inventory-table-card">{isLoading && <div className="table-loading">Loading stock levels...</div>}{isError && !isLoading && <div className="table-loading">We couldn’t load inventory right now. Please refresh and try again.</div>}{!isLoading && !isError && !filteredStock.length && <div className="table-loading">No inventory matches your filters.</div>}{!isLoading && !isError && filteredStock.length > 0 && <div className="table-responsive"><PaginatedTable className="table inventory-table"><thead><tr><th><input type="checkbox" aria-label="Select all inventory" /></th><th>Product</th><th>SKU</th><th>Branch</th><th>Current Stock</th><th>Reorder Level</th><th>Status</th><th>Last Updated</th><th>Actions</th></tr></thead><tbody>{filteredStock.map((item) => <tr key={item.id}><td><input type="checkbox" aria-label={`Select ${item.productName}`} /></td><td><div className="inventory-product"><ProductMark /><span><strong>{item.productName}</strong><small>{item.sku || 'Inventory item'}</small></span></div></td><td>{item.sku || '—'}</td><td><span className="branch-chip">⌖ {branches.find(branch => branch.id === item.branchId)?.name || 'Branch'}</span></td><td><strong>{number(item.quantity)}</strong></td><td>{number(item.reorderLevel)}</td><td><StatusBadge tone={Number(item.quantity) === 0 ? 'danger' : item.lowStock ? 'warning' : 'success'}>{Number(item.quantity) === 0 ? 'Out of Stock' : item.lowStock ? 'Low Stock' : 'Healthy'}</StatusBadge></td><td><span className="updated-date">{date(item.updatedAt)}<small>{time(item.updatedAt)}</small></span></td><td><details className="inventory-row-menu"><summary aria-label={`Actions for ${item.productName}`}><MoreHorizontal size={17}/></summary><div><button onClick={event => { event.currentTarget.closest('details').open = false; setDetailItem(item) }}>View stock details</button>{owner && <><button onClick={event => { event.currentTarget.closest('details').open = false; openRowAction('receive', item) }}>Receive stock</button><button onClick={event => { event.currentTarget.closest('details').open = false; openRowAction('adjust', item) }}>Adjust stock</button><button onClick={event => { event.currentTarget.closest('details').open = false; setFormError(''); setReorderItem(item); setReorderValue(String(item.reorderLevel)) }}>Edit reorder level</button></>}</div></details></td></tr>)}</tbody></PaginatedTable></div>}</div>
    </div><aside className="inventory-aside"><div className="quick-actions-card"><h2><PackagePlus size={17} /> Quick Actions</h2><button className="quick-action-primary" onClick={() => openStockModal('receive')}><span className="plus-icon">+</span> Add Stock</button><button onClick={() => openStockModal('adjust')}><ArrowUpFromLine size={15} /> Record Adjustment</button><button><Search size={15} /> View Movements</button></div><div className="activity-card"><h2>Recent Activity</h2>{movements.slice(0, 5).map((movement) => <div className="activity-item" key={movement.id}><MovementIcon type={movement.movementType} /><div><strong>{movement.movementType === 'RECEIPT' ? 'Stock received' : 'Stock adjusted'}</strong><span>{movement.reference || movement.notes || 'Inventory movement'}</span><small>{date(movement.createdAt)} · {time(movement.createdAt)}</small></div></div>)}{!movements.length && <p className="activity-empty">No stock movements recorded yet.</p>}</div></aside></div>
    {modal && <CrudModal title={modal === 'receive' ? 'Add stock' : 'Record adjustment'} description="Record a stock movement against a branch and product." onClose={() => setModal(null)} onSubmit={submitStock} submitLabel={modal === 'receive' ? 'Receive stock' : 'Save adjustment'} busy={saving} error={formError}><div className="crud-form-grid"><div className="crud-field"><label htmlFor="stock-branch">Branch</label><select id="stock-branch" disabled={Boolean(selected)} required value={form.branchId} onChange={(event) => setForm({ ...form, branchId: event.target.value })}>{branches.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></div><div className="crud-field"><label htmlFor="stock-product">Product</label><select id="stock-product" disabled={Boolean(selected)} required value={form.productId} onChange={(event) => setForm({ ...form, productId: event.target.value })}>{(data?.products || []).map((item) => <option key={item.id} value={item.id}>{item.name} ({item.sku})</option>)}</select></div>{modal === 'receive' ? <><div className="crud-field"><label htmlFor="stock-quantity">Quantity received</label><input id="stock-quantity" type="number" min="0.001" step="0.001" required value={form.quantity} onChange={(event) => setForm({ ...form, quantity: event.target.value })} /></div><div className="crud-field"><label htmlFor="stock-reorder">Reorder level</label><input id="stock-reorder" type="number" min="0" step="0.001" value={form.reorderLevel} onChange={(event) => setForm({ ...form, reorderLevel: event.target.value })} /></div></> : <div className="crud-field full"><label htmlFor="stock-change">Quantity change</label><input id="stock-change" type="number" step="0.001" required value={form.quantityChange} onChange={(event) => setForm({ ...form, quantityChange: event.target.value })} placeholder="Use a negative value to reduce stock" /></div>}<div className="crud-field full"><label htmlFor="stock-reference">Reference</label><input id="stock-reference" value={form.reference} onChange={(event) => setForm({ ...form, reference: event.target.value })} /></div><div className="crud-field full"><label htmlFor="stock-notes">Notes</label><textarea id="stock-notes" value={form.notes} onChange={(event) => setForm({ ...form, notes: event.target.value })} /></div></div></CrudModal>}
    {detailItem && <CrudModal title="Inventory details" onClose={() => setDetailItem(null)} onSubmit={event => { event.preventDefault(); setDetailItem(null) }} submitLabel="Close"><p><strong>{detailItem.productName}</strong> · {detailItem.sku}</p><p>Branch: {branches.find(branch => branch.id === detailItem.branchId)?.name || 'Branch'}</p><p>Available stock: {number(detailItem.quantity)}</p><p>Reorder level: {number(detailItem.reorderLevel)}</p></CrudModal>}
    {reorderItem && <CrudModal title="Edit reorder level" description={reorderItem.productName} onClose={() => setReorderItem(null)} onSubmit={saveReorder} submitLabel="Save changes" busy={saving} error={formError}><div className="crud-field"><label htmlFor="row-reorder">Reorder level</label><input id="row-reorder" type="number" min="0" step="0.001" required value={reorderValue} onChange={event => setReorderValue(event.target.value)}/></div></CrudModal>}
  </section>
}

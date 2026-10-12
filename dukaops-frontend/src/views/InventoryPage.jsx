import { useMemo, useState } from 'react'
import { AlertTriangle, Boxes, CheckCircle2 } from 'lucide-react'
import { useInventory } from '../hooks/useModuleData'
import PageToolbar from '../components/ui/PageToolbar'
import StatusBadge from '../components/ui/StatusBadge'

export default function InventoryPage() {
  const { data, isLoading } = useInventory(); const [search, setSearch] = useState('')
  const stock = useMemo(() => (data?.stock || []).filter((item) => item.productName.toLowerCase().includes(search.toLowerCase())), [data, search])
  return <section className="module-page"><div className="module-heading"><div><p className="eyebrow">STOCK CONTROL</p><h1>Inventory</h1><p>Track stock on hand and spot replenishment needs before they become problems.</p></div><div className="branch-context"><span>Branch</span><strong>{data?.selectedBranch?.name || 'Main Branch'}</strong></div></div><PageToolbar search={search} onSearch={setSearch} onFilter={() => {}} filter="All stock" actionLabel="Receive stock" /><div className="inventory-summary"><div><Boxes size={18} /><span>Total SKUs<strong>{stock.length}</strong></span></div><div><AlertTriangle size={18} /><span>Low stock<strong>{stock.filter((item) => item.lowStock).length}</strong></span></div><div><CheckCircle2 size={18} /><span>Healthy stock<strong>{stock.filter((item) => !item.lowStock).length}</strong></span></div></div><div className="data-card">{isLoading ? <div className="table-loading">Loading stock levels...</div> : <div className="table-responsive"><table className="table module-table"><thead><tr><th>Product</th><th>Quantity on hand</th><th>Reorder level</th><th>Stock status</th></tr></thead><tbody>{stock.map((item) => <tr key={item.productId}><td><strong>{item.productName}</strong><small className="table-subtext">{item.sku || 'Inventory item'}</small></td><td><strong>{item.quantity}</strong></td><td>{item.reorderLevel}</td><td><StatusBadge tone={item.lowStock ? 'warning' : 'success'}>{item.lowStock ? 'Reorder soon' : 'Healthy'}</StatusBadge></td></tr>)}</tbody></table></div>}</div></section>
}

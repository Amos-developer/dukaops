import { ArrowUpRight, MoreHorizontal, PackagePlus, TriangleAlert } from 'lucide-react'
import StatusBadge from '../ui/StatusBadge'

const lowStock = [['Organic Green Tea', 'TEA-014', 24, 30], ['Wheat Flour 2kg', 'FLR-008', 18, 20], ['Cooking Oil 1L', 'OIL-021', 8, 15]]
const movements = [['Premium Coffee Beans', 'Receipt', '+48 units', 'Today, 09:42'], ['Cane Sugar 1kg', 'Adjustment', '+12 units', 'Yesterday, 16:18'], ['Wheat Flour 2kg', 'Receipt', '+36 units', '04 Jun, 14:31']]
const topProducts = [['Premium Coffee Beans', 78], ['Cane Sugar 1kg', 64], ['Organic Green Tea', 24], ['Wheat Flour 2kg', 18]]

export function LowStockTable() {
  return <section className="dashboard-card table-card"><div className="table-card-title"><div><p className="eyebrow">ACTION REQUIRED</p><h2>Low-stock alerts</h2></div><a href="/inventory">View all <ArrowUpRight size={14} /></a></div><div className="compact-list">{lowStock.map(([name, sku, quantity, reorder]) => <div className="compact-row" key={sku}><div className="compact-main"><div className="alert-icon"><TriangleAlert size={15} /></div><div><strong>{name}</strong><span>{sku}</span></div></div><div className="stock-reading"><strong>{quantity}</strong><span>/ {reorder} min.</span></div><StatusBadge tone="warning">Restock</StatusBadge></div>)}</div></section>
}

export function MovementTable() {
  return <section className="dashboard-card table-card"><div className="table-card-title"><div><p className="eyebrow">LATEST ACTIVITY</p><h2>Recent stock movements</h2></div><a href="/movements">View all <ArrowUpRight size={14} /></a></div><div className="compact-list">{movements.map(([name, type, quantity, date]) => <div className="compact-row" key={`${name}-${date}`}><div className="compact-main"><div className="movement-icon"><PackagePlus size={15} /></div><div><strong>{name}</strong><span>{type} · {date}</span></div></div><strong className="quantity-positive">{quantity}</strong></div>)}</div></section>
}

export function ProductInsights() {
  return <section className="dashboard-card table-card"><div className="table-card-title"><div><p className="eyebrow">STOCK POSITION</p><h2>Top products by quantity</h2></div><MoreHorizontal size={18} className="muted-icon" /></div><div className="product-ranking">{topProducts.map(([name, quantity], index) => <div className="ranking-row" key={name}><span className="ranking-number">0{index + 1}</span><div className="ranking-copy"><strong>{name}</strong><div className="ranking-track"><i style={{ width: `${quantity}%` }} /></div></div><b>{quantity}</b></div>)}</div></section>
}

import { ArrowUpRight, MoreHorizontal, PackagePlus, TriangleAlert } from 'lucide-react'
import StatusBadge from '../ui/StatusBadge'

export function LowStockTable({ stock = [] }) {
  const items = stock.filter((item) => item.lowStock).slice(0, 4)
  return <section className="dashboard-card table-card"><div className="table-card-title"><div><p className="eyebrow">ACTION REQUIRED</p><h2>Low-stock alerts</h2></div><a href="/inventory">View all <ArrowUpRight size={14} /></a></div><div className="compact-list">{items.length ? items.map((item) => <div className="compact-row" key={item.productId}><div className="compact-main"><div className="alert-icon"><TriangleAlert size={15} /></div><div><strong>{item.productName}</strong><span>{item.sku}</span></div></div><div className="stock-reading"><strong>{item.quantity}</strong><span>/ {item.reorderLevel} min.</span></div><StatusBadge tone="warning">Restock</StatusBadge></div>) : <div className="table-empty">No low-stock products.</div>}</div></section>
}

export function MovementTable({ movements = [] }) {
  return <section className="dashboard-card table-card"><div className="table-card-title"><div><p className="eyebrow">LATEST ACTIVITY</p><h2>Recent stock movements</h2></div><a href="/movements">View all <ArrowUpRight size={14} /></a></div><div className="compact-list">{movements.length ? movements.map((movement) => <div className="compact-row" key={movement.id}><div className="compact-main"><div className="movement-icon"><PackagePlus size={15} /></div><div><strong>{movement.productName}</strong><span>{movement.movementType} · {new Date(movement.createdAt).toLocaleString()}</span></div></div><strong className="quantity-positive">{movement.quantityChange > 0 ? '+' : ''}{movement.quantityChange}</strong></div>) : <div className="table-empty">No stock movements yet.</div>}</div></section>
}

export function ProductInsights({ products = [] }) {
  const max = Math.max(...products.map((item) => Number(item.quantity || 0)), 1)
  return <section className="dashboard-card table-card"><div className="table-card-title"><div><p className="eyebrow">STOCK POSITION</p><h2>Top products by quantity</h2></div><MoreHorizontal size={18} className="muted-icon" /></div><div className="product-ranking">{products.map((product, index) => <div className="ranking-row" key={product.productId}><span className="ranking-number">0{index + 1}</span><div className="ranking-copy"><strong>{product.productName}</strong><div className="ranking-track"><i style={{ width: `${(Number(product.quantity) / max) * 100}%` }} /></div></div><b>{product.quantity}</b></div>)}</div></section>
}

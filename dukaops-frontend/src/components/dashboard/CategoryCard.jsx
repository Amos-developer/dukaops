import { ChevronDown } from 'lucide-react'
export default function CategoryCard({ stock }) {
  const total = stock.length || 1
  const low = stock.filter((item) => item.lowStock).length
  return <section className="dashboard-card category-card"><div className="category-heading"><div><p className="eyebrow">INVENTORY MIX</p><h2>Top categories</h2></div><button className="period-btn">Weekly <ChevronDown size={14} /></button></div><div className="donut" style={{ '--donut-progress': `${Math.max(18, 100 - (low / total) * 60)}%` }}><div><strong>{stock.length || 0}</strong><span>Products</span></div></div><div className="category-list"><div><span className="category-dot teal" />Healthy stock <strong>{Math.max(0, total - low)}</strong></div><div><span className="category-dot orange" />Low stock <strong>{low}</strong></div></div></section>
}

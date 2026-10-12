import { ArrowDownRight, ArrowUpRight, Boxes, Building2, CircleDollarSign, ShoppingBag, WalletCards } from 'lucide-react'

const iconMap = { revenue: CircleDollarSign, orders: ShoppingBag, expenses: WalletCards, products: Boxes, branches: Building2 }
export default function KpiCard({ label, value, change, trend = 'up', tone = 'blue', icon = 'revenue' }) {
  const Icon = iconMap[icon]
  return <article className="kpi-card"><div className={`kpi-icon ${tone}`}><Icon size={19} /></div><div className="kpi-body"><span>{label}</span><strong>{value}</strong><small className={trend}><>{trend === 'up' ? <ArrowUpRight size={13} /> : <ArrowDownRight size={13} />} {change}</><em>vs last month</em></small></div><button className="kpi-menu">•••</button></article>
}

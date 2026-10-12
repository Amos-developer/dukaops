import { ArrowUpRight, Boxes, ChevronDown, PieChart } from 'lucide-react'

const branchColors = ['#3184ee', '#44b9ae', '#8e7be2', '#f2a45c']

export function StockByBranch({ branches = [] }) {
  return <section className="dashboard-card analytics-card branch-chart-card"><div className="analytics-title"><div><p className="eyebrow">INVENTORY COVERAGE</p><h2>Stock levels by branch</h2></div><button className="period-btn">Live data <ChevronDown size={14} /></button></div><div className="branch-bars">{branches.map(([name, value], index) => <div className="branch-bar-row" key={name}><div className="branch-bar-label"><span>{name}</span><strong>{value}%</strong></div><div className="branch-bar-track"><i style={{ width: `${value}%`, background: branchColors[index % branchColors.length] }} /></div></div>)}</div><div className="analytics-footer"><span><i className="dot blue" /> Current stock coverage</span><a href="/branches">View branches <ArrowUpRight size={13} /></a></div></section>
}

export function MovementTrend({ movements = [] }) {
  const recentCounts = movements.slice(0, 12).map((_, index) => 35 + ((index * 17) % 55))
  const points = (recentCounts.length ? recentCounts : [0]).map((value, index) => `${index * (341 / Math.max(recentCounts.length - 1, 1))},${100 - value}`).join(' ')
  return <section className="dashboard-card analytics-card movement-chart-card"><div className="analytics-title"><div><p className="eyebrow">STOCK ACTIVITY</p><h2>Movement trends</h2></div><button className="period-btn">Live data <ChevronDown size={14} /></button></div><div className="trend-summary"><strong>{movements.length}</strong><span><ArrowUpRight size={13} /> Live <em>stock movements</em></span></div><div className="line-chart"><div className="line-y-axis"><span>100</span><span>75</span><span>50</span><span>25</span><span>0</span></div><svg viewBox="0 0 341 110" preserveAspectRatio="none" aria-label="Stock movement trend chart"><defs><linearGradient id="trendFill" x1="0" x2="0" y1="0" y2="1"><stop offset="0" stopColor="#3184ee" stopOpacity=".22" /><stop offset="1" stopColor="#3184ee" stopOpacity="0" /></linearGradient></defs><path d={`M 0,110 L ${points} L 341,110 Z`} fill="url(#trendFill)" /><polyline points={points} fill="none" stroke="#3184ee" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" /></svg></div><div className="chart-axis-labels"><span>Recent</span><span>Movements</span></div></section>
}

export function CategoryDistribution() {
  return <section className="dashboard-card analytics-card category-empty-card"><div className="analytics-title"><div><p className="eyebrow">PRODUCT MIX</p><h2>Inventory by category</h2></div><PieChart size={20} className="muted-icon" /></div><div className="category-empty"><div className="empty-chart-icon"><Boxes size={24} /></div><strong>Categories are not configured</strong><span>Create product categories to see inventory distribution here.</span><button className="secondary-btn">Set up categories</button></div></section>
}

import { ArrowUpRight, Boxes, ChevronDown, PieChart } from 'lucide-react'

const branchData = [['Main branch', 78, '#3184ee'], ['Westlands', 62, '#44b9ae'], ['Kilimani', 45, '#8e7be2'], ['Industrial area', 31, '#f2a45c']]
const movementData = [34, 46, 42, 58, 51, 67, 61, 74, 62, 81, 70, 88]

export function StockByBranch() {
  return <section className="dashboard-card analytics-card branch-chart-card"><div className="analytics-title"><div><p className="eyebrow">INVENTORY COVERAGE</p><h2>Stock levels by branch</h2></div><button className="period-btn">This month <ChevronDown size={14} /></button></div><div className="branch-bars">{branchData.map(([name, value, color]) => <div className="branch-bar-row" key={name}><div className="branch-bar-label"><span>{name}</span><strong>{value}%</strong></div><div className="branch-bar-track"><i style={{ width: `${value}%`, background: color }} /></div></div>)}</div><div className="analytics-footer"><span><i className="dot blue" /> Current stock coverage</span><a href="/branches">View branches <ArrowUpRight size={13} /></a></div></section>
}

export function MovementTrend() {
  const points = movementData.map((value, index) => `${index * 31},${100 - value}`).join(' ')
  return <section className="dashboard-card analytics-card movement-chart-card"><div className="analytics-title"><div><p className="eyebrow">STOCK ACTIVITY</p><h2>Movement trends</h2></div><button className="period-btn">Last 30 days <ChevronDown size={14} /></button></div><div className="trend-summary"><strong>638</strong><span><ArrowUpRight size={13} /> 16.8% <em>vs last period</em></span></div><div className="line-chart"><div className="line-y-axis"><span>100</span><span>75</span><span>50</span><span>25</span><span>0</span></div><svg viewBox="0 0 341 110" preserveAspectRatio="none" aria-label="Stock movement trend chart"><defs><linearGradient id="trendFill" x1="0" x2="0" y1="0" y2="1"><stop offset="0" stopColor="#3184ee" stopOpacity=".22" /><stop offset="1" stopColor="#3184ee" stopOpacity="0" /></linearGradient></defs><path d={`M 0,110 L ${points} L 341,110 Z`} fill="url(#trendFill)" /><polyline points={points} fill="none" stroke="#3184ee" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" /></svg></div><div className="chart-axis-labels"><span>01 Jun</span><span>08 Jun</span><span>15 Jun</span><span>22 Jun</span><span>30 Jun</span></div></section>
}

export function CategoryDistribution() {
  return <section className="dashboard-card analytics-card category-empty-card"><div className="analytics-title"><div><p className="eyebrow">PRODUCT MIX</p><h2>Inventory by category</h2></div><PieChart size={20} className="muted-icon" /></div><div className="category-empty"><div className="empty-chart-icon"><Boxes size={24} /></div><strong>Categories are not configured</strong><span>Create product categories to see inventory distribution here.</span><button className="secondary-btn">Set up categories</button></div></section>
}

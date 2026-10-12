import { useMemo } from 'react'
import { ArrowUpRight, Boxes, GitBranch } from 'lucide-react'
import { useDashboard } from '../hooks/useDashboard'
import KpiCard from '../components/dashboard/KpiCard'
import { CategoryDistribution, MovementTrend, StockByBranch } from '../components/dashboard/DashboardAnalytics'
import { LowStockTable, MovementTable, ProductInsights } from '../components/dashboard/DashboardTables'

export default function DashboardPage() {
  const { data, isLoading } = useDashboard()
  const stats = useMemo(() => { const products = data?.products || []; const stock = data?.stock || []; return { activeProducts: products.filter((item) => item.status === 'ACTIVE').length, lowStock: stock.filter((item) => item.lowStock).length, branches: data?.branches?.length || 0 } }, [data])
  if (isLoading) return <div className="loading-state"><div className="spinner-border text-primary" role="status" /><p>Loading workspace data...</p></div>
  return <>
    <div className="kpi-grid dashboard-kpis"><KpiCard label="Active products" value={stats.activeProducts} change="8.2%" tone="blue" icon="products" /><KpiCard label="Total branches" value={stats.branches} change="2.4%" tone="purple" icon="branches" /><KpiCard label="Low-stock products" value={stats.lowStock} change="4.6%" trend="down" tone="orange" icon="orders" /><KpiCard label="Movements today" value="12" change="16.8%" tone="teal" icon="expenses" /><KpiCard label="Inventory value" value="KES —" change="Coming soon" tone="blue" icon="revenue" /></div>
    <div className="analytics-grid"><StockByBranch /><MovementTrend /><CategoryDistribution /></div>
    <div className="dashboard-table-grid"><LowStockTable /><MovementTable /></div>
    <div className="dashboard-table-grid single-table-grid"><ProductInsights /><section className="dashboard-card table-card branch-summary-card"><div className="table-card-title"><div><p className="eyebrow">COVERAGE</p><h2>Branch inventory summary</h2></div><a href="/branches">View all <ArrowUpRight size={14} /></a></div><div className="branch-summary-list"><div><div><GitBranch size={16} /><strong>Main branch</strong></div><span>184 units</span><i><b style={{ width: '82%' }} /></i></div><div><div><GitBranch size={16} /><strong>Westlands</strong></div><span>142 units</span><i><b style={{ width: '63%' }} /></i></div><div><div><GitBranch size={16} /><strong>Kilimani</strong></div><span>98 units</span><i><b style={{ width: '44%' }} /></i></div></div></section></div>
    <div className="dashboard-footnote"><Boxes size={15} /> Team activity and system notifications will appear here when audit logging is enabled.</div>
  </>
}

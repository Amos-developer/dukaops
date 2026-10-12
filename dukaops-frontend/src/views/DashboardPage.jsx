import { useMemo } from 'react'
import { ArrowUpRight, Boxes, GitBranch } from 'lucide-react'
import { useDashboard } from '../hooks/useDashboard'
import KpiCard from '../components/dashboard/KpiCard'
import { CategoryDistribution, MovementTrend, StockByBranch } from '../components/dashboard/DashboardAnalytics'
import { LowStockTable, MovementTable, ProductInsights } from '../components/dashboard/DashboardTables'

export default function DashboardPage() {
  const { data, isLoading, isError, error, refetch } = useDashboard()
  const computed = useMemo(() => {
    const products = data?.products || []
    const stock = data?.stock || []
    const productMap = new Map(products.map((product) => [product.id, product]))
    const movements = data?.movements || []
    const today = new Date().toISOString().slice(0, 10)
    const inventoryValue = stock.reduce((sum, item) => sum + (Number(item.quantity || 0) * Number(productMap.get(item.productId)?.unitPrice || 0)), 0)
    const branchTotals = (data?.stockByBranch || []).map((branch) => ({ ...branch, total: branch.stock.reduce((sum, item) => sum + Number(item.quantity || 0), 0) }))
    const maxBranchTotal = Math.max(...branchTotals.map((branch) => branch.total), 1)
    return {
      products, stock, movements, productMap, branchTotals,
      activeProducts: products.filter((item) => item.status === 'ACTIVE').length,
      lowStock: stock.filter((item) => item.lowStock).length,
      branches: data?.branches?.length || 0,
      movementsToday: movements.filter((item) => item.createdAt?.startsWith(today)).length,
      inventoryValue,
      branchChart: branchTotals.map((branch) => [branch.name, Math.round((branch.total / maxBranchTotal) * 100)]),
      recentMovements: movements.slice(0, 4).map((movement) => ({ ...movement, productName: productMap.get(movement.productId)?.name || 'Product' })),
      topProducts: stock.slice().sort((a, b) => Number(b.quantity) - Number(a.quantity)).slice(0, 4),
    }
  }, [data])
  if (isLoading) return <div className="loading-state"><div className="spinner-border text-primary" role="status" /><p>Loading workspace data...</p></div>
  if (isError) return <div className="loading-state error-state"><strong>Unable to load live inventory data.</strong><p>{error?.message || 'Check that the backend is running and your frontend credentials are configured.'}</p><button className="primary-btn" onClick={() => refetch()}>Try again</button></div>
  return <>
    <div className="kpi-grid dashboard-kpis"><KpiCard label="Active products" value={computed.activeProducts} change="Live" tone="blue" icon="products" /><KpiCard label="Total branches" value={computed.branches} change="Live" tone="purple" icon="branches" /><KpiCard label="Low-stock products" value={computed.lowStock} change="Live" trend="down" tone="orange" icon="orders" /><KpiCard label="Movements today" value={computed.movementsToday} change="Live" tone="teal" icon="expenses" /><KpiCard label="Inventory value" value={`KES ${computed.inventoryValue.toLocaleString()}`} change="Live" tone="blue" icon="revenue" /></div>
    <div className="analytics-grid"><StockByBranch branches={computed.branchChart} /><MovementTrend movements={computed.movements} /><CategoryDistribution /></div>
    <div className="dashboard-table-grid"><LowStockTable stock={computed.stock} /><MovementTable movements={computed.recentMovements} /></div>
    <div className="dashboard-table-grid single-table-grid"><ProductInsights products={computed.topProducts} /><section className="dashboard-card table-card branch-summary-card"><div className="table-card-title"><div><p className="eyebrow">COVERAGE</p><h2>Branch inventory summary</h2></div><a href="/branches">View all <ArrowUpRight size={14} /></a></div><div className="branch-summary-list">{computed.branchTotals.map((branch) => <div key={branch.id}><div><GitBranch size={16} /><strong>{branch.name}</strong></div><span>{branch.total} units</span><i><b style={{ width: `${Math.min(100, (branch.total / Math.max(...computed.branchTotals.map((item) => item.total), 1)) * 100)}%` }} /></i></div>)}</div></section></div>
    <div className="dashboard-footnote"><Boxes size={15} /> Team activity and system notifications will appear here when audit logging is enabled.</div>
  </>
}

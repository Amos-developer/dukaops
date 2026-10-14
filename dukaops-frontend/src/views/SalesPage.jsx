import { useQuery } from '@tanstack/react-query'
import { api } from '../services/api'

export default function SalesPage() {
  const summary = useQuery({ queryKey: ['sales-summary'], queryFn: api.salesSummary })
  const orders = useQuery({ queryKey: ['sales-orders'], queryFn: () => api.salesOrders({ size: 50 }) })
  const data = summary.data || {}
  const rows = orders.data?.content || []
  return <section className="sales-page"><div className="module-heading"><div><p className="eyebrow">SALES &amp; ORDERS</p><h1>Sales overview</h1><p>Track completed sales and pending customer orders.</p></div></div><div className="sales-metrics"><div><small>Revenue</small><strong>TSH {Number(data.revenue || 0).toLocaleString()}</strong></div><div><small>Completed sales</small><strong>{data.completedSales || 0}</strong></div><div><small>Pending orders</small><strong>{data.pendingOrders || 0}</strong></div><div><small>Average sale</small><strong>TSH {Number(data.averageSaleValue || 0).toLocaleString()}</strong></div></div><div className="data-card"><div className="data-card-heading"><strong>Recent orders</strong><span>{orders.isLoading ? 'Loading...' : `${rows.length} records`}</span></div><div className="table-responsive"><table className="table module-table"><thead><tr><th>Order</th><th>Customer</th><th>Status</th><th>Total</th><th>Date</th></tr></thead><tbody>{rows.map((order) => <tr key={order.id}><td>{order.id.slice(0, 8)}</td><td>{order.customerName || 'Walk-in customer'}</td><td>{order.status}</td><td>TSH {Number(order.totalAmount).toLocaleString()}</td><td>{new Date(order.createdAt).toLocaleDateString()}</td></tr>)}</tbody></table>{!orders.isLoading && !rows.length && <div className="table-loading">No sales recorded yet.</div>}</div></div></section>
}

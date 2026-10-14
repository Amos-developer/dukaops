import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowRight, Plus, Search } from 'lucide-react'
import { api } from '../services/api'

const money = (value) => `TSH ${Number(value || 0).toLocaleString()}`

export default function SalesPage() {
  const [search, setSearch] = useState(''); const [status, setStatus] = useState('')
  const summary = useQuery({ queryKey: ['sales-summary'], queryFn: api.salesSummary })
  const orders = useQuery({ queryKey: ['sales-orders', search, status], queryFn: () => api.salesOrders({ search, status, size: 50 }) })
  const data = summary.data || {}; const rows = orders.data?.content || []
  return <section className="sales-page"><div className="sales-heading"><div><p className="eyebrow">SALES &amp; ORDERS</p><h1>Sales &amp; Orders</h1><p>Manage transactions, customer orders, and payments from one place.</p></div><Link className="sales-primary" to="/sales?view=pos"><Plus size={15}/> New Sale</Link></div>
    {(summary.isError || orders.isError) && <div className="sales-error">We couldn’t load sales data right now. Please check the backend connection and try again.</div>}
    <div className="sales-metrics"><div><small>Total Revenue</small><strong>{money(data.revenue)}</strong><em>Completed sales</em></div><div><small>Completed Sales</small><strong>{data.completedSales || 0}</strong><em>Successful transactions</em></div><div><small>Pending Orders</small><strong>{data.pendingOrders || 0}</strong><em>Awaiting action</em></div><div><small>Average Sale</small><strong>{money(data.averageSaleValue)}</strong><em>Per completed sale</em></div></div>
    <div className="sales-links"><Link to="/sales?view=orders">View all orders <ArrowRight size={14}/></Link><Link to="/sales?view=pos">Open POS <ArrowRight size={14}/></Link></div>
    <div className="sales-toolbar"><label><Search size={15}/><input value={search} onChange={(e)=>setSearch(e.target.value)} placeholder="Search customer or order"/></label><select value={status} onChange={(e)=>setStatus(e.target.value)}><option value="">All statuses</option><option value="PENDING">Pending</option><option value="CONFIRMED">Confirmed</option><option value="COMPLETED">Completed</option><option value="CANCELLED">Cancelled</option></select><input type="date" aria-label="From date"/><input type="date" aria-label="To date"/></div>
    <div className="data-card"><div className="data-card-heading"><strong>Recent transactions</strong><span>{orders.isLoading ? 'Loading...' : `${rows.length} records`}</span></div><div className="table-responsive"><table className="table module-table"><thead><tr><th>Order</th><th>Customer</th><th>Status</th><th>Total</th><th>Date</th></tr></thead><tbody>{rows.map((order)=><tr key={order.id}><td>#{order.id.slice(0,8)}</td><td>{order.customerName || 'Walk-in customer'}</td><td><span className="sales-status">{order.status}</span></td><td>{money(order.totalAmount)}</td><td>{order.createdAt ? new Date(order.createdAt).toLocaleDateString() : '—'}</td></tr>)}</tbody></table>{!orders.isLoading && !rows.length && <div className="sales-empty"><strong>No sales yet</strong><span>Completed transactions will appear here once you make your first sale.</span><Link to="/sales?view=pos">Create a sale <ArrowRight size={14}/></Link></div>}</div></div>
  </section>
}

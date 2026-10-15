import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Store, Package, Boxes } from 'lucide-react'
import { api } from '../services/api'
import PaginatedTable from '../components/ui/PaginatedTable'
import StatusBadge from '../components/ui/StatusBadge'
import '../styles/branch-details.css'

const formatDate = value => value ? new Date(value).toLocaleString() : '—'
export default function BranchDetailsPage() {
  const { branchId } = useParams()
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('ALL')
  const session = useQuery({ queryKey: ['session'], queryFn: api.currentUser })
  const branch = useQuery({ queryKey: ['branch-details', session.data?.tenantId, branchId], queryFn: () => api.branchDetails(session.data.tenantId, branchId), enabled: Boolean(session.data?.tenantId) })
  const inventory = useQuery({ queryKey: ['branch-stock', branchId], queryFn: () => api.stock(branchId), enabled: branch.isSuccess, refetchOnMount: 'always' })
  const products = inventory.data || []
  const rows = products.filter(item => `${item.productName} ${item.sku}`.toLowerCase().includes(search.toLowerCase()) && (status === 'ALL' || (status === 'LOW' && item.lowStock) || (status === 'OUT' && Number(item.quantity) === 0)))
  const details = branch.data
  return <section className="branch-details-page"><Link className="branch-back" to="/branches"><ArrowLeft size={15}/> Back to branches</Link><header><div><h1><Store size={18}/> {details?.name || 'Branch details'}</h1><p>Branch information and products held at this location.</p></div><Link to="/inventory" className="branch-inventory-link">Manage inventory</Link></header>{branch.isPending ? <p role="status">Loading branch details…</p> : branch.isError ? <p role="alert">{branch.error.message}</p> : <><div className="branch-profile"><div><small>Branch name</small><strong>{details.name}</strong></div><div><small>Branch code</small><strong>{details.code}</strong></div><div><small>Type</small><strong>{details.isMain ? 'Main branch' : 'Branch'}</strong></div><div><small>Status</small><StatusBadge>{details.status}</StatusBadge></div><div><small>Created</small><strong>{formatDate(details.createdAt)}</strong></div><div><small>Last updated</small><strong>{formatDate(details.updatedAt)}</strong></div></div><div className="branch-stock-summary"><span><Package size={18}/><strong>{inventory.isSuccess ? products.length : '—'}</strong> Stocked products</span><span><Boxes size={18}/><strong>{inventory.isSuccess ? products.reduce((total, item) => total + Number(item.quantity), 0).toLocaleString() : '—'}</strong> Total quantity</span></div><section className="branch-stock-panel"><div className="branch-stock-toolbar"><h2>Branch products</h2><input aria-label="Search branch products" placeholder="Search product name or SKU" value={search} onChange={event => setSearch(event.target.value)}/><select aria-label="Stock status" value={status} onChange={event => setStatus(event.target.value)}><option value="ALL">All stock</option><option value="LOW">Low stock</option><option value="OUT">Out of stock</option></select></div>{inventory.isPending ? <p role="status">Loading products…</p> : inventory.isError ? <p role="alert">{inventory.error.message}</p> : rows.length ? <div className="table-responsive"><PaginatedTable className="table branch-stock-table"><thead><tr><th>Product</th><th>SKU</th><th>Quantity</th><th>Reorder level</th><th>Status</th><th>Updated</th></tr></thead><tbody>{rows.map(item => <tr key={item.id}><td>{item.productName}</td><td>{item.sku}</td><td>{Number(item.quantity).toLocaleString()}</td><td>{Number(item.reorderLevel).toLocaleString()}</td><td><StatusBadge>{Number(item.quantity) === 0 ? 'Out of stock' : item.lowStock ? 'Low stock' : 'Healthy'}</StatusBadge></td><td>{formatDate(item.updatedAt)}</td></tr>)}</tbody></PaginatedTable></div> : <p>No products match. Products appear here after stock is received into this branch.</p>}</section></>}</section>
}

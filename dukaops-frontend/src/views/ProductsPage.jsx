import PaginatedTable from '../components/ui/PaginatedTable'
import { useMemo, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Check,
  Download,
  Filter,
  Grid2X2,
  List,
  MoreHorizontal,
  PackageOpen,
  Plus,
  Search,
  Trash2,
  X,
} from 'lucide-react'
import { useProducts } from '../hooks/useModuleData'
import StatusBadge from '../components/ui/StatusBadge'
import CrudModal from '../components/ui/CrudModal'
import { api } from '../services/api'
import { useFeedback } from '../components/ui/FeedbackProvider'

const tabs = [
  { key: 'ALL', label: 'All' },
  { key: 'ACTIVE', label: 'Active' },
  { key: 'DRAFTS', label: 'Drafts' },
  { key: 'ARCHIVED', label: 'Archived' },
]

function initials(name = '') {
  return name.split(' ').filter(Boolean).slice(0, 2).map((part) => part[0]).join('').toUpperCase() || 'P'
}

function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString(undefined, { month: 'short', day: '2-digit', year: 'numeric' })
}

function ProductMetric({ icon, label, value, detail }) {
  return (
    <div className="product-metric">
      <div className="product-metric-top"><span className="product-metric-icon">{icon}</span><span className="product-metric-label">{label}</span><MoreHorizontal size={16} /></div>
      <strong>{value}</strong>
      <span className="product-metric-detail">{detail}</span>
    </div>
  )
}

function ProductIdentity({ product }) {
  return <div className="product-identity"><span className="product-avatar">{initials(product.name)}</span><span><strong>{product.name || 'Unnamed product'}</strong><small>{product.description || 'Product catalogue item'}</small></span></div>
}

function ProductStatus({ status }) {
  const active = status === 'ACTIVE'
  return <StatusBadge tone={active ? 'success' : 'neutral'}>{active ? 'Published' : 'Inactive'}</StatusBadge>
}

export default function ProductsPage() {
  const { data, isLoading, isError } = useProducts()
  const queryClient = useQueryClient()
  const feedback = useFeedback()
  const session = useQuery({ queryKey: ['session'], queryFn: api.currentUser })
  const owner = session.data?.role === 'OWNER'
  const [deleting, setDeleting] = useState(null)
  const deleteProduct = async event => { event.preventDefault(); setSaving(true); setFormError(''); try { await api.deleteProduct(deleting.id); setSelected(current => current.filter(id => id !== deleting.id)); await queryClient.invalidateQueries(); setDeleting(null); feedback.success('Product deleted successfully.') } catch (failure) { setFormError(failure.message); feedback.error(failure.message) } finally { setSaving(false) } }
  const [search, setSearch] = useState('')
  const [statusTab, setStatusTab] = useState('ALL')
  const [selected, setSelected] = useState([])
  const [view, setView] = useState('list')
  const [modal, setModal] = useState(null)
  const [form, setForm] = useState({ sku: '', name: '', description: '', unitPrice: '', costPrice: '' })
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const allProducts = data?.products || []

  const products = useMemo(() => allProducts.filter((product) => {
    const query = search.trim().toLowerCase()
    const matchesSearch = !query || `${product.name || ''} ${product.sku || ''}`.toLowerCase().includes(query)
    const matchesStatus = statusTab === 'ALL' || (statusTab === 'ACTIVE' && product.status === 'ACTIVE') || (statusTab === 'DRAFTS' && product.status === 'DRAFT') || (statusTab === 'ARCHIVED' && product.status === 'INACTIVE')
    return matchesSearch && matchesStatus
  }), [allProducts, search, statusTab])

  const totalValue = allProducts.reduce((sum, product) => sum + Number(product.unitPrice || 0), 0)
  const toggleSelected = (id) => setSelected((current) => current.includes(id) ? current.filter((item) => item !== id) : [...current, id])
  const selectAll = () => setSelected(selected.length === products.length ? [] : products.map((product) => product.id))
  const openCreate = () => { setForm({ sku: '', name: '', description: '', unitPrice: '', costPrice: '' }); setFormError(''); setModal({ type: 'create' }) }
  const openEdit = (product) => { setForm({ sku: product.sku || '', name: product.name || '', description: product.description || '', unitPrice: product.unitPrice || '', costPrice: product.costPrice || '' }); setFormError(''); setModal({ type: 'edit', product }) }
  const submitProduct = async (event) => { event.preventDefault(); setSaving(true); setFormError(''); try { const payload = { ...form, unitPrice: Number(form.unitPrice), costPrice: form.costPrice === '' ? null : Number(form.costPrice) }; if (modal.type === 'create') { await api.createProduct(payload); feedback.success('Product created successfully.') } else { await api.updateProduct(modal.product.id, payload); feedback.success('Product updated successfully.') } await queryClient.invalidateQueries({ queryKey: ['products'] }); setModal(null) } catch (error) { setFormError(error.message); feedback.error(error.message) } finally { setSaving(false) } }
  const toggleStatus = async (product) => { setSaving(true); try { const nextStatus = product.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'; await api.changeProductStatus(product.id, nextStatus); await queryClient.invalidateQueries({ queryKey: ['products'] }); feedback.success(nextStatus === 'ACTIVE' ? 'Product activated successfully.' : 'Product archived successfully.') } catch (error) { setFormError(error.message); feedback.error(error.message); setModal({ type: 'error' }) } finally { setSaving(false) } }

  return (
    <section className="module-page products-page">
      <div className="products-heading module-heading">
        <div><p className="eyebrow">CATALOGUE</p><h1>Products</h1><p>Manage inventory, pricing and availability across your store.</p></div>
        <div className="products-heading-actions"><button className="secondary-btn"><Download size={15} /> Export</button><button className="primary-btn" onClick={openCreate}><Plus size={15} /> Add product</button></div>
      </div>

      <div className="products-kpis">
        <ProductMetric icon={<PackageOpen size={15} />} label="Total products" value={allProducts.length.toLocaleString()} detail="Live catalogue" />
        <ProductMetric icon={<span className="metric-symbol">TSH</span>} label="Catalogue value" value={`TSH ${totalValue.toLocaleString()}`} detail="Based on unit prices" />
        <ProductMetric icon={<span className="metric-symbol">↗</span>} label="Total orders" value="—" detail="Sales module coming soon" />
        <ProductMetric icon={<span className="metric-symbol">◎</span>} label="Customers" value="—" detail="Customer data coming soon" />
      </div>

      <div className="products-toolbar">
        <label className="product-search"><Search size={16} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by product name or ID" /></label>
        <button className="toolbar-btn"><Filter size={15} /> Filter</button>
        <div className="view-toggle" aria-label="View options"><button className={view === 'grid' ? 'active' : ''} onClick={() => setView('grid')} aria-label="Grid view"><Grid2X2 size={15} /></button><button className={view === 'list' ? 'active' : ''} onClick={() => setView('list')} aria-label="List view"><List size={15} /></button></div>
        <div className="status-tabs">{tabs.map((tab) => <button key={tab.key} className={statusTab === tab.key ? 'active' : ''} onClick={() => setStatusTab(tab.key)}>{tab.label}</button>)}</div>
      </div>

      <div className="data-card products-data-card">
        <div className="data-card-heading"><div><strong>All products</strong><span>{products.length} product{products.length === 1 ? '' : 's'} in your catalogue</span></div><PackageOpen size={19} /></div>
        {isLoading && <div className="table-loading">Loading products...</div>}
        {isError && !isLoading && <div className="table-loading">We couldn’t load products right now. Please refresh and try again.</div>}
        {!isLoading && !isError && !products.length && <div className="table-loading">No products match your current filters.</div>}
        {!isLoading && !isError && products.length > 0 && <>
          <div className="table-responsive products-desktop-table">
            <PaginatedTable className="table products-table"><thead><tr><th><input type="checkbox" checked={products.length > 0 && selected.length === products.length} onChange={selectAll} aria-label="Select all products" /></th><th>Product name</th><th>ID &amp; create date</th><th>Price</th><th>Stock</th><th>Status</th><th>Actions</th></tr></thead><tbody>{products.map((product) => <tr key={product.id}><td><input type="checkbox" checked={selected.includes(product.id)} onChange={() => toggleSelected(product.id)} aria-label={`Select ${product.name}`} /></td><td><ProductIdentity product={product} /></td><td><strong>{product.sku || `#${product.id}`}</strong><small className="table-subtext">{formatDate(product.createdAt)}</small></td><td><strong>TSH {Number(product.unitPrice || 0).toLocaleString()}</strong><small className="table-subtext">Unit price</small></td><td>—</td><td><ProductStatus status={product.status} /></td><td><div className="row-actions"><button className="row-action" onClick={() => openEdit(product)}>Edit</button><button className="row-action" onClick={() => toggleStatus(product)} disabled={saving}>{product.status === 'ACTIVE' ? 'Archive' : 'Activate'}</button>{owner && <button className="row-action" disabled={saving} onClick={() => { setFormError(''); setDeleting(product) }}>Delete</button>}</div></td></tr>)}</tbody></PaginatedTable>
          </div>
          <div className="products-mobile-list">{products.map((product) => <article className="product-mobile-card" key={product.id}><div className="product-mobile-top"><input type="checkbox" checked={selected.includes(product.id)} onChange={() => toggleSelected(product.id)} aria-label={`Select ${product.name}`} /><ProductIdentity product={product} />{owner && <button className="row-action" onClick={() => { setFormError(''); setDeleting(product) }}>Delete</button>}</div><div className="product-mobile-meta"><span><small>SKU</small><strong>{product.sku || `#${product.id}`}</strong></span><span><small>Price</small><strong>TSH {Number(product.unitPrice || 0).toLocaleString()}</strong></span><span><small>Status</small><ProductStatus status={product.status} /></span></div></article>)}</div>
        </>}
      </div>

      {selected.length > 0 && <div className="bulk-bar"><span>{selected.length} Selected</span><button><Download size={15} /> Export</button><button><span>↗</span> Edit info</button>{owner && <button disabled={selected.length !== 1} title="Select one product to delete" onClick={() => { setFormError(''); setDeleting(allProducts.find(product => product.id === selected[0])) }}><Trash2 size={15} /> Delete</button>}<button className="bulk-close" onClick={() => setSelected([])} aria-label="Clear selected products"><X size={16} /></button></div>}
      {modal && modal.type !== 'error' && <CrudModal title={modal.type === 'create' ? 'Add product' : 'Edit product'} description="Keep your catalogue details accurate and ready for stock operations." onClose={() => setModal(null)} onSubmit={submitProduct} submitLabel={modal.type === 'create' ? 'Create product' : 'Save product'} busy={saving} error={formError}><div className="crud-form-grid"><div className="crud-field"><label htmlFor="product-sku">SKU</label><input id="product-sku" required value={form.sku} onChange={(event) => setForm({ ...form, sku: event.target.value })} /></div><div className="crud-field"><label htmlFor="product-name">Product name</label><input id="product-name" required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></div><div className="crud-field"><label htmlFor="product-price">Unit price (TSH)</label><input id="product-price" type="number" min="0" step="0.01" required value={form.unitPrice} onChange={(event) => setForm({ ...form, unitPrice: event.target.value })} /></div><div className="crud-field"><label htmlFor="product-cost">Cost price (TSH)</label><input id="product-cost" type="number" min="0" step="0.01" value={form.costPrice} onChange={(event) => setForm({ ...form, costPrice: event.target.value })} /></div><div className="crud-field full"><label htmlFor="product-description">Description</label><textarea id="product-description" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} /></div></div></CrudModal>}
      {modal?.type === 'error' && <CrudModal title="Could not update product" onClose={() => setModal(null)} onSubmit={(event) => { event.preventDefault(); setModal(null) }} submitLabel="Close" error={formError}><p className="crud-help">Please try again after checking your connection.</p></CrudModal>}
      {deleting && <CrudModal title="Delete product" description="This action permanently removes an unused product." onClose={() => setDeleting(null)} onSubmit={deleteProduct} submitLabel="Delete product" busy={saving} error={formError}><p>Delete <strong>{deleting.name}</strong> ({deleting.sku})?</p><p>Products with stock or transaction history must be archived instead.</p></CrudModal>}
    </section>
  )
}

import { useMemo, useState } from 'react'
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
  const [search, setSearch] = useState('')
  const [statusTab, setStatusTab] = useState('ALL')
  const [selected, setSelected] = useState([])
  const [view, setView] = useState('list')
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

  return (
    <section className="module-page products-page">
      <div className="products-heading module-heading">
        <div><p className="eyebrow">CATALOGUE</p><h1>Products</h1><p>Manage inventory, pricing and availability across your store.</p></div>
        <div className="products-heading-actions"><button className="secondary-btn"><Download size={15} /> Export</button><button className="primary-btn"><Plus size={15} /> Add product</button></div>
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
            <table className="table products-table"><thead><tr><th><input type="checkbox" checked={products.length > 0 && selected.length === products.length} onChange={selectAll} aria-label="Select all products" /></th><th>Product name</th><th>ID &amp; create date</th><th>Price</th><th>Stock</th><th>Status</th><th /></tr></thead><tbody>{products.map((product) => <tr key={product.id}><td><input type="checkbox" checked={selected.includes(product.id)} onChange={() => toggleSelected(product.id)} aria-label={`Select ${product.name}`} /></td><td><ProductIdentity product={product} /></td><td><strong>{product.sku || `#${product.id}`}</strong><small className="table-subtext">{formatDate(product.createdAt)}</small></td><td><strong>TSH {Number(product.unitPrice || 0).toLocaleString()}</strong><small className="table-subtext">Unit price</small></td><td>—</td><td><ProductStatus status={product.status} /></td><td className="text-end"><button className="icon-btn" aria-label={`More actions for ${product.name}`}><MoreHorizontal size={17} /></button></td></tr>)}</tbody></table>
          </div>
          <div className="products-mobile-list">{products.map((product) => <article className="product-mobile-card" key={product.id}><div className="product-mobile-top"><input type="checkbox" checked={selected.includes(product.id)} onChange={() => toggleSelected(product.id)} aria-label={`Select ${product.name}`} /><ProductIdentity product={product} /><button className="icon-btn"><MoreHorizontal size={17} /></button></div><div className="product-mobile-meta"><span><small>SKU</small><strong>{product.sku || `#${product.id}`}</strong></span><span><small>Price</small><strong>TSH {Number(product.unitPrice || 0).toLocaleString()}</strong></span><span><small>Status</small><ProductStatus status={product.status} /></span></div></article>)}</div>
        </>}
      </div>

      {selected.length > 0 && <div className="bulk-bar"><span>{selected.length} Selected</span><button><Download size={15} /> Export</button><button><span>↗</span> Edit info</button><button><Trash2 size={15} /> Delete</button><button className="bulk-close" onClick={() => setSelected([])} aria-label="Clear selected products"><X size={16} /></button></div>}
    </section>
  )
}

import { useMemo, useState } from 'react'
import { MoreHorizontal, PackageOpen } from 'lucide-react'
import { useProducts } from '../hooks/useModuleData'
import PageToolbar from '../components/ui/PageToolbar'
import StatusBadge from '../components/ui/StatusBadge'

export default function ProductsPage() {
  const { data, isLoading } = useProducts(); const [search, setSearch] = useState('')
  const products = useMemo(() => (data?.products || []).filter((product) => `${product.name} ${product.sku}`.toLowerCase().includes(search.toLowerCase())), [data, search])
  return <section className="module-page"><div className="module-heading"><div><p className="eyebrow">CATALOGUE</p><h1>Products</h1><p>Keep your product catalogue, prices, and availability up to date.</p></div></div><PageToolbar search={search} onSearch={setSearch} onFilter={() => {}} filter="All status" actionLabel="Add product" /><div className="data-card"><div className="data-card-heading"><div><strong>All products</strong><span>{products.length} products in your catalogue</span></div><PackageOpen size={20} /></div>{isLoading ? <div className="table-loading">Loading products...</div> : <div className="table-responsive"><table className="table module-table"><thead><tr><th>Product</th><th>SKU</th><th>Unit price</th><th>Status</th><th /></tr></thead><tbody>{products.map((product) => <tr key={product.id}><td><div className="entity-cell"><div className="entity-icon"><PackageOpen size={16} /></div><strong>{product.name}</strong></div></td><td>{product.sku}</td><td>TSH {Number(product.unitPrice || 0).toLocaleString()}</td><td><StatusBadge tone={product.status === 'ACTIVE' ? 'success' : 'neutral'}>{product.status === 'ACTIVE' ? 'Active' : 'Inactive'}</StatusBadge></td><td className="text-end"><button className="icon-btn"><MoreHorizontal size={17} /></button></td></tr>)}</tbody></table></div>}</div></section>
}

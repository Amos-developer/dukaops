import { Plus, Search, SlidersHorizontal } from 'lucide-react'

export default function PageToolbar({ search, onSearch, filter, onFilter, actionLabel }) {
  return <div className="page-toolbar"><div className="module-search"><Search size={16} /><input value={search} onChange={(event) => onSearch(event.target.value)} placeholder="Search..." /></div><div className="toolbar-actions"><button className="secondary-btn" onClick={onFilter}><SlidersHorizontal size={15} /> {filter || 'Filter'}</button>{actionLabel && <button className="primary-btn"><Plus size={15} /> {actionLabel}</button>}</div></div>
}

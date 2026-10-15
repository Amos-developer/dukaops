import { Children, cloneElement, isValidElement, useState } from 'react'
import './pagination.css'

const PAGE_SIZE = 10

export default function PaginatedTable({ children, ...tableProps }) {
  const parts = Children.toArray(children)
  const body = parts.find(part => isValidElement(part) && part.type === 'tbody')
  const rows = Children.toArray(body?.props.children)
  const signature = rows.map(row => row.key).join('|')
  const [navigation, setNavigation] = useState({ signature, page: 0 })
  const pages = Math.max(1, Math.ceil(rows.length / PAGE_SIZE))
  const page = navigation.signature === signature ? Math.min(navigation.page, pages - 1) : 0
  const start = page * PAGE_SIZE
  const navigate = next => setNavigation({ signature, page: next })
  return <><table {...tableProps}>{parts.map(part => part === body ? cloneElement(body, {}, rows.slice(start, start + PAGE_SIZE)) : part)}</table>{rows.length > 0 && <nav className="table-pagination" aria-label="Table pagination"><span role="status">Showing {start + 1}–{Math.min(start + PAGE_SIZE, rows.length)} of {rows.length} records</span><div><button type="button" onClick={() => navigate(0)} disabled={page === 0} aria-label="First page">«</button><button type="button" onClick={() => navigate(page - 1)} disabled={page === 0}>Previous</button><span>Page {page + 1} of {pages}</span><button type="button" onClick={() => navigate(page + 1)} disabled={page >= pages - 1}>Next</button><button type="button" onClick={() => navigate(pages - 1)} disabled={page >= pages - 1} aria-label="Last page">»</button></div></nav>}</>
}

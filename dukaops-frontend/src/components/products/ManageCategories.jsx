import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import CrudModal from '../ui/CrudModal'
import PaginatedTable from '../ui/PaginatedTable'
import { useFeedback } from '../ui/FeedbackProvider'
import { api } from '../../services/api'
import '../../styles/categories.css'

export default function ManageCategories({ onClose }) {
  const categories = useQuery({ queryKey: ['categories'], queryFn: api.categories })
  const client = useQueryClient()
  const feedback = useFeedback()
  const [editing, setEditing] = useState(null)
  const [name, setName] = useState('')
  const [search, setSearch] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const reset = () => { setEditing(null); setName(''); setError('') }
  const save = async event => {
    event.preventDefault()
    if (!name.trim()) return setError('Enter a category name.')
    setBusy(true); setError('')
    try {
      const payload = { name: name.trim(), active: editing?.active ?? true }
      if (editing) await api.updateCategory(editing.id, payload)
      else await api.createCategory(payload)
      await client.invalidateQueries({ queryKey: ['categories'] })
      await client.invalidateQueries({ queryKey: ['category-inventory'] })
      reset(); feedback.success(editing ? 'Category updated.' : 'Category created.')
    } catch (failure) { setError(failure.message); feedback.error(failure.message) } finally { setBusy(false) }
  }
  const toggle = async category => {
    setBusy(true); setError('')
    try {
      await api.updateCategory(category.id, { name: category.name, active: !category.active })
      await client.invalidateQueries({ queryKey: ['categories'] })
      if (editing?.id === category.id) reset()
      feedback.success(category.active ? 'Category archived. Existing product assignments are preserved.' : 'Category activated.')
    } catch (failure) { setError(failure.message); feedback.error(failure.message) } finally { setBusy(false) }
  }
  const rows = (categories.data || []).filter(category => category.name.toLowerCase().includes(search.trim().toLowerCase()))
  return <CrudModal title="Manage categories" description="Organize your catalogue. Archived categories retain existing products and inventory history." onClose={onClose} onSubmit={save} submitLabel={editing ? 'Save category' : 'Add category'} busy={busy} error={error}>
    <div className="category-manager"><label className="crud-field"><span>{editing ? 'Rename category' : 'New category'}</span><input required maxLength={100} value={name} onChange={event => setName(event.target.value)} placeholder="e.g. Beverages" disabled={busy}/></label>{editing && <button type="button" className="secondary-btn" disabled={busy} onClick={reset}>Cancel editing</button>}
      <label className="crud-field"><span>Search categories</span><input value={search} onChange={event => setSearch(event.target.value)} placeholder="Find a category…"/></label>
      {categories.isPending ? <p role="status">Loading categories…</p> : categories.isError ? <div role="alert"><p>{categories.error.message}</p><button type="button" onClick={() => categories.refetch()}>Try again</button></div> : !rows.length ? <p>No categories found. Add your first category above.</p> : <div className="table-responsive"><PaginatedTable className="table"><thead><tr><th>Name</th><th>Products</th><th>Status</th><th>Actions</th></tr></thead><tbody>{rows.map(category => <tr key={category.id}><td>{category.name}</td><td>{category.productCount}</td><td>{category.active ? 'Active' : 'Archived'}</td><td><div className="row-actions"><button type="button" className="row-action" disabled={busy} onClick={() => { setEditing(category); setName(category.name); setError('') }}>Rename</button><button type="button" className="row-action" disabled={busy} onClick={() => toggle(category)}>{category.active ? 'Archive' : 'Activate'}</button></div></td></tr>)}</tbody></PaginatedTable></div>}
    </div>
  </CrudModal>
}

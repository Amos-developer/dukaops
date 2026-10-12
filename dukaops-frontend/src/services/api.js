const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

function authHeaders() {
  const username = import.meta.env.VITE_API_USERNAME
  const password = import.meta.env.VITE_API_PASSWORD
  if (!username || !password) return {}
  return { Authorization: `Basic ${btoa(`${username}:${password}`)}` }
}

export async function apiFetch(path) {
  const response = await fetch(`${API_URL}${path}`, { headers: { ...authHeaders() } })
  if (!response.ok) throw new Error(`API request failed (${response.status})`)
  return response.json()
}

export const api = {
  currentUser: () => apiFetch('/auth/me'),
  products: () => apiFetch('/products'),
  branches: (tenantId) => apiFetch(`/tenants/${tenantId}/branches`),
  stock: (branchId) => apiFetch(`/inventory/branches/${branchId}/stock`),
  branchMovements: (branchId) => apiFetch(`/inventory/branches/${branchId}/movements`),
}

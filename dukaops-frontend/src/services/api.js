const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000/api'

function authHeaders() {
  const username = import.meta.env.VITE_API_USERNAME
  const password = import.meta.env.VITE_API_PASSWORD
  if (!username || !password) return {}
  return { Authorization: `Basic ${btoa(`${username}:${password}`)}` }
}

export async function apiFetch(path) {
  let response
  try {
    response = await fetch(`${API_URL}${path}`, { headers: { ...authHeaders() } })
  } catch {
    throw new Error(`Backend is unreachable at ${API_URL}. Start Spring Boot and verify VITE_API_URL.`)
  }
  if (response.status === 401) throw new Error('Backend authentication failed. Set VITE_API_USERNAME and VITE_API_PASSWORD.')
  if (response.status === 403) throw new Error('Backend rejected the request. Check CORS_ALLOWED_ORIGINS and user permissions.')
  if (!response.ok) throw new Error(`API request failed for ${path} (${response.status})`)
  return response.json()
}

export const api = {
  currentUser: () => apiFetch('/auth/me'),
  products: () => apiFetch('/products'),
  branches: (tenantId) => apiFetch(`/tenants/${tenantId}/branches`),
  stock: (branchId) => apiFetch(`/inventory/branches/${branchId}/stock`),
  branchMovements: (branchId) => apiFetch(`/inventory/branches/${branchId}/movements`),
}

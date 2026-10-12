const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000/api'
const credentialKey = 'dukaops.credentials'

export function getStoredCredentials() {
  try { return JSON.parse(localStorage.getItem(credentialKey) || 'null') } catch { return null }
}

export function saveCredentials(email, password) {
  localStorage.setItem(credentialKey, JSON.stringify({ email, password }))
}

export function clearCredentials() {
  localStorage.removeItem(credentialKey)
}

function authHeaders() {
  const stored = getStoredCredentials()
  const username = stored?.email || import.meta.env.VITE_API_USERNAME
  const password = stored?.password || import.meta.env.VITE_API_PASSWORD
  if (!username || !password) return {}
  return { Authorization: `Basic ${btoa(`${username}:${password}`)}` }
}

export async function registerBusiness(payload) {
  const response = await fetch(`${API_URL}/auth/register`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
  if (!response.ok) throw new Error((await response.text()) || `Registration failed (${response.status})`)
  return response.json()
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

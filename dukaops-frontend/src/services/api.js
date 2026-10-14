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
  if (response.status === 409) throw new Error('That business or email is already registered. Try a different value.')
  if (response.status === 400) throw new Error('Please review the information and try again.')
  if (!response.ok) throw new Error('We could not create your workspace right now. Please try again shortly.')
  return response.json()
}

export async function authenticate(email, password) {
  let response
  try {
    response = await fetch(`${API_URL}/auth/me`, {
      headers: { Authorization: `Basic ${btoa(`${email}:${password}`)}` },
    })
  } catch {
    throw new Error('We could not connect to DukaOps right now. Please try again shortly.')
  }
  if (response.status === 401) throw new Error('We could not sign you in. Please check your email and password.')
  if (response.status === 403) throw new Error('You do not have permission to access this workspace.')
  if (!response.ok) throw new Error('Something went wrong while signing you in. Please try again.')
  return response.json()
}

export async function apiFetch(path) {
  let response
  try {
    response = await fetch(`${API_URL}${path}`, { headers: { ...authHeaders() } })
  } catch {
    throw new Error('We could not connect to DukaOps right now. Please try again shortly.')
  }
  if (response.status === 401) throw new Error('We could not sign you in. Please check your email and password.')
  if (response.status === 403) throw new Error('You do not have permission to complete this action.')
  if (response.status >= 500) throw new Error('DukaOps is temporarily unavailable. Please try again shortly.')
  if ((response.status === 404 || response.status === 405) && path.startsWith('/team/invitations')) {
    throw new Error('Invitation listing is unavailable on the running server. Please restart the backend with the latest application version.')
  }
  if (!response.ok) throw new Error('Something went wrong while processing your request.')
  return response.json()
}

async function apiRequest(path, method, payload) {
  let response
  try {
    response = await fetch(`${API_URL}${path}`, { method, headers: { 'Content-Type': 'application/json', ...authHeaders() }, body: payload === undefined ? undefined : JSON.stringify(payload) })
  } catch {
    throw new Error('We could not connect to DukaOps right now. Please try again shortly.')
  }
  if (response.status === 401) throw new Error('We could not sign you in. Please check your email and password.')
  if (response.status === 403) throw new Error('You do not have permission to complete this action.')
  if (response.status === 400) throw new Error('Please review the form details and try again.')
  if (response.status === 409) throw new Error('This record already exists. Please use different details.')
  if (!response.ok) throw new Error('Something went wrong while saving your changes.')
  const responseBody = await response.text()
  return responseBody.trim() ? JSON.parse(responseBody) : null
}

export const api = {
  settings: (section) => apiFetch(`/settings/${section}`),
  saveSettings: (section, payload) => apiRequest(`/settings/${section}`, 'PUT', payload),
  currentUser: () => apiFetch('/auth/me'),
  createTeamUser: (payload) => apiRequest('/team/users', 'POST', payload),
  teamInvitations: () => apiFetch('/team/invitations'),
  products: () => apiFetch('/products'),
  branches: (tenantId) => apiFetch(`/tenants/${tenantId}/branches`),
  stock: (branchId) => apiFetch(`/inventory/branches/${branchId}/stock`),
  branchMovements: (branchId) => apiFetch(`/inventory/branches/${branchId}/movements`),
  createProduct: (payload) => apiRequest('/products', 'POST', payload),
  updateProduct: (productId, payload) => apiRequest(`/products/${productId}`, 'PUT', payload),
  changeProductStatus: (productId, status) => apiRequest(`/products/${productId}/status`, 'PATCH', { status }),
  createBranch: (tenantId, payload) => apiRequest(`/tenants/${tenantId}/branches`, 'POST', payload),
  receiveStock: (payload) => apiRequest('/inventory/receipts', 'POST', payload),
  adjustStock: (payload) => apiRequest('/inventory/adjustments', 'POST', payload),
  teamUsers: ({ search = '', role = '', status = '', page = 0, size = 100 } = {}) => {
    const params = new URLSearchParams({ page: String(page), size: String(size) })
    if (search) params.set('search', search)
    if (role) params.set('role', role)
    if (status) params.set('status', status)
    return apiFetch(`/team/users?${params.toString()}`)
  },
  createTeamInvitation: (payload) => apiRequest('/team/invitations', 'POST', payload),
  updateTeamUserRole: (userId, role) => apiRequest(`/team/users/${userId}/role`, 'PATCH', { role }),
  updateTeamUserBranches: (userId, branchIds) => apiRequest(`/team/users/${userId}/branches`, 'PUT', { branchIds }),
  updateTeamUserStatus: (userId, status) => apiRequest(`/team/users/${userId}/status`, 'PATCH', { status }),
  salesSummary: () => apiFetch('/sales/summary'),
  salesOrders: ({ search = '', status = '', page = 0, size = 20 } = {}) => { const params = new URLSearchParams({ page: String(page), size: String(size) }); if (search) params.set('search', search); if (status) params.set('status', status); return apiFetch(`/sales/orders?${params.toString()}`) },
  salesOrder: (id) => apiFetch(`/sales/orders/${id}`),
  checkout: (payload) => apiRequest('/sales/checkout', 'POST', payload),
  updateOrderStatus: (id, status) => apiRequest(`/sales/orders/${id}/status`, 'PATCH', { status }),
  contacts: (type, { search = '', status = '', page = 0, size = 50 } = {}) => { const params = new URLSearchParams({ page: String(page), size: String(size) }); if (search) params.set('search', search); if (status) params.set('status', status); return apiFetch(`/contacts/${type}?${params.toString()}`) },
  createContact: (type, payload) => apiRequest(`/contacts/${type}`, 'POST', payload),
  updateContact: (type, id, payload) => apiRequest(`/contacts/${type}/${id}`, 'PUT', payload),
  updateContactStatus: (type, id, status) => apiRequest(`/contacts/${type}/${id}/status?status=${status}`, 'PATCH'),
  report: (type, params = {}) => { const query = new URLSearchParams(); Object.entries(params).forEach(([key, value]) => { if (value) query.set(key, value) }); return apiFetch(`/reports/${type}${query.toString() ? `?${query}` : ''}`) },
  exportReport: (type = 'sales', branchId) => apiFetch(`/reports/export?type=${encodeURIComponent(type)}${branchId ? `&branchId=${branchId}` : ''}`),
}

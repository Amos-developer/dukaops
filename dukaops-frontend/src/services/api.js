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
  if (!response.ok) throw new Error('Something went wrong while processing your request.')
  return response.json()
}

export const api = {
  currentUser: () => apiFetch('/auth/me'),
  products: () => apiFetch('/products'),
  branches: (tenantId) => apiFetch(`/tenants/${tenantId}/branches`),
  stock: (branchId) => apiFetch(`/inventory/branches/${branchId}/stock`),
  branchMovements: (branchId) => apiFetch(`/inventory/branches/${branchId}/movements`),
}

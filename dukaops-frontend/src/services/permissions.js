export const businessAdmin = role => ['OWNER', 'ADMIN'].includes(role)
export const inventoryManager = role => ['OWNER', 'ADMIN', 'MANAGER'].includes(role)
export function canVisit(role, path) {
  if (path === '/team' || path === '/customers') return businessAdmin(role)
  if (['/reports', '/branches', '/movements', '/products', '/dashboard'].some(route => path === route || path.startsWith(`${route}/`))) return inventoryManager(role)
  return Boolean(role)
}

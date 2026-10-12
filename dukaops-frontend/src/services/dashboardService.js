import { api } from './api'

export const demoDashboard = {
  user: { email: 'admin@dukaops.com', role: 'ADMIN' },
  branches: [{ id: 'demo-main', name: 'Main Branch', isMain: true }],
  products: [
    { id: '1', name: 'Premium Coffee Beans', sku: 'COF-001', unitPrice: 1250, status: 'ACTIVE' },
    { id: '2', name: 'Organic Green Tea', sku: 'TEA-014', unitPrice: 860, status: 'ACTIVE' },
    { id: '3', name: 'Cane Sugar 1kg', sku: 'SUG-003', unitPrice: 190, status: 'ACTIVE' },
    { id: '4', name: 'Wheat Flour 2kg', sku: 'FLR-008', unitPrice: 280, status: 'ACTIVE' },
  ],
  stock: [
    { productId: '1', productName: 'Premium Coffee Beans', quantity: 78, reorderLevel: 30, lowStock: false },
    { productId: '2', productName: 'Organic Green Tea', quantity: 24, reorderLevel: 30, lowStock: true },
    { productId: '3', productName: 'Cane Sugar 1kg', quantity: 64, reorderLevel: 25, lowStock: false },
    { productId: '4', productName: 'Wheat Flour 2kg', quantity: 18, reorderLevel: 20, lowStock: true },
  ],
  isDemo: true,
}

export async function getDashboardData() {
  try {
    const user = await api.currentUser()
    const [products, branches] = await Promise.all([api.products(), api.branches(user.tenantId)])
    const branch = branches.find((item) => item.isMain) || branches[0]
    const stock = branch ? await api.stock(branch.id) : []
    return { user, products, branches, stock, isDemo: false }
  } catch {
    return demoDashboard
  }
}

export async function getProductsPageData() {
  try {
    return { products: await api.products(), isDemo: false }
  } catch {
    return { products: demoDashboard.products, isDemo: true }
  }
}

export async function getInventoryPageData() {
  try {
    const user = await api.currentUser()
    const branches = await api.branches(user.tenantId)
    const branch = branches.find((item) => item.isMain) || branches[0]
    const stock = branch ? await api.stock(branch.id) : []
    return { branches, stock, selectedBranch: branch, isDemo: false }
  } catch {
    return { branches: demoDashboard.branches, stock: demoDashboard.stock, selectedBranch: demoDashboard.branches[0], isDemo: true }
  }
}

export async function getBranchesPageData() {
  try {
    const user = await api.currentUser()
    return { branches: await api.branches(user.tenantId), isDemo: false }
  } catch {
    return { branches: demoDashboard.branches, isDemo: true }
  }
}

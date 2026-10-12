import { api } from './api'

export async function getDashboardData() {
  const user = await api.currentUser()
  const [products, branches] = await Promise.all([api.products(), api.branches(user.tenantId)])
  const branchData = await Promise.all(branches.map(async (branch) => ({
    branch,
    stock: await api.stock(branch.id),
    movements: await api.branchMovements(branch.id),
  })))
  return {
    user,
    products,
    branches,
    stock: branchData.flatMap((item) => item.stock),
    movements: branchData.flatMap((item) => item.movements),
    stockByBranch: branchData.map((item) => ({ ...item.branch, stock: item.stock })),
    isDemo: false,
  }
}

export async function getProductsPageData() {
  return { products: await api.products(), isDemo: false }
}

export async function getInventoryPageData() {
  const user = await api.currentUser()
  const branches = await api.branches(user.tenantId)
  const branch = branches.find((item) => item.isMain) || branches[0]
  const stock = branch ? await api.stock(branch.id) : []
  const movements = branch ? await api.branchMovements(branch.id) : []
  return { branches, stock, movements, selectedBranch: branch, isDemo: false }
}

export async function getBranchesPageData() {
  const user = await api.currentUser()
  return { branches: await api.branches(user.tenantId), isDemo: false }
}

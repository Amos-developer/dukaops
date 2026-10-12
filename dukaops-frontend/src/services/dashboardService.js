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
  const [branches, products] = await Promise.all([api.branches(user.tenantId), api.products()])
  const branch = branches.find((item) => item.isMain) || branches[0]
  const stock = branch ? await api.stock(branch.id) : []
  const movements = branch ? await api.branchMovements(branch.id) : []
  return { branches, products, stock, movements, selectedBranch: branch, isDemo: false }
}

export async function getBranchesPageData() {
  const user = await api.currentUser()
  return { branches: await api.branches(user.tenantId), isDemo: false }
}

export async function getStockMovementsPageData() {
  const user = await api.currentUser()
  const [branches, products] = await Promise.all([api.branches(user.tenantId), api.products()])
  const movementsByBranch = await Promise.all(branches.map(async (branch) => ({
    branch,
    movements: await api.branchMovements(branch.id),
  })))
  const productMap = new Map(products.map((product) => [product.id, product]))
  return {
    branches,
    products,
    movements: movementsByBranch.flatMap(({ branch, movements }) => movements.map((movement) => ({
      ...movement,
      branchName: branch.name,
      productName: productMap.get(movement.productId)?.name || 'Product',
      sku: productMap.get(movement.productId)?.sku || '—',
    }))),
    isDemo: false,
  }
}

export async function getTeamPageData() {
  const user = await api.currentUser()
  const branches = await api.branches(user.tenantId)
  return { user, branches, isDemo: false }
}

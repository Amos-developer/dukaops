import { useQuery } from '@tanstack/react-query'
import { getBranchesPageData, getInventoryPageData, getProductsPageData } from '../services/dashboardService'

export const useProducts = () => useQuery({ queryKey: ['products'], queryFn: getProductsPageData })
export const useInventory = () => useQuery({ queryKey: ['inventory'], queryFn: getInventoryPageData })
export const useBranches = () => useQuery({ queryKey: ['branches'], queryFn: getBranchesPageData })

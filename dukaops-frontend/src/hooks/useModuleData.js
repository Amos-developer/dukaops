import { useQuery } from '@tanstack/react-query'
import { getBranchesPageData, getInventoryPageData, getProductsPageData, getStockMovementsPageData, getTeamPageData } from '../services/dashboardService'

export const useProducts = () => useQuery({ queryKey: ['products'], queryFn: getProductsPageData })
export const useInventory = () => useQuery({ queryKey: ['inventory'], queryFn: getInventoryPageData })
export const useBranches = () => useQuery({ queryKey: ['branches'], queryFn: getBranchesPageData })
export const useStockMovements = () => useQuery({ queryKey: ['stock-movements'], queryFn: getStockMovementsPageData })
export const useTeam = () => useQuery({ queryKey: ['team'], queryFn: getTeamPageData })

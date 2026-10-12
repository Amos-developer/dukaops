import { Navigate, Route, Routes } from 'react-router-dom'
import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import DashboardPage from './views/DashboardPage'
import SectionPage from './views/SectionPage'
import ProductsPage from './views/ProductsPage'
import InventoryPage from './views/InventoryPage'
import BranchesPage from './views/BranchesPage'
import LandingPage from './views/LandingPage'
import LoginPage from './views/LoginPage'
import AuthPage from './views/AuthPage'
import AppShell from './components/layout/AppShell'
import { api, clearCredentials, getStoredCredentials } from './services/api'

function ProtectedApp() {
  const hasCredentials = Boolean(getStoredCredentials() || (import.meta.env.VITE_API_USERNAME && import.meta.env.VITE_API_PASSWORD))
  const session = useQuery({ queryKey: ['session'], queryFn: api.currentUser, enabled: hasCredentials, retry: false, staleTime: 60_000 })
  useEffect(() => { if (session.isError) clearCredentials() }, [session.isError])
  if (!hasCredentials) return <Navigate to="/login" replace />
  if (session.isLoading) return <div className="loading-state"><div className="spinner-border text-primary" role="status" /><p>Verifying your session...</p></div>
  if (session.isError) return <Navigate to="/login" replace />
  return <AppShell><Routes><Route path="/dashboard" element={<DashboardPage />} /><Route path="/products" element={<ProductsPage />} /><Route path="/inventory" element={<InventoryPage />} /><Route path="/movements" element={<SectionPage title="Stock Movements" description="Review receipts, adjustments, transfers, and stock history." icon="inventory" />} /><Route path="/branches" element={<BranchesPage />} /><Route path="/team" element={<SectionPage title="Team & Users" description="Invite team members and manage their roles and access." icon="team" />} /><Route path="/sales" element={<SectionPage title="Sales & Orders" description="Sales workflows are planned for a later release." icon="sales" later />} /><Route path="/customers" element={<SectionPage title="Customers & Suppliers" description="Customer and supplier management is planned for a later release." icon="customers" later />} /><Route path="/reports" element={<SectionPage title="Reports" description="Advanced reporting and analytics are planned for a later release." icon="reports" later />} /><Route path="/settings" element={<SectionPage title="Settings" description="Configure your workspace preferences and integrations." icon="settings" />} /><Route path="*" element={<Navigate to="/dashboard" replace />} /></Routes></AppShell>
}

export default function App() {
  return <Routes><Route path="/" element={<LandingPage />} /><Route path="/login" element={<LoginPage />} /><Route path="/register" element={<AuthPage />} /><Route path="/*" element={<ProtectedApp />} /></Routes>
}

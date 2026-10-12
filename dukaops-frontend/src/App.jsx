import { Navigate, Route, Routes } from 'react-router-dom'
import DashboardPage from './views/DashboardPage'
import SectionPage from './views/SectionPage'
import ProductsPage from './views/ProductsPage'
import InventoryPage from './views/InventoryPage'
import BranchesPage from './views/BranchesPage'
import AppShell from './components/layout/AppShell'

export default function App() {
  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/inventory" element={<InventoryPage />} />
        <Route path="/movements" element={<SectionPage title="Stock Movements" description="Review receipts, adjustments, transfers, and stock history." icon="inventory" />} />
        <Route path="/branches" element={<BranchesPage />} />
        <Route path="/team" element={<SectionPage title="Team & Users" description="Invite team members and manage their roles and access." icon="team" />} />
        <Route path="/sales" element={<SectionPage title="Sales & Orders" description="Sales workflows are planned for a later release." icon="sales" later />} />
        <Route path="/customers" element={<SectionPage title="Customers & Suppliers" description="Customer and supplier management is planned for a later release." icon="customers" later />} />
        <Route path="/reports" element={<SectionPage title="Reports" description="Advanced reporting and analytics are planned for a later release." icon="reports" later />} />
        <Route path="/settings" element={<SectionPage title="Settings" description="Configure your workspace preferences and integrations." icon="settings" />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AppShell>
  )
}

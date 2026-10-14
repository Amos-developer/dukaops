import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useIsFetching } from '@tanstack/react-query'
import { clearCredentials } from '../../services/api'
import { Bell, Boxes, Building2, ChevronDown, ChevronRight, FileBarChart2, HelpCircle, LayoutDashboard, LogOut, Menu, Package, PanelLeftClose, PanelLeftOpen, Search, Settings, ShoppingCart, Store, Users, X } from 'lucide-react'

const navGroups = [
  { label: 'Overview', items: [{ label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard }] },
  {
    label: 'MVP',
    items: [
      { label: 'Products', path: '/products', icon: Package },
      { label: 'Inventory', path: '/inventory', icon: Boxes },
      { label: 'Stock Movements', path: '/movements', icon: FileBarChart2 },
      { label: 'Branches', path: '/branches', icon: Store },
      { label: 'Team & Users', path: '/team', icon: Users },
    ],
  },
  {
    label: 'Later',
    items: [
      { label: 'Sales & Orders', path: '/sales', icon: ShoppingCart },
      { label: 'Customers & Suppliers', path: '/customers', icon: Users },
      { label: 'Reports', path: '/reports', icon: FileBarChart2 },
    ],
  },
]

export default function AppShell({ children }) {
  const [collapsed, setCollapsed] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const isFetching = useIsFetching() > 0
  const logout = () => { clearCredentials(); window.location.href = '/auth' }
  return (
    <div className="app-shell">
      <aside className={`sidebar ${collapsed ? 'sidebar-collapsed' : ''} ${mobileOpen ? 'sidebar-mobile-open' : ''}`}>
        <div className="brand-row"><div className="brand-mark">D</div><span className="brand-name">Duka<span>Ops</span></span><button className="icon-btn sidebar-close" onClick={() => setMobileOpen(false)} aria-label="Close menu"><X size={18} /></button></div>
        <nav className="main-nav">
          {navGroups.map((group) => <div className="nav-group" key={group.label}>
            <div className="workspace-label"><span>{group.label}</span>{group.label !== 'Overview' && <small>{group.label === 'MVP' ? 'CORE' : 'SOON'}</small>}</div>
            {group.items.map(({ label, path, icon: Icon }) => <NavLink to={path} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`} key={label} onClick={() => setMobileOpen(false)}><Icon size={18} /><span>{label}</span>{group.label !== 'Overview' && <ChevronRight className="nav-chevron" size={15} />}</NavLink>)}
          </div>)}
        </nav>
        <div className="sidebar-bottom">
          <div className="workspace-label"><span>Manage</span></div>
          <NavLink to="/settings" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}><Settings size={18} /><span>Settings</span><ChevronRight className="nav-chevron" size={15} /></NavLink>
          <NavLink to="/help" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`} onClick={() => setMobileOpen(false)}><HelpCircle size={18} /><span>Help center</span></NavLink>
          <button className="nav-item logout" onClick={logout}><LogOut size={18} /><span>Log out</span></button>
        </div>
        <button className="collapse-btn" onClick={() => setCollapsed((value) => !value)}>{collapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}<span>{collapsed ? 'Expand' : 'Collapse'}</span></button>
      </aside>
      {mobileOpen && <button className="sidebar-backdrop" onClick={() => setMobileOpen(false)} aria-label="Close navigation" />}
      <main className={`main-content ${collapsed ? 'main-expanded' : ''}`}>
        <header className="topbar">
          <div className={`topbar-progress ${isFetching ? 'visible' : ''}`} aria-hidden="true" />
          <button className="icon-btn mobile-menu" onClick={() => setMobileOpen(true)} aria-label="Open menu"><Menu size={21} /></button>
          <div className="business-context"><div className="business-logo">D</div><div><strong>DukaOps Demo</strong><span>Business workspace</span></div></div>
          <div className="branch-selector"><Building2 size={15} /><span>Main branch</span><ChevronDown size={14} /></div>
          <div className="search-box"><Search size={17} /><input placeholder="Search products, reports..." aria-label="Search" /></div>
          <div className="topbar-actions"><button className="icon-btn"><Bell size={19} /><span className="notification-dot" /></button><div className="profile"><div className="avatar">AM</div><div className="profile-copy"><strong>Amos M.</strong><small>Administrator</small></div><ChevronDown size={15} /></div><button className="logout-top" onClick={logout}><LogOut size={15} /><span>Log out</span></button></div>
        </header>
        <div className="page-content">{children || <Outlet />}</div>
      </main>
    </div>
  )
}

import { Boxes, CircleDollarSign, FileBarChart2, Package, Settings, Store, Users } from 'lucide-react'

const icons = { products: Package, inventory: Boxes, branches: Store, team: Users, sales: CircleDollarSign, customers: Users, reports: FileBarChart2, settings: Settings }

export default function SectionPage({ title, description, icon, later }) {
  const Icon = icons[icon] || Package
  return <section className="section-page"><div className="section-page-icon"><Icon size={25} /></div><p className="eyebrow">{later ? 'COMING LATER' : 'MVP MODULE'}</p><h1>{title}</h1><p className="section-page-description">{description}</p><div className="section-page-card"><strong>{later ? 'Planned for a later release' : 'Module ready for implementation'}</strong><span>{later ? 'The navigation is ready so this area can be added without changing the shell.' : 'This page is connected to the application shell and ready for its feature workflow.'}</span></div></section>
}

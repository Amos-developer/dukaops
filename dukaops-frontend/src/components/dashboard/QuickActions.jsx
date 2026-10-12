import { ArrowDownToLine, Building2, ClipboardPlus, PackagePlus } from 'lucide-react'

export default function QuickActions() {
  const actions = [['Add product', PackagePlus], ['Receive stock', ArrowDownToLine], ['Adjust stock', ClipboardPlus], ['Add branch', Building2]]
  return <section className="quick-actions"><div><p className="eyebrow">QUICK ACTIONS</p><h2>Keep operations moving</h2></div><div className="quick-action-grid">{actions.map(([label, Icon]) => <button key={label}><span><Icon size={17} /></span>{label}</button>)}</div></section>
}

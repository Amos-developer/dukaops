export default function SectionHeader({ eyebrow, title, action }) {
  return <div className="section-header"><div><p className="eyebrow">{eyebrow}</p><h2>{title}</h2></div>{action}</div>
}

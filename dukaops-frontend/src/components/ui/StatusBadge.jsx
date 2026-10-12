export default function StatusBadge({ children, tone = 'success' }) {
  return <span className={`status-badge ${tone}`}>{children}</span>
}

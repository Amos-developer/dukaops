import PaginatedTable from '../ui/PaginatedTable'
import { ArrowUpRight, CheckCircle2, Clock3, Filter, Plus } from 'lucide-react'
import SectionHeader from '../ui/SectionHeader'

const transactions = [
  ['Lydia Wanjiku', 'Stock received', 'Today, 09:42', '+ 48 units', 'completed'],
  ['Brian Otieno', 'Stock adjustment', 'Yesterday, 16:18', '+ 12 units', 'completed'],
  ['Mercy Njeri', 'Low stock alert', 'Yesterday, 11:06', 'Needs review', 'pending'],
  ['Kevin Mwangi', 'Stock received', '04 Jun, 14:31', '+ 36 units', 'completed'],
]
export default function TransactionsTable() {
  return <section className="dashboard-card transactions-card"><SectionHeader eyebrow="ACTIVITY" title="Recent transactions" action={<div className="table-actions"><button className="secondary-btn"><Filter size={15} /> Filter</button><button className="primary-btn"><Plus size={15} /> New transaction</button></div>} /><div className="table-responsive"><PaginatedTable className="table align-middle"><thead><tr><th>Activity</th><th>Date</th><th>Status</th><th className="text-end">Quantity</th></tr></thead><tbody>{transactions.map(([name, action, date, quantity, status]) => <tr key={`${name}-${date}`}><td><div className="transaction-person"><div className="person-avatar">{name.split(' ').map((word) => word[0]).join('')}</div><div><strong>{name}</strong><span>{action}</span></div></div></td><td>{date}</td><td><span className={`status-pill ${status}`}>{status === 'completed' ? <CheckCircle2 size={13} /> : <Clock3 size={13} />}{status === 'completed' ? 'Completed' : 'Pending'}</span></td><td className={`text-end quantity ${status === 'pending' ? 'muted' : ''}`}>{quantity} {status !== 'pending' && <ArrowUpRight size={14} />}</td></tr>)}</tbody></PaginatedTable></div><button className="view-all">View all activity <ArrowUpRight size={15} /></button></section>
}

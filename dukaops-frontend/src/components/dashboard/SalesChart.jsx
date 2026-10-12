import { useState } from 'react'
import { ChevronDown, MoreHorizontal } from 'lucide-react'
import SectionHeader from '../ui/SectionHeader'

const values = [32, 44, 37, 51, 43, 58, 49, 46, 76, 29, 68, 48]
const labels = ['2 am', '4 am', '6 am', '8 am', '10 am', '12 pm', '2 pm', '4 pm', '6 pm', '8 pm', '10 pm', '12 am']
export default function SalesChart() {
  const [period, setPeriod] = useState('This year')
  return <section className="dashboard-card chart-card"><SectionHeader eyebrow="PERFORMANCE" title="Sales & purchase" action={<div className="chart-actions"><span className="legend"><i className="purchase" /> Purchase</span><span className="legend"><i className="sales" /> Sales</span><select value={period} onChange={(event) => setPeriod(event.target.value)}><option>This year</option><option>This month</option><option>This week</option></select><button className="icon-btn"><MoreHorizontal size={18} /></button></div>} /><div className="chart-wrap"><div className="y-axis"><span>80K</span><span>60K</span><span>40K</span><span>20K</span><span>0K</span></div><div className="bars">{values.map((value, index) => <div className="bar-col" key={labels[index]}><div className="bar" style={{ height: `${value}%` }}><i /><b /></div><span>{labels[index]}</span></div>)}</div></div></section>
}

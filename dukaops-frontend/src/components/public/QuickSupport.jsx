import { useEffect, useId, useRef, useState } from 'react'
import { ArrowUpRight, Headphones, Phone, X } from 'lucide-react'
import '../../styles/quick-support.css'

function WhatsAppIcon() {
  return <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M20.5 11.5a8.5 8.5 0 0 1-12.6 7.4L3 20.5l1.6-4.8A8.5 8.5 0 1 1 20.5 11.5Z"/><path d="m8.3 7.6 1.2 2.1-.9 1.1c.7 1.5 1.9 2.6 3.4 3.3l1.1-.9 2.1 1.2c-.2 1.3-1 1.8-2.1 1.6-3.3-.6-6-3.3-6.5-6.5-.2-1 .4-1.7 1.7-1.9Z"/></svg>
}

export default function QuickSupport() {
  const [open, setOpen] = useState(false)
  const panelId = useId()
  const root = useRef(null)
  const launcher = useRef(null)
  const closeButton = useRef(null)
  const close = () => { setOpen(false); launcher.current?.focus() }

  useEffect(() => {
    if (!open) return
    closeButton.current?.focus()
    const onKeyDown = event => {
      if (event.key === 'Escape') { setOpen(false); launcher.current?.focus() }
    }
    const onPointerDown = event => {
      if (!root.current?.contains(event.target)) setOpen(false)
    }
    document.addEventListener('keydown', onKeyDown)
    document.addEventListener('pointerdown', onPointerDown)
    return () => {
      document.removeEventListener('keydown', onKeyDown)
      document.removeEventListener('pointerdown', onPointerDown)
    }
  }, [open])

  return <aside className="quick-support" ref={root} aria-label="DukaOps support">
    {open && <section className="quick-support-panel" id={panelId} aria-labelledby={`${panelId}-title`}>
      <header className="quick-support-heading"><div className="quick-support-heading-icon"><Headphones size={23}/></div><div><span>WE’RE HERE TO HELP</span><h2 id={`${panelId}-title`}>Quick support</h2></div><button type="button" ref={closeButton} className="quick-support-close" onClick={close} aria-label="Close support"><X size={19}/></button></header>
      <div className="quick-support-body"><p>Need a hand with DukaOps?</p><span className="quick-support-description">Ask a question, explore our features, or get help with your workspace.</span>
        <a className="quick-support-call" href="tel:+255710759213"><span className="quick-support-call-icon"><Phone size={20}/></span><span><small>Call now</small><strong>0710 759 213</strong></span><ArrowUpRight size={18} aria-hidden="true"/></a>
        <a className="quick-support-whatsapp" href="https://wa.me/255676796515?text=Hello%20DukaOps%2C%20I%20need%20some%20help." target="_blank" rel="noopener noreferrer"><WhatsAppIcon/><span>Chat on WhatsApp<small>0676 796 515 · opens WhatsApp</small></span><ArrowUpRight size={18} aria-hidden="true"/></a>
      </div><footer className="quick-support-footer">A conversation away. No account needed.</footer>
    </section>}
    <button type="button" className={`quick-support-launcher${open ? ' is-open' : ''}`} ref={launcher} aria-expanded={open} aria-controls={open ? panelId : undefined} aria-label={open ? 'Close quick support' : 'Open quick support'} onClick={() => setOpen(current => !current)}><span className="quick-support-launcher-icon">{open ? <X size={24}/> : <Headphones size={25}/>}</span><span><strong>Need help?</strong><small>Let’s talk</small></span>{!open && <span className="quick-support-wave" aria-hidden="true">👋</span>}</button>
  </aside>
}

import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import { CheckCircle2, X, XCircle } from 'lucide-react'

const FeedbackContext = createContext(null)

export function FeedbackProvider({ children }) {
  const [toast, setToast] = useState(null)
  const notify = useCallback((type, message) => {
    setToast({ type, message })
    window.setTimeout(() => setToast((current) => current?.message === message ? null : current), 4500)
  }, [])
  const value = useMemo(() => ({ success: (message) => notify('success', message), error: (message) => notify('error', message) }), [notify])
  return <FeedbackContext.Provider value={value}>{children}{toast && <div className={`feedback-toast ${toast.type}`} role="status"><span>{toast.type === 'success' ? <CheckCircle2 size={17} /> : <XCircle size={17} />}</span><p>{toast.message}</p><button onClick={() => setToast(null)} aria-label="Dismiss notification"><X size={15} /></button></div>}</FeedbackContext.Provider>
}

export function useFeedback() {
  return useContext(FeedbackContext)
}

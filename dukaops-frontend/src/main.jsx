import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import 'bootstrap/dist/css/bootstrap.min.css'
import './styles/index.css'
import './styles/layout.css'
import './styles/dashboard.css'
import './styles/section-page.css'
import './styles/modules.css'
import './styles/status.css'
import './styles/auth.css'
import './styles/public.css'
import './styles/login.css'
import './styles/landing.css'
import './styles/product-page.css'
import './styles/inventory-page.css'
import './styles/inventory-overrides.css'
import './styles/movement-page.css'
import './styles/page-heading-overrides.css'
import './styles/crud.css'
import './styles/branch-page.css'
import './styles/team-page.css'
import './styles/sales-page.css'
import './styles/theme.css'
import './styles/feedback.css'
import App from './App'
import { FeedbackProvider } from './components/ui/FeedbackProvider'

const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 30_000, retry: 1 } },
})

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <FeedbackProvider><App /></FeedbackProvider>
      </BrowserRouter>
    </QueryClientProvider>
  </React.StrictMode>,
)

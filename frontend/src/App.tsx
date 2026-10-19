// Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors.
import { useCallback, useEffect, useMemo, useState } from 'react'
import { Activity, ArrowDown, ArrowUpRight, Check, ChevronDown, CircleHelp, Command, Layers3, Plus, Radio, RefreshCw, Search, Send, ShoppingBag, X } from 'lucide-react'

type Order = {
  id: string
  customerName: string
  item: string
  quantity: number
  unitPrice: number
  total: number
  createdAt: string
  status: string
}

type OrderForm = { customerName: string; item: string; quantity: number; unitPrice: number }
const emptyForm: OrderForm = { customerName: '', item: '', quantity: 1, unitPrice: 0 }
const money = (value: number) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value)

/** Provides the order dashboard and its API Gateway-backed create/list workflow. */
export default function App() {
  const [orders, setOrders] = useState<Order[]>([])
  const [form, setForm] = useState<OrderForm>(emptyForm)
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [modalOpen, setModalOpen] = useState(false)

  const refreshOrders = useCallback(async () => {
    setError('')
    try {
      const response = await fetch('/api/orders')
      if (!response.ok) throw new Error(`Could not load orders (${response.status})`)
      setOrders(await response.json() as Order[])
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not connect to the API Gateway')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { void refreshOrders() }, [refreshOrders])

  const visibleOrders = useMemo(() => {
    const term = search.trim().toLowerCase()
    return orders.filter(order => `${order.customerName} ${order.item} ${order.id}`.toLowerCase().includes(term))
  }, [orders, search])

  const totalRevenue = orders.reduce((total, order) => total + order.total, 0)

  async function submitOrder(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSending(true)
    setError('')
    try {
      const response = await fetch('/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })
      if (!response.ok) {
        const details = await response.json().catch(() => null) as { detail?: string; message?: string } | null
        throw new Error(details?.detail ?? details?.message ?? `Order request failed (${response.status})`)
      }
      setForm(emptyForm)
      setModalOpen(false)
      await refreshOrders()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not create order')
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#" aria-label="Relay home">
          <span className="brand-mark"><Radio size={19} strokeWidth={2.5} /></span>
          <span>relay<span className="brand-period">.</span></span>
          <span className="brand-tag">DEMO</span>
        </a>
        <div className="workspace-switch">
          <div className="workspace-icon">S</div>
          <div className="workspace-label"><span>Workspace</span><strong>Sample project</strong></div>
          <ChevronDown size={15} />
        </div>
        <div className="nav-caption">WORKSPACE</div>
        <nav className="navigation">
          <a className="nav-link active" href="#"><Layers3 size={17} /> Overview <span className="nav-indicator" /></a>
          <a className="nav-link" href="#orders"><ShoppingBag size={17} /> Orders <span className="nav-count">{orders.length}</span></a>
          <a className="nav-link" href="#architecture"><Activity size={17} /> Event flow</a>
        </nav>
        <div className="sidebar-bottom">
          <div className="status-card">
            <div className="status-heading"><span className="live-dot" /> SYSTEM STATUS</div>
            <div className="status-row"><span>API Gateway</span><span className="status-ok">Operational</span></div>
            <div className="status-row"><span>Kafka broker</span><span className="status-ok">Operational</span></div>
            <div className="status-row"><span>Order service</span><span className="status-ok">Operational</span></div>
          </div>
          <div className="sidebar-footer"><span className="avatar">JD</span><span className="profile-name">Jane Developer<small>Project admin</small></span><ChevronDown size={14} /></div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumbs"><span>Workspace</span><span className="crumb-divider">/</span><strong>Overview</strong></div>
          <div className="top-actions"><span className="environment"><i /> DEVELOPMENT</span><button className="icon-button" aria-label="Help"><CircleHelp size={17} /></button><span className="top-avatar">JD</span></div>
        </header>
        <div className="page-content">
          <section className="page-heading">
            <div><div className="eyebrow"><span className="eyebrow-line" /> ORDER OPERATIONS</div><h1>Good morning, Jane <span className="wave">✳</span></h1><p>Here’s what’s happening with your order pipeline today.</p></div>
            <button className="primary-button" onClick={() => { setError(''); setModalOpen(true) }}><Plus size={17} /> Create order</button>
          </section>

          {error && <div className="error-banner" role="alert"><span>{error}</span><button onClick={() => setError('')} aria-label="Dismiss error"><X size={16} /></button></div>}

          <section className="metrics-grid" aria-label="Order metrics">
            <article className="metric-card">
              <div className="metric-top"><span>Total orders</span><span className="metric-icon violet"><ShoppingBag size={17} /></span></div>
              <div className="metric-value">{orders.length.toString().padStart(2, '0')}</div>
              <div className="metric-foot"><span className="metric-neutral"><Activity size={12} /> Live count</span><span>all time</span></div>
              <div className="sparkline purple"><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /></div>
            </article>
            <article className="metric-card">
              <div className="metric-top"><span>Gross revenue</span><span className="metric-icon green"><ArrowUpRight size={17} /></span></div>
              <div className="metric-value">{money(totalRevenue)}</div>
              <div className="metric-foot"><span className="metric-neutral"><Activity size={12} /> Calculated</span><span>all time</span></div>
              <div className="sparkline mint"><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /></div>
            </article>
            <article className="metric-card">
              <div className="metric-top"><span>Average order</span><span className="metric-icon amber"><Activity size={17} /></span></div>
              <div className="metric-value">{money(orders.length ? totalRevenue / orders.length : 0)}</div>
              <div className="metric-foot"><span className="metric-neutral"><Activity size={12} /> Per order</span><span>all time</span></div>
              <div className="sparkline gold"><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /></div>
            </article>
          </section>

          <section className="orders-panel" id="orders">
            <div className="panel-heading"><div><div className="panel-title">Recent orders <span className="order-pill">{orders.length}</span></div><p>A live view of orders moving through your system.</p></div><button className="refresh-button" onClick={() => void refreshOrders()} aria-label="Refresh orders"><RefreshCw size={15} /></button></div>
            <div className="table-toolbar"><span className="table-caption">ALL ORDERS <ArrowDown size={12} /></span><label className="search-box"><Search size={15} /><input value={search} onChange={event => setSearch(event.target.value)} placeholder="Search orders..." aria-label="Search orders" /><kbd><Command size={10} /> K</kbd></label></div>
            <div className="table-wrap"><table><thead><tr><th>ORDER ID</th><th>CUSTOMER</th><th>ITEM</th><th>QUANTITY</th><th>AMOUNT</th><th>STATUS</th><th>CREATED</th><th /></tr></thead>
              <tbody>{loading ? <tr><td className="empty-state" colSpan={8}>Loading orders…</td></tr> : visibleOrders.length === 0 ? <tr><td className="empty-state" colSpan={8}>{search ? 'No orders match your search.' : 'No orders yet. Create one to see the event flow in action.'}</td></tr> : visibleOrders.map(order => <tr key={order.id}><td><span className="order-id">#{order.id.slice(0, 8).toUpperCase()}</span></td><td><div className="customer-cell"><span className="customer-avatar">{order.customerName.slice(0, 1).toUpperCase()}</span>{order.customerName}</div></td><td>{order.item}</td><td>{order.quantity.toString().padStart(2, '0')}</td><td className="amount-cell">{money(order.total)}</td><td><span className="confirmed"><Check size={12} /> Confirmed</span></td><td className="date-cell">{new Date(order.createdAt).toLocaleDateString()}</td><td><button className="row-action" aria-label={`View order ${order.id}`}><ArrowUpRight size={15} /></button></td></tr>)}</tbody>
            </table></div>
            <div className="table-footer"><span>Showing <strong>{visibleOrders.length}</strong> of <strong>{orders.length}</strong> orders</span><span className="updated"><span className="live-dot" /> Synced with API</span></div>
          </section>

          <section className="architecture-panel" id="architecture"><div className="architecture-title"><div><div className="eyebrow"><span className="eyebrow-line" /> UNDER THE HOOD</div><h2>One order. A complete event flow.</h2></div><span className="architecture-tag"><Activity size={13} /> LIVE ARCHITECTURE</span></div>
            <div className="flow"><div className="flow-node"><div className="flow-icon react-icon"><span>⚛</span></div><strong>React</strong><small>Web client</small></div><div className="flow-connector"><span /><span /><span /><span /><span /><ArrowUpRight size={13} /></div><div className="flow-node"><div className="flow-icon gateway-icon"><Command size={17} /></div><strong>API Gateway</strong><small>Port 8080</small></div><div className="flow-connector"><span /><span /><span /><span /><span /><ArrowUpRight size={13} /></div><div className="flow-node"><div className="flow-icon spring-icon"><Layers3 size={17} /></div><strong>Spring Boot</strong><small>REST · Port 8081</small></div><div className="flow-connector kafka-connector"><span /><span /><span /><span /><span /><ArrowUpRight size={13} /></div><div className="flow-node"><div className="flow-icon kafka-icon"><Radio size={17} /></div><strong>Kafka</strong><small>orders.created</small></div><div className="flow-connector"><span /><span /><span /><span /><span /><ArrowUpRight size={13} /></div><div className="flow-node"><div className="flow-icon consumer-icon"><Send size={16} /></div><strong>Consumer</strong><small>Notifications</small></div></div>
          </section>
          <footer className="page-footer"><span>Built for learning, made to ship. <span className="footer-heart">♥</span></span><span>REACT · SPRING BOOT · KAFKA</span></footer>
        </div>
      </main>

      {modalOpen && <div className="modal-backdrop" onMouseDown={event => { if (event.target === event.currentTarget) setModalOpen(false) }}><section className="order-modal" role="dialog" aria-modal="true" aria-labelledby="modal-title"><div className="modal-heading"><div><div className="eyebrow"><span className="eyebrow-line" /> NEW TRANSACTION</div><h2 id="modal-title">Create an order</h2><p>Submit a REST request and publish an event to Kafka.</p></div><button className="icon-button" onClick={() => setModalOpen(false)} aria-label="Close dialog"><X size={18} /></button></div><form onSubmit={event => void submitOrder(event)}><label>Customer name<input required minLength={1} value={form.customerName} onChange={event => setForm({ ...form, customerName: event.target.value })} placeholder="e.g. Alex Morgan" /></label><label>Item<input required minLength={1} value={form.item} onChange={event => setForm({ ...form, item: event.target.value })} placeholder="e.g. Mechanical keyboard" /></label><div className="form-row"><label>Quantity<input required type="number" min="1" step="1" value={form.quantity} onChange={event => setForm({ ...form, quantity: Number(event.target.value) })} /></label><label>Unit price (USD)<input required type="number" min="0.01" step="0.01" value={form.unitPrice || ''} onChange={event => setForm({ ...form, unitPrice: Number(event.target.value) })} placeholder="0.00" /></label></div><div className="modal-total"><span>Order total</span><strong>{money(form.quantity * form.unitPrice)}</strong></div><button className="primary-button modal-submit" type="submit" disabled={sending}>{sending ? <><RefreshCw className="spin" size={16} /> Sending…</> : <><Plus size={16} /> Create order</>}</button></form></section></div>}
    </div>
  )
}

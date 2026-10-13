import { useCallback, useEffect, useState } from 'react'
import { api } from './api.js'
import { ErrorBanner, OrderCard } from './ui.jsx'

export default function MyOrders() {
  const [orders, setOrders] = useState(null)
  const [error, setError] = useState('')

  const load = useCallback(() => {
    api
      .clientOrders()
      .then(setOrders)
      .catch((e) => setError(e.message))
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const cancel = async (id) => {
    if (!window.confirm('Отменить заказ?')) return
    try {
      await api.cancelOrder(id)
      load()
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <div className="stack">
      <h2>Мои заказы</h2>
      <ErrorBanner message={error} onClose={() => setError('')} />
      {orders === null && !error && <p className="muted">Загрузка…</p>}
      {orders?.length === 0 && <p className="muted">Заказов пока нет. Оформите первый на вкладке «Новый заказ».</p>}
      {orders?.map((order) => (
        <OrderCard key={order.id} order={order} perspective="client">
          {order.status === 'NEW' && (
            <button type="button" className="btn btn-danger" onClick={() => cancel(order.id)}>
              Отменить заказ
            </button>
          )}
        </OrderCard>
      ))}
    </div>
  )
}

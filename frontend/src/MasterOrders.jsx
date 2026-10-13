import { useCallback, useEffect, useState } from 'react'
import { api } from './api.js'
import { STATUS_LABELS } from './constants.js'
import { ErrorBanner, OrderCard } from './ui.jsx'

export default function MasterOrders() {
  const [orders, setOrders] = useState(null)
  const [filter, setFilter] = useState('')
  const [error, setError] = useState('')

  const load = useCallback(() => {
    api
      .masterOrders(filter)
      .then(setOrders)
      .catch((e) => setError(e.message))
  }, [filter])

  useEffect(() => {
    load()
  }, [load])

  const changeStatus = async (id, status, confirmText) => {
    if (confirmText && !window.confirm(confirmText)) return
    try {
      await api.setOrderStatus(id, status)
      load()
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <div className="stack">
      <div className="row-between">
        <h2>Заказы клиентов</h2>
        <label className="inline">
          Статус
          <select value={filter} onChange={(e) => setFilter(e.target.value)}>
            <option value="">Все</option>
            {Object.entries(STATUS_LABELS).map(([key, label]) => (
              <option key={key} value={key}>
                {label}
              </option>
            ))}
          </select>
        </label>
      </div>

      <ErrorBanner message={error} onClose={() => setError('')} />
      {orders === null && !error && <p className="muted">Загрузка…</p>}
      {orders?.length === 0 && <p className="muted">Заказов нет.</p>}

      {orders?.map((order) => (
        <OrderCard key={order.id} order={order} perspective="master">
          {order.status === 'NEW' && (
            <>
              <button type="button" className="btn btn-primary" onClick={() => changeStatus(order.id, 'IN_PROGRESS')}>
                Принять в работу
              </button>
              <button
                type="button"
                className="btn btn-danger"
                onClick={() => changeStatus(order.id, 'CANCELLED', 'Отклонить заказ?')}
              >
                Отклонить
              </button>
            </>
          )}
          {order.status === 'IN_PROGRESS' && (
            <>
              <button type="button" className="btn btn-primary" onClick={() => changeStatus(order.id, 'COMPLETED')}>
                Завершить
              </button>
              <button
                type="button"
                className="btn btn-danger"
                onClick={() => changeStatus(order.id, 'CANCELLED', 'Отменить заказ в работе?')}
              >
                Отменить
              </button>
            </>
          )}
        </OrderCard>
      ))}
    </div>
  )
}

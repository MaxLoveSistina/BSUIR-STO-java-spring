import { useEffect, useState } from 'react'
import { api } from './api.js'
import { money } from './format.js'
import { ErrorBanner } from './ui.jsx'

export default function MasterClients() {
  const [clients, setClients] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api
      .masterClients()
      .then(setClients)
      .catch((e) => setError(e.message))
  }, [])

  return (
    <div className="stack">
      <h2>Мои клиенты</h2>
      <p className="muted">Клиенты, которые оформляли у вас заказы (отменённые заказы не учитываются).</p>
      <ErrorBanner message={error} onClose={() => setError('')} />
      {clients === null && !error && <p className="muted">Загрузка…</p>}
      {clients?.length === 0 && <p className="muted">Пока нет клиентов.</p>}
      {clients?.length > 0 && (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Клиент</th>
                <th>Телефон</th>
                <th>Email</th>
                <th className="num">Заказов</th>
                <th className="num">На сумму</th>
              </tr>
            </thead>
            <tbody>
              {clients.map((c) => (
                <tr key={c.id}>
                  <td>
                    <strong>{c.fullName}</strong>
                  </td>
                  <td>{c.phone || '—'}</td>
                  <td>{c.email}</td>
                  <td className="num">{c.ordersCount}</td>
                  <td className="num strong">{money(c.totalSpent)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

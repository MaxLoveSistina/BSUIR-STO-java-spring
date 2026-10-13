import { STATUS_LABELS, MATERIAL_LABELS } from './constants.js'
import { dateTime, days, money } from './format.js'

export function StatusBadge({ status }) {
  return <span className={`badge badge-${status.toLowerCase()}`}>{STATUS_LABELS[status] ?? status}</span>
}

export function ErrorBanner({ message, onClose }) {
  if (!message) return null
  return (
    <div className="banner banner-error" role="alert">
      <span>{message}</span>
      {onClose && (
        <button type="button" className="link" onClick={onClose} aria-label="Закрыть">
          ✕
        </button>
      )}
    </div>
  )
}

export function SuccessBanner({ message }) {
  if (!message) return null
  return <div className="banner banner-ok">{message}</div>
}

/**
 * Карточка заказа. perspective: 'client' — показываем мастера, 'master' — показываем клиента.
 * Кнопки действий передаются через children.
 */
export function OrderCard({ order, perspective, children }) {
  const car = [order.carBrand, order.carModel].filter(Boolean).join(' ')
  return (
    <article className="card order">
      <header className="order-head">
        <div>
          <h3>Заказ №{order.id}</h3>
          <div className="muted">{dateTime(order.createdAt)}</div>
        </div>
        <StatusBadge status={order.status} />
      </header>

      <dl className="meta">
        {perspective === 'client' ? (
          <div>
            <dt>Мастер</dt>
            <dd>{order.masterName}</dd>
          </div>
        ) : (
          <div>
            <dt>Клиент</dt>
            <dd>
              {order.clientName}
              {order.clientPhone ? ` · ${order.clientPhone}` : ''}
            </dd>
          </div>
        )}
        <div>
          <dt>Автомобиль</dt>
          <dd>
            {car}
            {order.plateNumber ? ` · ${order.plateNumber}` : ''}
          </dd>
        </div>
      </dl>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Услуга</th>
              <th>Материалы</th>
              <th>Срок</th>
              <th className="num">Работа</th>
              <th className="num">Материалы</th>
              <th className="num">Итого</th>
            </tr>
          </thead>
          <tbody>
            {order.items.map((item, i) => (
              <tr key={`${item.serviceId}-${i}`}>
                <td>{item.serviceName}</td>
                <td>
                  {MATERIAL_LABELS[item.materialType]}
                  <div className="muted small">{item.materialName}</div>
                </td>
                <td>{days(item.durationDays)}</td>
                <td className="num">{money(item.laborPrice)}</td>
                <td className="num">{money(item.materialPrice)}</td>
                <td className="num strong">{money(item.price)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {order.comment && <p className="comment">«{order.comment}»</p>}

      <footer className="order-foot">
        <div className="total">
          Всего: <strong>{money(order.totalPrice)}</strong>
          <span className="muted"> · срок {days(order.totalDurationDays)}</span>
        </div>
        <div className="actions">{children}</div>
      </footer>
    </article>
  )
}

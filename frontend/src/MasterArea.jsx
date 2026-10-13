import { useState } from 'react'
import MasterServices from './MasterServices.jsx'
import MasterOrders from './MasterOrders.jsx'
import MasterClients from './MasterClients.jsx'

const TABS = [
  ['orders', 'Заказы'],
  ['services', 'Мои услуги'],
  ['clients', 'Мои клиенты'],
]

export default function MasterArea() {
  const [tab, setTab] = useState('orders')

  return (
    <>
      <nav className="tabs tabs-main">
        {TABS.map(([key, label]) => (
          <button type="button" key={key} className={tab === key ? 'tab active' : 'tab'} onClick={() => setTab(key)}>
            {label}
          </button>
        ))}
      </nav>
      {tab === 'orders' && <MasterOrders />}
      {tab === 'services' && <MasterServices />}
      {tab === 'clients' && <MasterClients />}
    </>
  )
}

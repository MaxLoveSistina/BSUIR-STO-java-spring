import { useState } from 'react'
import NewOrder from './NewOrder.jsx'
import MyOrders from './MyOrders.jsx'

export default function ClientArea() {
  const [tab, setTab] = useState('new')

  return (
    <>
      <nav className="tabs tabs-main">
        <button type="button" className={tab === 'new' ? 'tab active' : 'tab'} onClick={() => setTab('new')}>
          Новый заказ
        </button>
        <button type="button" className={tab === 'orders' ? 'tab active' : 'tab'} onClick={() => setTab('orders')}>
          Мои заказы
        </button>
      </nav>
      {tab === 'new' ? <NewOrder onCreated={() => setTab('orders')} /> : <MyOrders />}
    </>
  )
}

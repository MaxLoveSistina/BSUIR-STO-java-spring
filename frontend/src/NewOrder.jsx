import { useEffect, useMemo, useState } from 'react'
import { api } from './api.js'
import { MATERIAL_LABELS } from './constants.js'
import { days, money } from './format.js'
import { ErrorBanner } from './ui.jsx'

const DEFAULT_CHOICE = { materialType: 'ECONOMY', deadlineType: 'STANDARD' }

export default function NewOrder({ onCreated }) {
  const [masters, setMasters] = useState([])
  const [deadlines, setDeadlines] = useState([])
  const [masterId, setMasterId] = useState(null)
  const [services, setServices] = useState([])
  // выбранные услуги: { [serviceId]: { materialType, deadlineType } }
  const [selection, setSelection] = useState({})
  const [calc, setCalc] = useState(null)
  const [car, setCar] = useState({ carBrand: '', carModel: '', plateNumber: '', comment: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    Promise.all([api.masters(), api.deadlines()])
      .then(([m, d]) => {
        setMasters(m)
        setDeadlines(d)
      })
      .catch((e) => setError(e.message))
  }, [])

  // Позиции заказа в формате API
  const items = useMemo(
    () => Object.entries(selection).map(([id, c]) => ({ serviceId: Number(id), ...c })),
    [selection],
  )

  // Пересчёт стоимости на сервере при любом изменении выбора (с небольшой задержкой)
  useEffect(() => {
    if (!masterId || items.length === 0) return undefined
    let cancelled = false
    const timer = setTimeout(() => {
      api
        .calculate(masterId, items)
        .then((result) => {
          if (!cancelled) setCalc(result)
        })
        .catch((e) => {
          if (!cancelled) setError(e.message)
        })
    }, 250)
    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [masterId, items])

  const chooseMaster = async (id) => {
    setMasterId(id)
    setSelection({})
    setCalc(null)
    setServices([])
    setError('')
    try {
      setServices(await api.masterServices(id))
    } catch (e) {
      setError(e.message)
    }
  }

  const toggleService = (id) => {
    setSelection((prev) => {
      const next = { ...prev }
      if (next[id]) delete next[id]
      else next[id] = { ...DEFAULT_CHOICE }
      return next
    })
  }

  const setChoice = (id, patch) =>
    setSelection((prev) => ({ ...prev, [id]: { ...prev[id], ...patch } }))

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      await api.createOrder({
        masterId,
        carBrand: car.carBrand.trim(),
        carModel: car.carModel.trim(),
        plateNumber: car.plateNumber.trim() || null,
        comment: car.comment.trim() || null,
        items,
      })
      onCreated()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const hasItems = items.length > 0
  const visibleCalc = hasItems ? calc : null
  const priceByService = Object.fromEntries((visibleCalc?.items ?? []).map((i) => [i.serviceId, i]))

  return (
    <div className="stack">
      <ErrorBanner message={error} onClose={() => setError('')} />

      <section>
        <h2>1. Выберите мастера</h2>
        {masters.length === 0 ? (
          <p className="muted">Пока нет доступных мастеров.</p>
        ) : (
          <div className="grid">
            {masters.map((m) => (
              <button
                type="button"
                key={m.id}
                className={masterId === m.id ? 'card pick active' : 'card pick'}
                onClick={() => chooseMaster(m.id)}
              >
                <strong>{m.fullName}</strong>
                <span className="muted small">{m.phone || 'телефон не указан'}</span>
              </button>
            ))}
          </div>
        )}
      </section>

      {masterId && (
        <form className="stack" onSubmit={submit}>
          <section>
            <h2>2. Услуги, материалы и сроки</h2>
            {services.length === 0 ? (
              <p className="muted">У мастера пока нет доступных услуг.</p>
            ) : (
              <div className="stack-sm">
                {services.map((s) => {
                  const choice = selection[s.id]
                  const line = priceByService[s.id]
                  return (
                    <div key={s.id} className={choice ? 'card service selected' : 'card service'}>
                      <label className="service-head">
                        <input type="checkbox" checked={Boolean(choice)} onChange={() => toggleService(s.id)} />
                        <div className="grow">
                          <strong>{s.name}</strong>
                          {s.description && <div className="muted small">{s.description}</div>}
                        </div>
                        <div className="right">
                          <div>от {money(s.laborPrice)}</div>
                          <div className="muted small">работа · {days(s.durationDays)}</div>
                        </div>
                      </label>

                      {choice && (
                        <div className="options">
                          <fieldset>
                            <legend>Материалы</legend>
                            {['ECONOMY', 'PREMIUM'].map((type) => {
                              const name = type === 'ECONOMY' ? s.economyMaterialName : s.premiumMaterialName
                              const price = type === 'ECONOMY' ? s.economyMaterialPrice : s.premiumMaterialPrice
                              return (
                                <label key={type} className={choice.materialType === type ? 'choice active' : 'choice'}>
                                  <input
                                    type="radio"
                                    name={`mat-${s.id}`}
                                    checked={choice.materialType === type}
                                    onChange={() => setChoice(s.id, { materialType: type })}
                                  />
                                  <span>
                                    <b>{MATERIAL_LABELS[type]}</b> — {name}
                                    <span className="muted small"> · {money(price)}</span>
                                  </span>
                                </label>
                              )
                            })}
                          </fieldset>

                          <label className="deadline">
                            Срок выполнения
                            <select
                              value={choice.deadlineType}
                              onChange={(e) => setChoice(s.id, { deadlineType: e.target.value })}
                            >
                              {deadlines.map((d) => (
                                <option key={d.code} value={d.code}>
                                  {d.label} (работа ×{Number(d.priceMultiplier)}, срок ×{Number(d.durationFactor)})
                                </option>
                              ))}
                            </select>
                          </label>

                          {line && (
                            <div className="line-total">
                              {money(line.laborPrice)} работа + {money(line.materialPrice)} материалы ={' '}
                              <strong>{money(line.price)}</strong>
                              <span className="muted"> · {days(line.durationDays)}</span>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  )
                })}
              </div>
            )}
          </section>

          {hasItems && (
            <>
              <section>
                <h2>3. Автомобиль</h2>
                <div className="form-grid">
                  <label>
                    Марка
                    <input value={car.carBrand} onChange={(e) => setCar({ ...car, carBrand: e.target.value })} required maxLength={50} />
                  </label>
                  <label>
                    Модель
                    <input value={car.carModel} onChange={(e) => setCar({ ...car, carModel: e.target.value })} required maxLength={50} />
                  </label>
                  <label>
                    Госномер
                    <input value={car.plateNumber} onChange={(e) => setCar({ ...car, plateNumber: e.target.value })} maxLength={20} />
                  </label>
                  <label className="wide">
                    Комментарий
                    <textarea rows={2} value={car.comment} onChange={(e) => setCar({ ...car, comment: e.target.value })} maxLength={1000} />
                  </label>
                </div>
              </section>

              <section className="card summary">
                <div>
                  <div className="muted">Общая стоимость ({items.length} усл.)</div>
                  <div className="summary-total">{visibleCalc ? money(visibleCalc.totalPrice) : '…'}</div>
                  {visibleCalc && <div className="muted">Общий срок: {days(visibleCalc.totalDurationDays)}</div>}
                </div>
                <button type="submit" className="btn btn-primary btn-lg" disabled={busy || !visibleCalc}>
                  {busy ? 'Оформляем…' : 'Оформить заказ'}
                </button>
              </section>
            </>
          )}
        </form>
      )}
    </div>
  )
}

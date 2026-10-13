import { useCallback, useEffect, useState } from 'react'
import { api } from './api.js'
import { days, money } from './format.js'
import { ErrorBanner } from './ui.jsx'

const EMPTY = {
  name: '',
  description: '',
  laborPrice: '',
  durationDays: 1,
  economyMaterialName: '',
  economyMaterialPrice: '',
  premiumMaterialName: '',
  premiumMaterialPrice: '',
  active: true,
}

export default function MasterServices() {
  const [services, setServices] = useState(null)
  const [form, setForm] = useState(null) // null — форма закрыта; иначе { id?, ...поля }
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const load = useCallback(() => {
    api
      .myServices()
      .then(setServices)
      .catch((e) => setError(e.message))
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const toPayload = (s) => ({
    name: s.name.trim(),
    description: s.description?.trim() || null,
    laborPrice: Number(s.laborPrice),
    durationDays: Number(s.durationDays),
    economyMaterialName: s.economyMaterialName.trim(),
    economyMaterialPrice: Number(s.economyMaterialPrice),
    premiumMaterialName: s.premiumMaterialName.trim(),
    premiumMaterialPrice: Number(s.premiumMaterialPrice),
    active: s.active,
  })

  const save = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      if (form.id) await api.updateService(form.id, toPayload(form))
      else await api.createService(toPayload(form))
      setForm(null)
      load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const toggleActive = async (s) => {
    try {
      await api.updateService(s.id, toPayload({ ...s, active: !s.active }))
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const remove = async (s) => {
    if (!window.confirm(`Удалить услугу «${s.name}»? Уже оформленные заказы не изменятся.`)) return
    try {
      await api.deleteService(s.id)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const field = (name) => ({
    value: form[name],
    onChange: (e) => setForm({ ...form, [name]: e.target.value }),
  })

  return (
    <div className="stack">
      <div className="row-between">
        <h2>Мои услуги</h2>
        {!form && (
          <button type="button" className="btn btn-primary" onClick={() => setForm({ ...EMPTY })}>
            + Добавить услугу
          </button>
        )}
      </div>

      <ErrorBanner message={error} onClose={() => setError('')} />

      {form && (
        <form className="card stack-sm" onSubmit={save}>
          <h3>{form.id ? 'Редактирование услуги' : 'Новая услуга'}</h3>
          <div className="form-grid">
            <label className="wide">
              Название
              <input {...field('name')} required maxLength={150} />
            </label>
            <label className="wide">
              Описание
              <textarea rows={2} {...field('description')} maxLength={1000} />
            </label>
            <label>
              Стоимость работы, ₽
              <input type="number" min="0.01" step="0.01" {...field('laborPrice')} required />
            </label>
            <label>
              Базовый срок, дней
              <input type="number" min="1" max="365" step="1" {...field('durationDays')} required />
            </label>
            <fieldset className="wide material-set">
              <legend>Дешёвые материалы</legend>
              <label>
                Название
                <input {...field('economyMaterialName')} required maxLength={150} />
              </label>
              <label>
                Цена, ₽
                <input type="number" min="0" step="0.01" {...field('economyMaterialPrice')} required />
              </label>
            </fieldset>
            <fieldset className="wide material-set">
              <legend>Дорогие материалы</legend>
              <label>
                Название
                <input {...field('premiumMaterialName')} required maxLength={150} />
              </label>
              <label>
                Цена, ₽
                <input type="number" min="0" step="0.01" {...field('premiumMaterialPrice')} required />
              </label>
            </fieldset>
            <label className="inline wide">
              <input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} />
              Доступна клиентам для заказа
            </label>
          </div>
          <div className="actions">
            <button type="submit" className="btn btn-primary" disabled={busy}>
              {busy ? 'Сохраняем…' : 'Сохранить'}
            </button>
            <button type="button" className="btn btn-ghost" onClick={() => setForm(null)}>
              Отмена
            </button>
          </div>
        </form>
      )}

      {services === null && !error && <p className="muted">Загрузка…</p>}
      {services?.length === 0 && !form && <p className="muted">Услуг пока нет — добавьте первую.</p>}

      {services?.length > 0 && (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Услуга</th>
                <th className="num">Работа</th>
                <th>Срок</th>
                <th>Дешёвые материалы</th>
                <th>Дорогие материалы</th>
                <th>Статус</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {services.map((s) => (
                <tr key={s.id} className={s.active ? '' : 'dim'}>
                  <td>
                    <strong>{s.name}</strong>
                    {s.description && <div className="muted small">{s.description}</div>}
                  </td>
                  <td className="num">{money(s.laborPrice)}</td>
                  <td>{days(s.durationDays)}</td>
                  <td>
                    {s.economyMaterialName}
                    <div className="muted small">{money(s.economyMaterialPrice)}</div>
                  </td>
                  <td>
                    {s.premiumMaterialName}
                    <div className="muted small">{money(s.premiumMaterialPrice)}</div>
                  </td>
                  <td>{s.active ? 'Доступна' : 'Скрыта'}</td>
                  <td className="row-actions">
                    <button type="button" className="link" onClick={() => setForm({ ...s })}>
                      Изменить
                    </button>
                    <button type="button" className="link" onClick={() => toggleActive(s)}>
                      {s.active ? 'Скрыть' : 'Показать'}
                    </button>
                    <button type="button" className="link danger" onClick={() => remove(s)}>
                      Удалить
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

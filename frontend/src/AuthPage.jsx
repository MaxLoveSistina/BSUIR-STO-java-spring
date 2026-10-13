import { useState } from 'react'
import { api } from './api.js'
import { ErrorBanner } from './ui.jsx'

const emptyRegister = {
  role: 'ROLE_CLIENT',
  fullName: '',
  phone: '',
  username: '',
  email: '',
  password: '',
}

export default function AuthPage({ onAuth }) {
  const [mode, setMode] = useState('login')
  const [login, setLogin] = useState({ usernameOrEmail: '', password: '' })
  const [reg, setReg] = useState(emptyRegister)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      const response =
        mode === 'login'
          ? await api.login(login.usernameOrEmail.trim(), login.password)
          : await api.register({ ...reg, phone: reg.phone.trim() || null })
      onAuth(response)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const setReg1 = (field) => (e) => setReg({ ...reg, [field]: e.target.value })

  return (
    <div className="auth-wrap">
      <form className="card auth-card" onSubmit={submit}>
        <div className="brand brand-big">
          <span className="brand-mark">🔧</span> АвтоСервис
        </div>
        <p className="muted center">Запись на обслуживание и ремонт автомобиля</p>

        <div className="tabs">
          <button type="button" className={mode === 'login' ? 'tab active' : 'tab'} onClick={() => setMode('login')}>
            Вход
          </button>
          <button type="button" className={mode === 'register' ? 'tab active' : 'tab'} onClick={() => setMode('register')}>
            Регистрация
          </button>
        </div>

        <ErrorBanner message={error} onClose={() => setError('')} />

        {mode === 'login' ? (
          <>
            <label>
              Логин или email
              <input
                value={login.usernameOrEmail}
                onChange={(e) => setLogin({ ...login, usernameOrEmail: e.target.value })}
                autoComplete="username"
                required
              />
            </label>
            <label>
              Пароль
              <input
                type="password"
                value={login.password}
                onChange={(e) => setLogin({ ...login, password: e.target.value })}
                autoComplete="current-password"
                required
              />
            </label>
          </>
        ) : (
          <>
            <div className="role-switch">
              <label className={reg.role === 'ROLE_CLIENT' ? 'choice active' : 'choice'}>
                <input
                  type="radio"
                  name="role"
                  checked={reg.role === 'ROLE_CLIENT'}
                  onChange={() => setReg({ ...reg, role: 'ROLE_CLIENT' })}
                />
                Я клиент
              </label>
              <label className={reg.role === 'ROLE_MASTER' ? 'choice active' : 'choice'}>
                <input
                  type="radio"
                  name="role"
                  checked={reg.role === 'ROLE_MASTER'}
                  onChange={() => setReg({ ...reg, role: 'ROLE_MASTER' })}
                />
                Я мастер
              </label>
            </div>
            <label>
              Имя и фамилия
              <input value={reg.fullName} onChange={setReg1('fullName')} required maxLength={100} />
            </label>
            <label>
              Телефон
              <input value={reg.phone} onChange={setReg1('phone')} maxLength={30} />
            </label>
            <label>
              Логин
              <input value={reg.username} onChange={setReg1('username')} required minLength={3} maxLength={50} autoComplete="username" />
            </label>
            <label>
              Email
              <input type="email" value={reg.email} onChange={setReg1('email')} required autoComplete="email" />
            </label>
            <label>
              Пароль (от 6 символов)
              <input type="password" value={reg.password} onChange={setReg1('password')} required minLength={6} autoComplete="new-password" />
            </label>
          </>
        )}

        <button type="submit" className="btn btn-primary" disabled={busy}>
          {busy ? 'Подождите…' : mode === 'login' ? 'Войти' : 'Создать аккаунт'}
        </button>

        {mode === 'login' && (
          <p className="muted small center">
            Демо: <code>client_anna</code> / <code>master_ivan</code>, пароль <code>password123</code>
          </p>
        )}
      </form>
    </div>
  )
}

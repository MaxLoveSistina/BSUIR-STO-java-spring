import { useEffect, useState } from 'react'
import { loadAuth, saveAuth } from './api.js'
import AuthPage from './AuthPage.jsx'
import ClientArea from './ClientArea.jsx'
import MasterArea from './MasterArea.jsx'

function App() {
  const [auth, setAuth] = useState(() => loadAuth())

  useEffect(() => {
    // Токен протух — api.js сообщает событием
    const onExpired = () => {
      saveAuth(null)
      setAuth(null)
    }
    window.addEventListener('auth:expired', onExpired)
    return () => window.removeEventListener('auth:expired', onExpired)
  }, [])

  const handleAuth = (response) => {
    const next = { token: response.token, user: response.user }
    saveAuth(next)
    setAuth(next)
  }

  const logout = () => {
    saveAuth(null)
    setAuth(null)
  }

  if (!auth) return <AuthPage onAuth={handleAuth} />

  const isMaster = auth.user.role === 'ROLE_MASTER'

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <span className="brand-mark">🔧</span> АвтоСервис
        </div>
        <div className="user">
          <span className="role-chip">{isMaster ? 'Мастер' : 'Клиент'}</span>
          <span>{auth.user.fullName}</span>
          <button type="button" className="btn btn-ghost" onClick={logout}>
            Выйти
          </button>
        </div>
      </header>
      <main className="container">{isMaster ? <MasterArea /> : <ClientArea />}</main>
    </div>
  )
}

export default App

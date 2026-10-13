// Тонкая обёртка над fetch для REST API бэкенда.
const BASE = '/api/v1'
const STORAGE_KEY = 'autoservice.auth'

export function loadAuth() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || null
  } catch {
    return null
  }
}

export function saveAuth(auth) {
  if (auth) localStorage.setItem(STORAGE_KEY, JSON.stringify(auth))
  else localStorage.removeItem(STORAGE_KEY)
}

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const stored = auth ? loadAuth() : null
  if (stored?.token) headers.Authorization = `Bearer ${stored.token}`

  let response
  try {
    response = await fetch(`${BASE}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new Error('Нет связи с сервером. Проверьте, что backend запущен.')
  }

  if (response.status === 204) return null

  const text = await response.text()
  let data = null
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = null
    }
  }

  if (!response.ok) {
    // Токен истёк или недействителен — выходим из аккаунта
    if (response.status === 401 && stored?.token) {
      window.dispatchEvent(new Event('auth:expired'))
    }
    throw new Error(data?.message || `Ошибка ${response.status}`)
  }
  return data
}

export const api = {
  // auth
  login: (usernameOrEmail, password) =>
    request('/auth/login', { method: 'POST', body: { usernameOrEmail, password }, auth: false }),
  register: (payload) => request('/auth/register', { method: 'POST', body: payload, auth: false }),

  // справочники и каталог (публичные)
  deadlines: () => request('/meta/deadlines', { auth: false }),
  masters: () => request('/masters', { auth: false }),
  masterServices: (masterId) => request(`/masters/${masterId}/services`, { auth: false }),

  // клиент
  calculate: (masterId, items) =>
    request('/client/orders/calculate', { method: 'POST', body: { masterId, items } }),
  createOrder: (payload) => request('/client/orders', { method: 'POST', body: payload }),
  clientOrders: () => request('/client/orders'),
  cancelOrder: (id) => request(`/client/orders/${id}/cancel`, { method: 'PATCH' }),

  // мастер
  myServices: () => request('/master/services'),
  createService: (payload) => request('/master/services', { method: 'POST', body: payload }),
  updateService: (id, payload) => request(`/master/services/${id}`, { method: 'PUT', body: payload }),
  deleteService: (id) => request(`/master/services/${id}`, { method: 'DELETE' }),
  masterOrders: (status) => request(`/master/orders${status ? `?status=${status}` : ''}`),
  setOrderStatus: (id, status) =>
    request(`/master/orders/${id}/status`, { method: 'PATCH', body: { status } }),
  masterClients: () => request('/master/clients'),
}

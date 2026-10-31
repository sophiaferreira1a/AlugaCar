const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api').replace(/\/$/, '')
const TOKEN_KEY = 'alugacar.token'

export class ApiError extends Error {
  constructor(message, status, errors) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.errors = errors || {}
  }
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

async function request(path, { method = 'GET', body, headers = {} } = {}) {
  const options = {
    method,
    headers: { Accept: 'application/json', ...headers },
  }

  if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json'
    options.body = JSON.stringify(body)
  }

  const token = getToken()
  if (token) {
    options.headers.Authorization = `Bearer ${token}`
  }

  let response
  try {
    response = await fetch(`${API_URL}${path}`, options)
  } catch {
    throw new ApiError('Não foi possível conectar ao servidor. Verifique se o backend está rodando.', 0)
  }

  if (response.status === 401 && !path.startsWith('/auth/')) {
    window.dispatchEvent(new Event('alugacar:unauthorized'))
  }

  if (response.status === 204) {
    return null
  }

  const data = await response.json().catch(() => null)

  if (!response.ok) {
    const message =
      data?.detail || data?.message || `Não foi possível concluir a operação (${response.status}).`
    throw new ApiError(message, response.status, data?.errors)
  }

  return data
}

function registerPath(tipo) {
  const paths = {
    cliente: '/auth/register/cliente',
    empresa: '/auth/register/empresa',
    banco: '/auth/register/banco',
  }
  return paths[tipo] || paths.cliente
}

export const api = {
  login: (credentials) => request('/auth/login', { method: 'POST', body: credentials }),
  register: (tipo, payload) => request(registerPath(tipo), { method: 'POST', body: payload }),

  me: () => request('/perfil'),
  updatePerfil: (payload) => request('/perfil', { method: 'PUT', body: payload }),

  rendimentos: () => request('/rendimentos'),
  criarRendimento: (payload) => request('/rendimentos', { method: 'POST', body: payload }),
  atualizarRendimento: (id, payload) =>
    request(`/rendimentos/${id}`, { method: 'PUT', body: payload }),
  removerRendimento: (id) => request(`/rendimentos/${id}`, { method: 'DELETE' }),

  automoveis: () => request('/automoveis'),
  meusAutomoveis: () => request('/automoveis/meus'),
  criarAutomovel: (payload) => request('/automoveis', { method: 'POST', body: payload }),
  atualizarAutomovel: (id, payload) =>
    request(`/automoveis/${id}`, { method: 'PUT', body: payload }),
  removerAutomovel: (id) => request(`/automoveis/${id}`, { method: 'DELETE' }),

  meusPedidos: () => request('/pedidos/meus'),
  todosPedidos: () => request('/pedidos'),
  criarPedido: (payload) => request('/pedidos', { method: 'POST', body: payload }),
  atualizarPedido: (id, payload) => request(`/pedidos/${id}`, { method: 'PUT', body: payload }),
  cancelarPedido: (id) => request(`/pedidos/${id}/cancelar`, { method: 'PATCH' }),
  avaliarPedido: (id, payload) =>
    request(`/pedidos/${id}/status`, { method: 'PATCH', body: payload }),

  criarContrato: (payload) => request('/contratos', { method: 'POST', body: payload }),
}

export function mensagemErro(error) {
  if (error instanceof ApiError) {
    const details = Object.values(error.errors || {})
    if (details.length) {
      return `${error.message}: ${details.join(' ')}`
    }
    return error.message
  }
  return 'Ocorreu um erro inesperado.'
}

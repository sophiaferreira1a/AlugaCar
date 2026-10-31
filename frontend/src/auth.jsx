import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, clearToken, getToken, setToken } from './api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(() => getToken())
  const [usuario, setUsuario] = useState(null)
  const [loading, setLoading] = useState(Boolean(getToken()))

  const logout = useCallback(() => {
    clearToken()
    setTokenState(null)
    setUsuario(null)
    setLoading(false)
  }, [])

  const refreshUsuario = useCallback(async () => {
    if (!getToken()) {
      setLoading(false)
      return null
    }
    try {
      const perfil = await api.me()
      setUsuario(perfil)
      return perfil
    } catch {
      logout()
      return null
    } finally {
      setLoading(false)
    }
  }, [logout])

  useEffect(() => {
    refreshUsuario()
  }, [refreshUsuario])

  useEffect(() => {
    const onUnauthorized = () => logout()
    window.addEventListener('alugacar:unauthorized', onUnauthorized)
    return () => window.removeEventListener('alugacar:unauthorized', onUnauthorized)
  }, [logout])

  const login = useCallback(async (credentials) => {
    const response = await api.login(credentials)
    setToken(response.token)
    setTokenState(response.token)
    setUsuario(response.usuario)
    setLoading(false)
    return response.usuario
  }, [])

  const register = useCallback((tipo, payload) => api.register(tipo, payload), [])

  const value = useMemo(
    () => ({
      token,
      usuario,
      loading,
      login,
      register,
      logout,
      refreshUsuario,
      setUsuario,
    }),
    [token, usuario, loading, login, register, logout, refreshUsuario],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider')
  }
  return context
}

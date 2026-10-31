import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../auth'

export default function ProtectedRoute({ children }) {
  const { token, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <div className="full-loading">
        <div className="spinner" />
        <span>Carregando sua conta...</span>
      </div>
    )
  }

  if (!token) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  }

  return children
}

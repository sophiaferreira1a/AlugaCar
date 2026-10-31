import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import AutomoveisPage from './pages/AutomoveisPage'
import DashboardPage from './pages/DashboardPage'
import LoginPage from './pages/LoginPage'
import PedidosPage from './pages/PedidosPage'
import PerfilPage from './pages/PerfilPage'
import RegisterPage from './pages/RegisterPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/cadastro" element={<RegisterPage />} />

      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="/automoveis" element={<AutomoveisPage />} />
        <Route path="/pedidos" element={<PedidosPage />} />
        <Route path="/perfil" element={<PerfilPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

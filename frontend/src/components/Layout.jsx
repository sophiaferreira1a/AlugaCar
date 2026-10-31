import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'

const links = [
  { to: '/', label: 'Visão geral', end: true },
  { to: '/automoveis', label: 'Automóveis' },
  { to: '/pedidos', label: 'Pedidos' },
  { to: '/perfil', label: 'Meu perfil' },
]

const tipoLabel = {
  CLIENTE: 'Cliente',
  EMPRESA: 'Empresa',
  BANCO: 'Banco',
}

export default function Layout() {
  const { usuario, logout } = useAuth()
  const navigate = useNavigate()

  function sair() {
    logout()
    navigate('/login', { replace: true })
  }

  const iniciais = (usuario?.nome || 'AC')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((parte) => parte[0])
    .join('')
    .toUpperCase()

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">AC</span>
          <div>
            <strong>AlugaCar</strong>
            <small>Mobilidade com crédito</small>
          </div>
        </div>

        <nav className="main-nav">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.end}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
            >
              <span className="nav-dot" />
              {link.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <span className="badge badge-soft">{tipoLabel[usuario?.tipo] || usuario?.tipo}</span>
          <p>AlugaCar 2026</p>
        </div>
      </aside>

      <div className="main-column">
        <header className="topbar">
          <div>
            <span className="topbar-label">Área autenticada</span>
            <strong className="topbar-title">Olá, {usuario?.nome?.split(' ')[0] || 'usuário'}</strong>
          </div>
          <div className="topbar-user">
            <span className="avatar">{iniciais}</span>
            <div className="topbar-user-info">
              <strong>{usuario?.nome}</strong>
              <small>{tipoLabel[usuario?.tipo] || usuario?.tipo} · @{usuario?.username}</small>
            </div>
            <button type="button" className="btn btn-ghost" onClick={sair}>
              Sair
            </button>
          </div>
        </header>

        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { mensagemErro } from '../api'
import { useAuth } from '../auth'

export default function LoginPage() {
  const { login, token } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ username: '', senha: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const mensagem = location.state?.message

  useEffect(() => {
    if (token) navigate('/', { replace: true })
  }, [token, navigate])

  function atualizar(event) {
    setForm((atual) => ({ ...atual, [event.target.name]: event.target.value }))
  }

  async function enviar(event) {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(form)
      navigate(location.state?.from || '/', { replace: true })
    } catch (err) {
      setError(mensagemErro(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <section className="auth-hero">
        <div className="auth-hero-content">
          <span className="eyebrow">AlugaCar</span>
          <h1>Seu próximo carro começa com uma boa análise.</h1>
          <p>
            Solicite o aluguel, acompanhe a avaliação da empresa e execute o contrato de crédito
            com o banco — tudo em um só lugar.
          </p>
          <div className="hero-points">
            <span>Pedidos online</span>
            <span>Análise de crédito</span>
            <span>Contrato digital</span>
          </div>
        </div>
        <div className="hero-car" aria-hidden="true">
          <span />
        </div>
      </section>

      <section className="auth-panel">
        <div className="auth-card">
          <span className="eyebrow">Bem-vindo de volta</span>
          <h2>Entrar na conta</h2>
          <p className="muted">Use o nome de usuário e a senha cadastrados.</p>

          {mensagem && <div className="alert alert-success">{mensagem}</div>}
          {error && <div className="alert alert-error">{error}</div>}

          <form className="form" onSubmit={enviar}>
            <label>
              Usuário
              <input
                name="username"
                value={form.username}
                onChange={atualizar}
                autoComplete="username"
                required
                placeholder="seu.usuario"
              />
            </label>
            <label>
              Senha
              <input
                type="password"
                name="senha"
                value={form.senha}
                onChange={atualizar}
                autoComplete="current-password"
                required
                placeholder="••••••••"
              />
            </label>
            <button className="btn btn-primary btn-block" type="submit" disabled={loading}>
              {loading ? 'Entrando...' : 'Entrar'}
            </button>
          </form>

          <p className="auth-switch">
            Ainda não tem conta? <Link to="/cadastro">Criar cadastro</Link>
          </p>
        </div>
      </section>
    </div>
  )
}

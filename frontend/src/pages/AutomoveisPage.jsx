import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, mensagemErro } from '../api'
import { useAuth } from '../auth'

const formInicial = {
  matricula: '',
  ano: new Date().getFullYear(),
  marca: '',
  modelo: '',
  placa: '',
}

export default function AutomoveisPage() {
  const { usuario } = useAuth()
  const navigate = useNavigate()
  const [automoveis, setAutomoveis] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [form, setForm] = useState(formInicial)
  const [showForm, setShowForm] = useState(false)
  const [editId, setEditId] = useState(null)
  const [salvando, setSalvando] = useState(false)

  async function carregar() {
    setLoading(true)
    setError('')
    try {
      setAutomoveis(await api.automoveis())
    } catch (err) {
      setError(mensagemErro(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    carregar()
  }, [])

  function atualizar(event) {
    setForm((atual) => ({ ...atual, [event.target.name]: event.target.value }))
  }

  function abrirNovo() {
    setForm(formInicial)
    setEditId(null)
    setShowForm(true)
    setSucesso('')
  }

  function editar(automovel) {
    setForm({
      matricula: automovel.matricula,
      ano: automovel.ano,
      marca: automovel.marca,
      modelo: automovel.modelo,
      placa: automovel.placa,
    })
    setEditId(automovel.id)
    setShowForm(true)
    setSucesso('')
  }

  async function enviar(event) {
    event.preventDefault()
    setError('')
    setSucesso('')
    setSalvando(true)
    try {
      const payload = { ...form, ano: Number(form.ano) }
      if (editId) {
        await api.atualizarAutomovel(editId, payload)
        setSucesso('Automóvel atualizado com sucesso.')
      } else {
        await api.criarAutomovel(payload)
        setSucesso('Automóvel cadastrado com sucesso.')
      }
      setShowForm(false)
      setForm(formInicial)
      setEditId(null)
      await carregar()
    } catch (err) {
      setError(mensagemErro(err))
    } finally {
      setSalvando(false)
    }
  }

  async function remover(automovel) {
    if (!window.confirm(`Remover ${automovel.marca} ${automovel.modelo} (${automovel.placa})?`)) {
      return
    }
    setError('')
    try {
      await api.removerAutomovel(automovel.id)
      setSucesso('Automóvel removido.')
      await carregar()
    } catch (err) {
      setError(mensagemErro(err))
    }
  }

  function fazerPedido(automovel) {
    navigate(`/pedidos?automovel=${automovel.id}`)
  }

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <span className="eyebrow">Frota</span>
          <h1>Automóveis</h1>
          <p className="muted">Consulte a frota e gerencie os veículos de sua propriedade.</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={abrirNovo}>
          + Cadastrar automóvel
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {sucesso && <div className="alert alert-success">{sucesso}</div>}

      {showForm && (
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>{editId ? 'Editar automóvel' : 'Novo automóvel'}</h2>
              <p className="muted">O veículo será vinculado ao seu usuário como proprietário.</p>
            </div>
            <button type="button" className="btn btn-ghost" onClick={() => setShowForm(false)}>
              Fechar
            </button>
          </div>

          <form className="form" onSubmit={enviar}>
            <div className="form-grid">
              <label>
                Matrícula
                <input name="matricula" value={form.matricula} onChange={atualizar} required />
              </label>
              <label>
                Ano
                <input
                  type="number"
                  name="ano"
                  min="1900"
                  max="2100"
                  value={form.ano}
                  onChange={atualizar}
                  required
                />
              </label>
              <label>
                Marca
                <input name="marca" value={form.marca} onChange={atualizar} required />
              </label>
              <label>
                Modelo
                <input name="modelo" value={form.modelo} onChange={atualizar} required />
              </label>
              <label>
                Placa
                <input
                  name="placa"
                  value={form.placa}
                  onChange={atualizar}
                  required
                  maxLength={10}
                  style={{ textTransform: 'uppercase' }}
                />
              </label>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={salvando}>
                {salvando ? 'Salvando...' : editId ? 'Salvar alterações' : 'Cadastrar'}
              </button>
              <button type="button" className="btn btn-ghost" onClick={() => setShowForm(false)}>
                Cancelar
              </button>
            </div>
          </form>
        </section>
      )}

      <section className="panel">
        <div className="panel-heading">
          <div>
            <h2>Frota cadastrada</h2>
            <p className="muted">{automoveis.length} automóvel(is) disponíveis na plataforma.</p>
          </div>
          <button type="button" className="btn btn-ghost" onClick={carregar}>
            Atualizar
          </button>
        </div>

        {loading ? (
          <div className="empty-state">Carregando automóveis...</div>
        ) : automoveis.length === 0 ? (
          <div className="empty-state">
            <strong>Nenhum automóvel cadastrado.</strong>
            <span>Cadastre o primeiro veículo para começar.</span>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Veículo</th>
                  <th>Ano</th>
                  <th>Placa</th>
                  <th>Matrícula</th>
                  <th>Proprietário</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {automoveis.map((automovel) => {
                  const meu = automovel.proprietarioId === usuario?.id
                  return (
                    <tr key={automovel.id}>
                      <td>
                        <strong>
                          {automovel.marca} {automovel.modelo}
                        </strong>
                        {meu && <span className="badge badge-soft ml">Meu</span>}
                      </td>
                      <td>{automovel.ano}</td>
                      <td>
                        <span className="plate">{automovel.placa}</span>
                      </td>
                      <td className="mono">{automovel.matricula}</td>
                      <td>
                        {automovel.proprietarioNome}
                        <small className="table-sub">{automovel.proprietarioTipo}</small>
                      </td>
                      <td>
                        <div className="row-actions">
                          {usuario?.tipo === 'CLIENTE' && (
                            <button
                              type="button"
                              className="btn btn-small btn-primary"
                              onClick={() => fazerPedido(automovel)}
                            >
                              Alugar
                            </button>
                          )}
                          {meu && (
                            <>
                              <button
                                type="button"
                                className="btn btn-small btn-ghost"
                                onClick={() => editar(automovel)}
                              >
                                Editar
                              </button>
                              <button
                                type="button"
                                className="btn btn-small btn-danger-ghost"
                                onClick={() => remover(automovel)}
                              >
                                Excluir
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel muted-panel">
        <div>
          <h3>Como funciona a propriedade?</h3>
          <p className="muted">
            Cada automóvel pertence a um cliente, empresa ou banco. Somente o proprietário pode
            editar ou excluir o veículo. Qualquer pessoa autenticada pode consultar a frota.
          </p>
        </div>
      </section>
    </div>
  )
}

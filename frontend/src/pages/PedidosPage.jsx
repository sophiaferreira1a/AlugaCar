import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api, mensagemErro } from '../api'
import { useAuth } from '../auth'
import StatusBadge, { statusLabels } from '../components/StatusBadge'
import { adicionarDiasISO, formatarData, formatarMoeda, hojeISO } from '../utils'

const formInicial = {
  automovelId: '',
  dataInicio: hojeISO(),
  dataFim: adicionarDiasISO(3),
  observacoes: '',
}

const statusFiltros = [
  'TODOS',
  'PENDENTE',
  'EM_ANALISE',
  'APROVADO',
  'REPROVADO',
  'CANCELADO',
  'CONTRATO_EXECUTADO',
]

export default function PedidosPage() {
  const { usuario } = useAuth()
  const [searchParams] = useSearchParams()
  const ehCliente = usuario?.tipo === 'CLIENTE'
  const ehBanco = usuario?.tipo === 'BANCO'

  const [pedidos, setPedidos] = useState([])
  const [automoveis, setAutomoveis] = useState([])
  const [form, setForm] = useState(formInicial)
  const [editId, setEditId] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [filtro, setFiltro] = useState('TODOS')
  const [loading, setLoading] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [error, setError] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [contratoPedido, setContratoPedido] = useState(null)
  const [contratoForm, setContratoForm] = useState({
    valor: '',
    taxaJuros: '1.50',
    prazoMeses: '12',
  })

  async function carregar() {
    setLoading(true)
    setError('')
    try {
      if (ehCliente) {
        const [listaPedidos, listaAutomoveis] = await Promise.all([
          api.meusPedidos(),
          api.automoveis(),
        ])
        setPedidos(listaPedidos)
        setAutomoveis(listaAutomoveis)
      } else {
        setPedidos(await api.todosPedidos())
      }
    } catch (err) {
      setError(mensagemErro(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    carregar()
  }, [ehCliente])

  useEffect(() => {
    const automovelId = searchParams.get('automovel')
    if (ehCliente && automovelId) {
      setForm((atual) => ({ ...atual, automovelId }))
      setEditId(null)
      setShowForm(true)
    }
  }, [searchParams, ehCliente])

  const pedidosFiltrados = useMemo(
    () =>
      filtro === 'TODOS' ? pedidos : pedidos.filter((pedido) => pedido.status === filtro),
    [pedidos, filtro],
  )

  function atualizar(event) {
    setForm((atual) => ({ ...atual, [event.target.name]: event.target.value }))
  }

  function abrirNovo() {
    setForm({ ...formInicial, automovelId: automoveis[0]?.id || '' })
    setEditId(null)
    setShowForm(true)
    setSucesso('')
  }

  function editar(pedido) {
    setForm({
      automovelId: pedido.automovelId,
      dataInicio: pedido.dataInicio,
      dataFim: pedido.dataFim,
      observacoes: pedido.observacoes || '',
    })
    setEditId(pedido.id)
    setShowForm(true)
    setSucesso('')
  }

  async function enviar(event) {
    event.preventDefault()
    setError('')
    setSucesso('')
    setSalvando(true)
    try {
      if (editId) {
        await api.atualizarPedido(editId, {
          dataInicio: form.dataInicio,
          dataFim: form.dataFim,
          observacoes: form.observacoes,
        })
        setSucesso('Pedido atualizado com sucesso.')
      } else {
        await api.criarPedido({
          automovelId: form.automovelId,
          dataInicio: form.dataInicio,
          dataFim: form.dataFim,
          observacoes: form.observacoes,
        })
        setSucesso('Pedido criado. Agora é só aguardar a análise.')
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

  async function cancelar(pedido) {
    if (!window.confirm('Cancelar este pedido?')) return
    setError('')
    try {
      await api.cancelarPedido(pedido.id)
      setSucesso('Pedido cancelado.')
      await carregar()
    } catch (err) {
      setError(mensagemErro(err))
    }
  }

  async function avaliar(pedido, status) {
    let observacoes
    if (status === 'REPROVADO') {
      observacoes = window.prompt('Informe o motivo da reprovação (opcional):') ?? undefined
    }
    setError('')
    try {
      await api.avaliarPedido(pedido.id, { status, observacoes })
      setSucesso(`Pedido movido para ${statusLabels[status]}.`)
      await carregar()
    } catch (err) {
      setError(mensagemErro(err))
    }
  }

  function abrirContrato(pedido) {
    setContratoPedido(pedido)
    setContratoForm({ valor: '', taxaJuros: '1.50', prazoMeses: '12' })
    setError('')
    setSucesso('')
  }

  async function enviarContrato(event) {
    event.preventDefault()
    setError('')
    setSalvando(true)
    try {
      await api.criarContrato({
        pedidoId: contratoPedido.id,
        valor: Number(contratoForm.valor),
        taxaJuros: Number(contratoForm.taxaJuros),
        prazoMeses: Number(contratoForm.prazoMeses),
      })
      setSucesso('Contrato de crédito registrado e pedido finalizado.')
      setContratoPedido(null)
      await carregar()
    } catch (err) {
      setError(mensagemErro(err))
    } finally {
      setSalvando(false)
    }
  }

  function acoes(pedido) {
    if (ehCliente) {
      return (
        <div className="row-actions">
          {['PENDENTE', 'EM_ANALISE'].includes(pedido.status) && (
            <button type="button" className="btn btn-small btn-ghost" onClick={() => editar(pedido)}>
              Editar
            </button>
          )}
          {!['REPROVADO', 'CANCELADO', 'CONTRATO_EXECUTADO'].includes(pedido.status) && (
            <button
              type="button"
              className="btn btn-small btn-danger-ghost"
              onClick={() => cancelar(pedido)}
            >
              Cancelar
            </button>
          )}
        </div>
      )
    }

    return (
      <div className="row-actions">
        {pedido.status === 'PENDENTE' && (
          <button
            type="button"
            className="btn btn-small btn-primary"
            onClick={() => avaliar(pedido, 'EM_ANALISE')}
          >
            Iniciar análise
          </button>
        )}
        {pedido.status === 'EM_ANALISE' && (
          <>
            <button
              type="button"
              className="btn btn-small btn-success"
              onClick={() => avaliar(pedido, 'APROVADO')}
            >
              Aprovar
            </button>
            <button
              type="button"
              className="btn btn-small btn-danger-ghost"
              onClick={() => avaliar(pedido, 'REPROVADO')}
            >
              Reprovar
            </button>
          </>
        )}
        {pedido.status === 'APROVADO' && ehBanco && (
          <button
            type="button"
            className="btn btn-small btn-primary"
            onClick={() => abrirContrato(pedido)}
          >
            Registrar contrato
          </button>
        )}
        {pedido.status === 'APROVADO' && !ehBanco && (
          <span className="muted small-text">Aguardando banco</span>
        )}
      </div>
    )
  }

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <span className="eyebrow">Locação</span>
          <h1>Pedidos de aluguel</h1>
          <p className="muted">
            {ehCliente
              ? 'Solicite um automóvel, acompanhe a análise e consulte o contrato.'
              : 'Analise as solicitações, aprove pedidos e registre contratos de crédito.'}
          </p>
        </div>
        {ehCliente && (
          <button type="button" className="btn btn-primary" onClick={abrirNovo}>
            + Novo pedido
          </button>
        )}
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {sucesso && <div className="alert alert-success">{sucesso}</div>}

      {ehCliente && showForm && (
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>{editId ? 'Editar pedido' : 'Novo pedido'}</h2>
              <p className="muted">Escolha o veículo e o período desejado.</p>
            </div>
            <button type="button" className="btn btn-ghost" onClick={() => setShowForm(false)}>
              Fechar
            </button>
          </div>

          <form className="form" onSubmit={enviar}>
            <div className="form-grid">
              <label>
                Automóvel
                <select
                  name="automovelId"
                  value={form.automovelId}
                  onChange={atualizar}
                  required
                  disabled={Boolean(editId)}
                >
                  <option value="">Selecione um automóvel</option>
                  {automoveis.map((automovel) => (
                    <option key={automovel.id} value={automovel.id}>
                      {automovel.marca} {automovel.modelo} · {automovel.placa} ·{' '}
                      {automovel.proprietarioNome}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Data de início
                <input
                  type="date"
                  name="dataInicio"
                  value={form.dataInicio}
                  onChange={atualizar}
                  required
                />
              </label>
              <label>
                Data de fim
                <input
                  type="date"
                  name="dataFim"
                  value={form.dataFim}
                  onChange={atualizar}
                  required
                />
              </label>
              <label className="span-2">
                Observações
                <textarea
                  name="observacoes"
                  rows="3"
                  value={form.observacoes}
                  onChange={atualizar}
                  placeholder="Alguma informação importante para a análise?"
                />
              </label>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={salvando}>
                {salvando ? 'Enviando...' : editId ? 'Salvar alterações' : 'Criar pedido'}
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
            <h2>{ehCliente ? 'Meus pedidos' : 'Pedidos recebidos'}</h2>
            <p className="muted">{pedidosFiltrados.length} pedido(s) nesta visão.</p>
          </div>
          <div className="filter-row">
            <label className="compact-field">
              Status
              <select value={filtro} onChange={(event) => setFiltro(event.target.value)}>
                {statusFiltros.map((status) => (
                  <option key={status} value={status}>
                    {status === 'TODOS' ? 'Todos' : statusLabels[status]}
                  </option>
                ))}
              </select>
            </label>
            <button type="button" className="btn btn-ghost" onClick={carregar}>
              Atualizar
            </button>
          </div>
        </div>

        {loading ? (
          <div className="empty-state">Carregando pedidos...</div>
        ) : pedidosFiltrados.length === 0 ? (
          <div className="empty-state">
            <strong>Nenhum pedido encontrado.</strong>
            <span>
              {ehCliente
                ? 'Crie um novo pedido a partir da frota disponível.'
                : 'Não há pedidos com o status selecionado.'}
            </span>
            {ehCliente && (
              <button type="button" className="btn btn-primary" onClick={abrirNovo}>
                Criar pedido
              </button>
            )}
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Pedido</th>
                  {!ehCliente && <th>Cliente</th>}
                  <th>Automóvel</th>
                  <th>Período</th>
                  <th>Status</th>
                  <th>Contrato</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {pedidosFiltrados.map((pedido) => (
                  <tr key={pedido.id}>
                    <td>
                      <span className="mono">#{pedido.id.slice(0, 8)}</span>
                      <small className="table-sub">{formatarData(pedido.dataCriacao)}</small>
                    </td>
                    {!ehCliente && <td>{pedido.clienteNome}</td>}
                    <td>{pedido.automovelDescricao}</td>
                    <td>
                      {formatarData(pedido.dataInicio)} → {formatarData(pedido.dataFim)}
                    </td>
                    <td>
                      <StatusBadge status={pedido.status} />
                    </td>
                    <td>
                      {pedido.contrato ? (
                        <span className="contract-cell">
                          <strong>{formatarMoeda(pedido.contrato.valor)}</strong>
                          <small>{pedido.contrato.bancoNome}</small>
                        </span>
                      ) : (
                        <span className="muted">—</span>
                      )}
                    </td>
                    <td>{acoes(pedido)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel muted-panel">
        <div>
          <h3>Fluxo do pedido</h3>
          <p className="muted">
            Pendente → Em análise → Aprovado → Contrato executado. Pedidos reprovados ou
            cancelados são finalizados. O contrato de crédito é exclusivo do perfil Banco.
          </p>
        </div>
      </section>

      {contratoPedido && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal" role="dialog" aria-modal="true" aria-labelledby="contrato-titulo">
            <div className="modal-heading">
              <div>
                <span className="eyebrow">Contrato de crédito</span>
                <h2 id="contrato-titulo">Pedido #{contratoPedido.id.slice(0, 8)}</h2>
                <p className="muted">{contratoPedido.automovelDescricao}</p>
              </div>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setContratoPedido(null)}
              >
                Fechar
              </button>
            </div>

            <form className="form" onSubmit={enviarContrato}>
              <div className="form-grid">
                <label>
                  Valor do crédito
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={contratoForm.valor}
                    onChange={(event) =>
                      setContratoForm((atual) => ({ ...atual, valor: event.target.value }))
                    }
                    required
                    placeholder="0,00"
                  />
                </label>
                <label>
                  Taxa de juros (%)
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={contratoForm.taxaJuros}
                    onChange={(event) =>
                      setContratoForm((atual) => ({ ...atual, taxaJuros: event.target.value }))
                    }
                    required
                  />
                </label>
                <label>
                  Prazo (meses)
                  <input
                    type="number"
                    min="1"
                    step="1"
                    value={contratoForm.prazoMeses}
                    onChange={(event) =>
                      setContratoForm((atual) => ({ ...atual, prazoMeses: event.target.value }))
                    }
                    required
                  />
                </label>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary" disabled={salvando}>
                  {salvando ? 'Registrando...' : 'Registrar contrato'}
                </button>
                <button
                  type="button"
                  className="btn btn-ghost"
                  onClick={() => setContratoPedido(null)}
                >
                  Cancelar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

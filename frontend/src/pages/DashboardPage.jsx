import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, mensagemErro } from '../api'
import { useAuth } from '../auth'
import StatusBadge from '../components/StatusBadge'
import { formatarData, formatarMoeda } from '../utils'

export default function DashboardPage() {
  const { usuario } = useAuth()
  const [pedidos, setPedidos] = useState([])
  const [automoveis, setAutomoveis] = useState([])
  const [meusAutomoveis, setMeusAutomoveis] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const ehCliente = usuario?.tipo === 'CLIENTE'

  useEffect(() => {
    let ativo = true
    async function carregar() {
      setLoading(true)
      setError('')
      try {
        if (ehCliente) {
          const [listaPedidos, listaAutomoveis, listaMeus] = await Promise.all([
            api.meusPedidos(),
            api.automoveis(),
            api.meusAutomoveis(),
          ])
          if (!ativo) return
          setPedidos(listaPedidos)
          setAutomoveis(listaAutomoveis)
          setMeusAutomoveis(listaMeus)
        } else {
          const [listaPedidos, listaAutomoveis] = await Promise.all([
            api.todosPedidos(),
            api.automoveis(),
          ])
          if (!ativo) return
          setPedidos(listaPedidos)
          setAutomoveis(listaAutomoveis)
        }
      } catch (err) {
        if (ativo) setError(mensagemErro(err))
      } finally {
        if (ativo) setLoading(false)
      }
    }
    carregar()
    return () => {
      ativo = false
    }
  }, [ehCliente])

  const pedidosAtivos = pedidos.filter(
    (pedido) => !['REPROVADO', 'CANCELADO', 'CONTRATO_EXECUTADO'].includes(pedido.status),
  )
  const pedidosConcluidos = pedidos.filter((pedido) => pedido.status === 'CONTRATO_EXECUTADO')
  const pedidosAguardando = pedidos.filter((pedido) =>
    ['PENDENTE', 'EM_ANALISE'].includes(pedido.status),
  )
  const valorContratos = pedidos.reduce(
    (total, pedido) => total + Number(pedido.contrato?.valor || 0),
    0,
  )

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <span className="eyebrow">Visão geral</span>
          <h1>{ehCliente ? 'Sua jornada de aluguel' : 'Central de análise'}</h1>
          <p className="muted">
            {ehCliente
              ? 'Acompanhe seus pedidos, automóveis e contratos.'
              : 'Veja os pedidos da plataforma e avance as análises.'}
          </p>
        </div>
        <div className="heading-actions">
          {ehCliente ? (
            <>
              <Link className="btn btn-primary" to="/pedidos">
                Novo pedido
              </Link>
              <Link className="btn btn-ghost" to="/automoveis">
                Ver automóveis
              </Link>
            </>
          ) : (
            <Link className="btn btn-primary" to="/pedidos">
              Abrir pedidos
            </Link>
          )}
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="stat-grid">
        <article className="stat-card">
          <span>Pedidos totais</span>
          <strong>{loading ? '—' : pedidos.length}</strong>
          <small>{pedidosAtivos.length} em andamento</small>
        </article>
        <article className="stat-card accent">
          <span>Aguardando ação</span>
          <strong>{loading ? '—' : pedidosAguardando.length}</strong>
          <small>Pendentes ou em análise</small>
        </article>
        <article className="stat-card">
          <span>Automóveis</span>
          <strong>{loading ? '—' : automoveis.length}</strong>
          <small>{ehCliente ? `${meusAutomoveis.length} de sua propriedade` : 'Frota cadastrada'}</small>
        </article>
        <article className="stat-card">
          <span>{ehCliente ? 'Contratos executados' : 'Crédito concedido'}</span>
          <strong>{loading ? '—' : ehCliente ? pedidosConcluidos.length : formatarMoeda(valorContratos)}</strong>
          <small>{ehCliente ? 'Pedidos finalizados' : 'Somatório dos contratos'}</small>
        </article>
      </div>

      <section className="panel">
        <div className="panel-heading">
          <div>
            <h2>Pedidos recentes</h2>
            <p className="muted">Os últimos pedidos registrados na plataforma.</p>
          </div>
          <Link className="text-link" to="/pedidos">
            Ver todos
          </Link>
        </div>

        {loading ? (
          <div className="empty-state">Carregando pedidos...</div>
        ) : pedidos.length === 0 ? (
          <div className="empty-state">
            <strong>Nenhum pedido por aqui.</strong>
            <span>
              {ehCliente
                ? 'Escolha um automóvel e faça sua primeira solicitação.'
                : 'Os pedidos dos clientes aparecerão nesta lista.'}
            </span>
            {ehCliente && (
              <Link className="btn btn-primary" to="/automoveis">
                Escolher automóvel
              </Link>
            )}
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Pedido</th>
                  <th>Cliente</th>
                  <th>Automóvel</th>
                  <th>Período</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {pedidos.slice(0, 6).map((pedido) => (
                  <tr key={pedido.id}>
                    <td>
                      <span className="mono">#{pedido.id.slice(0, 8)}</span>
                      <small className="table-sub">{formatarData(pedido.dataCriacao)}</small>
                    </td>
                    <td>{pedido.clienteNome}</td>
                    <td>{pedido.automovelDescricao}</td>
                    <td>
                      {formatarData(pedido.dataInicio)} → {formatarData(pedido.dataFim)}
                    </td>
                    <td>
                      <StatusBadge status={pedido.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel split-panel">
        <div>
          <h2>Como funciona</h2>
          <ol className="steps">
            <li>
              <strong>Pedido</strong>
              <span>O cliente escolhe o automóvel e informa o período.</span>
            </li>
            <li>
              <strong>Análise</strong>
              <span>Empresa ou banco avalia e aprova o pedido.</span>
            </li>
            <li>
              <strong>Contrato</strong>
              <span>O banco registra o crédito e finaliza a locação.</span>
            </li>
          </ol>
        </div>
        <div className="info-card">
          <span className="eyebrow">Perfil atual</span>
          <h3>{usuario?.nome}</h3>
          <p className="muted">
            {usuario?.tipo === 'CLIENTE'
              ? `CPF ${usuario?.cpf || '—'}`
              : `${usuario?.tipo === 'BANCO' ? 'Banco' : 'Empresa'} · CNPJ ${usuario?.cnpj || '—'}`}
          </p>
          <dl className="mini-list">
            <div>
              <dt>Usuário</dt>
              <dd>@{usuario?.username}</dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd>{usuario?.ativo ? 'Ativo' : 'Inativo'}</dd>
            </div>
            <div>
              <dt>Cadastro</dt>
              <dd>{formatarData(usuario?.dataCadastro)}</dd>
            </div>
          </dl>
          <Link className="btn btn-ghost" to="/perfil">
            Gerenciar perfil
          </Link>
        </div>
      </section>
    </div>
  )
}

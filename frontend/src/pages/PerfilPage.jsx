import { useEffect, useState } from 'react'
import { api, mensagemErro } from '../api'
import { useAuth } from '../auth'
import { formatarData, formatarMoeda } from '../utils'

const enderecoVazio = {
  logradouro: '',
  numero: '',
  complemento: '',
  bairro: '',
  cidade: '',
  uf: '',
  cep: '',
}

const tipoLabel = {
  CLIENTE: 'Cliente',
  EMPRESA: 'Empresa',
  BANCO: 'Banco',
}

export default function PerfilPage() {
  const { usuario, setUsuario } = useAuth()
  const ehCliente = usuario?.tipo === 'CLIENTE'

  const [form, setForm] = useState({
    nome: usuario?.nome || '',
    profissao: usuario?.profissao || '',
    endereco: enderecoVazio,
  })
  const [rendimentos, setRendimentos] = useState([])
  const [novoRendimento, setNovoRendimento] = useState({ entidadeEmpregadora: '', valor: '' })
  const [error, setError] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [salvando, setSalvando] = useState(false)

  useEffect(() => {
    if (!usuario) return
    setForm({
      nome: usuario.nome || '',
      profissao: usuario.profissao || '',
      endereco: { ...enderecoVazio, ...(usuario.endereco || {}) },
    })
  }, [usuario])

  async function carregarRendimentos() {
    if (!ehCliente) return
    try {
      setRendimentos(await api.rendimentos())
    } catch (err) {
      setError(mensagemErro(err))
    }
  }

  useEffect(() => {
    carregarRendimentos()
  }, [ehCliente])

  function atualizar(event) {
    setForm((atual) => ({ ...atual, [event.target.name]: event.target.value }))
  }

  function atualizarEndereco(event) {
    setForm((atual) => ({
      ...atual,
      endereco: { ...atual.endereco, [event.target.name]: event.target.value },
    }))
  }

  async function salvarPerfil(event) {
    event.preventDefault()
    setError('')
    setSucesso('')
    setSalvando(true)
    try {
      const perfil = await api.updatePerfil(form)
      setUsuario(perfil)
      setSucesso('Perfil atualizado com sucesso.')
    } catch (err) {
      setError(mensagemErro(err))
    } finally {
      setSalvando(false)
    }
  }

  async function adicionarRendimento(event) {
    event.preventDefault()
    setError('')
    setSucesso('')
    try {
      await api.criarRendimento({
        entidadeEmpregadora: novoRendimento.entidadeEmpregadora,
        valor: Number(novoRendimento.valor),
      })
      setNovoRendimento({ entidadeEmpregadora: '', valor: '' })
      setSucesso('Rendimento adicionado.')
      await carregarRendimentos()
    } catch (err) {
      setError(mensagemErro(err))
    }
  }

  async function removerRendimento(id) {
    if (!window.confirm('Remover este rendimento?')) return
    setError('')
    try {
      await api.removerRendimento(id)
      setSucesso('Rendimento removido.')
      await carregarRendimentos()
    } catch (err) {
      setError(mensagemErro(err))
    }
  }

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <span className="eyebrow">Conta</span>
          <h1>Meu perfil</h1>
          <p className="muted">Dados cadastrais, endereço e informações de renda.</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {sucesso && <div className="alert alert-success">{sucesso}</div>}

      <div className="profile-grid">
        <section className="panel profile-summary">
          <div className="profile-avatar">
            {(usuario?.nome || 'AC')
              .split(' ')
              .filter(Boolean)
              .slice(0, 2)
              .map((parte) => parte[0])
              .join('')
              .toUpperCase()}
          </div>
          <h2>{usuario?.nome}</h2>
          <span className="badge badge-soft">{tipoLabel[usuario?.tipo] || usuario?.tipo}</span>
          <dl className="mini-list">
            <div>
              <dt>Usuário</dt>
              <dd>@{usuario?.username}</dd>
            </div>
            <div>
              <dt>Cadastro</dt>
              <dd>{formatarData(usuario?.dataCadastro)}</dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd>{usuario?.ativo ? 'Ativo' : 'Inativo'}</dd>
            </div>
          </dl>
        </section>

        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Dados da conta</h2>
              <p className="muted">Informações que identificam seu perfil na plataforma.</p>
            </div>
          </div>

          {ehCliente ? (
            <form className="form" onSubmit={salvarPerfil}>
              <div className="form-grid">
                <label>
                  Nome
                  <input name="nome" value={form.nome} onChange={atualizar} required />
                </label>
                <label>
                  Profissão
                  <input name="profissao" value={form.profissao} onChange={atualizar} />
                </label>
                <label>
                  CPF
                  <input value={usuario?.cpf || ''} disabled />
                </label>
                <label>
                  RG
                  <input value={usuario?.rg || ''} disabled />
                </label>
              </div>

              <div className="section-title">
                <div>
                  <h3>Endereço</h3>
                  <p className="muted">Atualize seu endereço quando necessário.</p>
                </div>
              </div>

              <div className="form-grid">
                <label>
                  Logradouro
                  <input
                    name="logradouro"
                    value={form.endereco.logradouro}
                    onChange={atualizarEndereco}
                  />
                </label>
                <label>
                  Número
                  <input name="numero" value={form.endereco.numero} onChange={atualizarEndereco} />
                </label>
                <label>
                  Complemento
                  <input
                    name="complemento"
                    value={form.endereco.complemento}
                    onChange={atualizarEndereco}
                  />
                </label>
                <label>
                  Bairro
                  <input name="bairro" value={form.endereco.bairro} onChange={atualizarEndereco} />
                </label>
                <label>
                  Cidade
                  <input name="cidade" value={form.endereco.cidade} onChange={atualizarEndereco} />
                </label>
                <label>
                  UF
                  <input
                    name="uf"
                    maxLength={2}
                    value={form.endereco.uf}
                    onChange={atualizarEndereco}
                  />
                </label>
                <label>
                  CEP
                  <input name="cep" value={form.endereco.cep} onChange={atualizarEndereco} />
                </label>
              </div>

              <div className="form-actions">
                <button type="submit" className="btn btn-primary" disabled={salvando}>
                  {salvando ? 'Salvando...' : 'Salvar perfil'}
                </button>
              </div>
            </form>
          ) : (
            <dl className="detail-list">
              <div>
                <dt>Nome</dt>
                <dd>{usuario?.nome}</dd>
              </div>
              <div>
                <dt>Razão social</dt>
                <dd>{usuario?.razaoSocial || '—'}</dd>
              </div>
              <div>
                <dt>CNPJ</dt>
                <dd>{usuario?.cnpj || '—'}</dd>
              </div>
              {usuario?.tipo === 'BANCO' && (
                <div>
                  <dt>Código do banco</dt>
                  <dd>{usuario?.codigoBanco || '—'}</dd>
                </div>
              )}
              <div>
                <dt>Permissões</dt>
                <dd>{usuario?.tipo === 'BANCO' ? 'Agente e banco' : 'Agente'}</dd>
              </div>
            </dl>
          )}
        </section>
      </div>

      {ehCliente && (
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Rendimentos</h2>
              <p className="muted">Até 3 fontes de renda podem ser vinculadas ao seu perfil.</p>
            </div>
            <span className="badge badge-soft">{rendimentos.length}/3</span>
          </div>

          {rendimentos.length === 0 ? (
            <div className="empty-state">
              <strong>Nenhum rendimento cadastrado.</strong>
              <span>Adicione uma fonte de renda para complementar sua análise.</span>
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Entidade empregadora</th>
                    <th>Valor mensal</th>
                    <th>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {rendimentos.map((rendimento) => (
                    <tr key={rendimento.id}>
                      <td>{rendimento.entidadeEmpregadora}</td>
                      <td>{formatarMoeda(rendimento.valor)}</td>
                      <td>
                        <button
                          type="button"
                          className="btn btn-small btn-danger-ghost"
                          onClick={() => removerRendimento(rendimento.id)}
                        >
                          Remover
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {rendimentos.length < 3 && (
            <form className="inline-form" onSubmit={adicionarRendimento}>
              <label>
                Entidade empregadora
                <input
                  value={novoRendimento.entidadeEmpregadora}
                  onChange={(event) =>
                    setNovoRendimento((atual) => ({
                      ...atual,
                      entidadeEmpregadora: event.target.value,
                    }))
                  }
                  required
                  placeholder="Empresa ou fonte de renda"
                />
              </label>
              <label>
                Valor mensal
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={novoRendimento.valor}
                  onChange={(event) =>
                    setNovoRendimento((atual) => ({ ...atual, valor: event.target.value }))
                  }
                  required
                  placeholder="0,00"
                />
              </label>
              <button type="submit" className="btn btn-primary">
                Adicionar
              </button>
            </form>
          )}
        </section>
      )}
    </div>
  )
}

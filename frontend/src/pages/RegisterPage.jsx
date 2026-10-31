import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { mensagemErro } from '../api'
import { useAuth } from '../auth'

const tipos = [
  { id: 'cliente', label: 'Cliente', descricao: 'Quero alugar um automóvel' },
  { id: 'empresa', label: 'Empresa', descricao: 'Gerencio automóveis e pedidos' },
  { id: 'banco', label: 'Banco', descricao: 'Analiso pedidos e concedo crédito' },
]

const enderecoInicial = {
  logradouro: '',
  numero: '',
  complemento: '',
  bairro: '',
  cidade: '',
  uf: '',
  cep: '',
}

const formInicial = {
  nome: '',
  username: '',
  senha: '',
  rg: '',
  cpf: '',
  profissao: '',
  cnpj: '',
  razaoSocial: '',
  codigoBanco: '',
}

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [tipo, setTipo] = useState('cliente')
  const [form, setForm] = useState(formInicial)
  const [endereco, setEndereco] = useState(enderecoInicial)
  const [rendimentos, setRendimentos] = useState([{ entidadeEmpregadora: '', valor: '' }])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function atualizar(event) {
    setForm((atual) => ({ ...atual, [event.target.name]: event.target.value }))
  }

  function atualizarEndereco(event) {
    setEndereco((atual) => ({ ...atual, [event.target.name]: event.target.value }))
  }

  function atualizarRendimento(index, campo, valor) {
    setRendimentos((atual) =>
      atual.map((rendimento, i) => (i === index ? { ...rendimento, [campo]: valor } : rendimento)),
    )
  }

  function adicionarRendimento() {
    if (rendimentos.length >= 3) return
    setRendimentos((atual) => [...atual, { entidadeEmpregadora: '', valor: '' }])
  }

  function removerRendimento(index) {
    setRendimentos((atual) => atual.filter((_, i) => i !== index))
  }

  async function enviar(event) {
    event.preventDefault()
    setError('')
    setLoading(true)

    try {
      let payload
      if (tipo === 'cliente') {
        payload = {
          nome: form.nome,
          username: form.username,
          senha: form.senha,
          rg: form.rg,
          cpf: form.cpf,
          profissao: form.profissao,
          endereco,
          rendimentos: rendimentos
            .filter(
              (rendimento) =>
                rendimento.entidadeEmpregadora.trim() !== '' && rendimento.valor !== '',
            )
            .map((rendimento) => ({
              entidadeEmpregadora: rendimento.entidadeEmpregadora,
              valor: Number(rendimento.valor),
            })),
        }
      } else {
        payload = {
          nome: form.nome,
          username: form.username,
          senha: form.senha,
          cnpj: form.cnpj,
          razaoSocial: form.razaoSocial,
          ...(tipo === 'banco' ? { codigoBanco: form.codigoBanco } : {}),
        }
      }

      await register(tipo, payload)
      navigate('/login', {
        state: { message: 'Cadastro realizado com sucesso. Agora faça login para continuar.' },
      })
    } catch (err) {
      setError(mensagemErro(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page register-page">
      <section className="auth-hero">
        <div className="auth-hero-content">
          <span className="eyebrow">Cadastro AlugaCar</span>
          <h1>Escolha como você participa da jornada.</h1>
          <p>
            Clientes alugam, empresas gerenciam a frota e bancos concedem o crédito. Crie sua
            conta no perfil certo e comece agora.
          </p>
          <div className="role-list">
            {tipos.map((item) => (
              <button
                key={item.id}
                type="button"
                className={`role-card ${tipo === item.id ? 'selected' : ''}`}
                onClick={() => {
                  setTipo(item.id)
                  setError('')
                }}
              >
                <strong>{item.label}</strong>
                <span>{item.descricao}</span>
              </button>
            ))}
          </div>
        </div>
        <div className="hero-car small" aria-hidden="true">
          <span />
        </div>
      </section>

      <section className="auth-panel">
        <div className="auth-card wide">
          <span className="eyebrow">Novo cadastro</span>
          <h2>Conta de {tipos.find((item) => item.id === tipo)?.label.toLowerCase()}</h2>

          {error && <div className="alert alert-error">{error}</div>}

          <form className="form" onSubmit={enviar}>
            <div className="form-grid">
              <label>
                Nome
                <input name="nome" value={form.nome} onChange={atualizar} required />
              </label>
              <label>
                Usuário
                <input
                  name="username"
                  value={form.username}
                  onChange={atualizar}
                  required
                  autoComplete="username"
                />
              </label>
              <label>
                Senha
                <input
                  type="password"
                  name="senha"
                  value={form.senha}
                  onChange={atualizar}
                  required
                  minLength={6}
                  autoComplete="new-password"
                />
              </label>

              {tipo === 'cliente' ? (
                <>
                  <label>
                    RG
                    <input name="rg" value={form.rg} onChange={atualizar} required />
                  </label>
                  <label>
                    CPF
                    <input name="cpf" value={form.cpf} onChange={atualizar} required />
                  </label>
                  <label>
                    Profissão
                    <input
                      name="profissao"
                      value={form.profissao}
                      onChange={atualizar}
                      placeholder="Ex.: Engenheira"
                    />
                  </label>
                </>
              ) : (
                <>
                  <label>
                    CNPJ
                    <input name="cnpj" value={form.cnpj} onChange={atualizar} required />
                  </label>
                  <label>
                    Razão social
                    <input
                      name="razaoSocial"
                      value={form.razaoSocial}
                      onChange={atualizar}
                      required
                    />
                  </label>
                  {tipo === 'banco' && (
                    <label>
                      Código do banco
                      <input
                        name="codigoBanco"
                        value={form.codigoBanco}
                        onChange={atualizar}
                        required
                      />
                    </label>
                  )}
                </>
              )}
            </div>

            {tipo === 'cliente' && (
              <>
                <div className="section-title">
                  <div>
                    <h3>Endereço</h3>
                    <p className="muted">Opcional, mas ajuda na análise.</p>
                  </div>
                </div>
                <div className="form-grid">
                  <label>
                    Logradouro
                    <input name="logradouro" value={endereco.logradouro} onChange={atualizarEndereco} />
                  </label>
                  <label>
                    Número
                    <input name="numero" value={endereco.numero} onChange={atualizarEndereco} />
                  </label>
                  <label>
                    Complemento
                    <input
                      name="complemento"
                      value={endereco.complemento}
                      onChange={atualizarEndereco}
                    />
                  </label>
                  <label>
                    Bairro
                    <input name="bairro" value={endereco.bairro} onChange={atualizarEndereco} />
                  </label>
                  <label>
                    Cidade
                    <input name="cidade" value={endereco.cidade} onChange={atualizarEndereco} />
                  </label>
                  <label>
                    UF
                    <input name="uf" maxLength={2} value={endereco.uf} onChange={atualizarEndereco} />
                  </label>
                  <label>
                    CEP
                    <input name="cep" value={endereco.cep} onChange={atualizarEndereco} />
                  </label>
                </div>

                <div className="section-title">
                  <div>
                    <h3>Rendimentos</h3>
                    <p className="muted">Até 3 rendimentos podem ser cadastrados.</p>
                  </div>
                  <button
                    type="button"
                    className="btn btn-ghost"
                    onClick={adicionarRendimento}
                    disabled={rendimentos.length >= 3}
                  >
                    + Adicionar
                  </button>
                </div>

                <div className="stack">
                  {rendimentos.map((rendimento, index) => (
                    <div className="inline-row" key={index}>
                      <label>
                        Entidade empregadora
                        <input
                          value={rendimento.entidadeEmpregadora}
                          onChange={(event) =>
                            atualizarRendimento(index, 'entidadeEmpregadora', event.target.value)
                          }
                          placeholder="Empresa ou fonte de renda"
                        />
                      </label>
                      <label>
                        Valor mensal
                        <input
                          type="number"
                          min="0.01"
                          step="0.01"
                          value={rendimento.valor}
                          onChange={(event) =>
                            atualizarRendimento(index, 'valor', event.target.value)
                          }
                          placeholder="0,00"
                        />
                      </label>
                      <button
                        type="button"
                        className="btn btn-danger-ghost"
                        onClick={() => removerRendimento(index)}
                        disabled={rendimentos.length === 1}
                      >
                        Remover
                      </button>
                    </div>
                  ))}
                </div>
              </>
            )}

            <button className="btn btn-primary btn-block" type="submit" disabled={loading}>
              {loading ? 'Criando conta...' : 'Criar conta'}
            </button>
          </form>

          <p className="auth-switch">
            Já tem conta? <Link to="/login">Entrar</Link>
          </p>
        </div>
      </section>
    </div>
  )
}

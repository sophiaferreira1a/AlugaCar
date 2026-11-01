# Documentação da API

Base URL local: `http://localhost:8080`. Todas as rotas, exceto `/api/auth/**`, exigem:

```http
Authorization: Bearer <token JWT>
```

## Formatos de erro

Erros de validação e de regras de negócio (`ProblemDetail`):

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "O cliente pode ter no máximo 3 rendimentos",
  "timestamp": "2026-11-01T12:00:00Z"
}
```

Em erros de validação de campos, o corpo também traz `errors` (`{ "campo": "mensagem" }`) e `detail` é `"Erro de validação"`.

Quando não há token válido, o filtro de segurança responde 401 em outro formato:

```json
{ "status": 401, "error": "Não autenticado", "message": "Informe um token JWT válido para acessar este recurso", "timestamp": "..." }
```

| Código | Quando |
|---|---|
| 200, 201, 204 | Sucesso (consulta/alteração, criação, remoção) |
| 400 | Validação de campos, corpo inválido ou regra de negócio violada (4º rendimento, transição de status inválida, datas invertidas) |
| 401 | Sem token, token inválido ou expirado, ou credenciais erradas |
| 403 | Perfil sem permissão, ou recurso de outro usuário (pedido de outro cliente, automóvel de outro proprietário) |
| 404 | Recurso inexistente |
| 409 | Conflito (username, CPF, CNPJ, placa, matrícula duplicados; automóvel com pedidos) |

## Autenticação

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/auth/register/cliente` | Cadastra cliente (201) |
| POST | `/api/auth/register/empresa` | Cadastra empresa (201) |
| POST | `/api/auth/register/banco` | Cadastra banco (201) |
| POST | `/api/auth/login` | Autentica e devolve o token (200) |

Cadastro de cliente (`profissao`, `endereco` e `rendimentos` são opcionais; no máximo 3 rendimentos):

```json
{
  "nome": "Maria Souza", "username": "maria", "senha": "senha123",
  "rg": "1234567", "cpf": "12345678901", "profissao": "Analista",
  "endereco": { "logradouro": "Rua A", "numero": "10", "cidade": "Belo Horizonte", "uf": "MG", "cep": "30000-000" },
  "rendimentos": [ { "entidadeEmpregadora": "Empresa X", "valor": 5000 } ]
}
```

Cadastro de empresa: `nome`, `username`, `senha`, `cnpj`, `razaoSocial`. Cadastro de banco: os mesmos campos e `codigoBanco`.

Login:

```json
// requisição
{ "username": "maria", "senha": "senha123" }
// resposta 200
{ "token": "eyJhbGciOi...", "usuario": { "id": "...", "nome": "Maria Souza", "tipo": "CLIENTE", "...": "..." } }
```

## Perfil (US03)

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| GET | `/api/perfil` | Autenticado | Dados do usuário logado (sem senha) |
| PUT | `/api/perfil` | Cliente | Atualiza `nome` (obrigatório), `profissao` e `endereco` |

## Rendimentos (US04, só cliente)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/rendimentos` | Lista os rendimentos do cliente |
| POST | `/api/rendimentos` | Adiciona (400 se já houver 3) |
| PUT | `/api/rendimentos/{id}` | Atualiza |
| DELETE | `/api/rendimentos/{id}` | Remove (204) |

Corpo: `{ "entidadeEmpregadora": "Empresa X", "valor": 5000 }` (valor maior que zero).

## Automóveis (US05)

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| GET | `/api/automoveis` | Autenticado | Lista todos |
| GET | `/api/automoveis/meus` | Autenticado | Lista os do usuário logado |
| GET | `/api/automoveis/{id}` | Autenticado | Consulta um automóvel |
| POST | `/api/automoveis` | Autenticado | Cadastra em nome do usuário logado (201) |
| PUT | `/api/automoveis/{id}` | Proprietário | Atualiza |
| DELETE | `/api/automoveis/{id}` | Proprietário | Remove (204; 409 se tiver pedidos) |

Corpo: `{ "matricula": "MAT123", "ano": 2024, "marca": "Toyota", "modelo": "Corolla", "placa": "ABC1D23" }`. A placa é convertida para maiúsculas.

## Pedidos de aluguel (US06 a US08 e US10)

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| POST | `/api/pedidos` | Cliente | Cria pedido `PENDENTE` (201) |
| GET | `/api/pedidos/meus` | Cliente | Lista os pedidos do cliente logado |
| GET | `/api/pedidos` | Empresa, banco | Lista os pedidos de todos os clientes |
| GET | `/api/pedidos/{id}` | Dono ou agente | Consulta um pedido (403 para outro cliente) |
| PUT | `/api/pedidos/{id}` | Cliente dono | Altera datas e observações (só `PENDENTE` ou `EM_ANALISE`) |
| PATCH | `/api/pedidos/{id}/cancelar` | Cliente dono | Cancela o pedido |
| PATCH | `/api/pedidos/{id}/status` | Empresa, banco | Avalia: `EM_ANALISE`, `APROVADO` ou `REPROVADO` |

Criação:

```json
{ "automovelId": "uuid-do-automovel", "dataInicio": "2026-11-10", "dataFim": "2026-11-15", "observacoes": "Retirada pela manhã" }
```

Avaliação:

```json
{ "status": "APROVADO", "observacoes": "Renda compatível" }
```

## Contratos de crédito (US09, só banco)

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/contratos` | Concede contrato a um pedido `APROVADO` (201); o pedido passa a `CONTRATO_EXECUTADO` |

```json
{ "pedidoId": "uuid-do-pedido", "valor": 18000.00, "taxaJuros": 1.8, "prazoMeses": 24 }
```

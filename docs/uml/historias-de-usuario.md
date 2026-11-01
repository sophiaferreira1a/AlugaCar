# Histórias de Usuário - Sistema de Aluguel de Carros

## US01 - Cadastro

**Como** visitante, **quero** me cadastrar como cliente, empresa ou banco informando usuário e senha, **para** poder usar o sistema.

**Critérios de aceite:**
- Usuário (username) único; duplicado retorna 409
- Senha de 6 a 100 caracteres, guardada com hash BCrypt
- Campos obrigatórios validados (400); CPF e CNPJ também são únicos
- O tipo de perfil é definido pela rota de cadastro (cliente, empresa ou banco)

## US02 - Login

**Como** usuário cadastrado, **quero** autenticar com usuário e senha, **para** acessar as funcionalidades do meu perfil.

**Critérios de aceite:**
- Credenciais válidas retornam um token JWT e os dados do perfil
- Credenciais inválidas retornam 401, sem detalhar o motivo
- Rotas protegidas exigem token válido; token inválido ou expirado retorna 401
- Usuário inativo não consegue autenticar

## US03 - Perfil do cliente

**Como** cliente, **quero** consultar e editar meus dados (nome, profissão e endereço), **para** manter meu cadastro atualizado para as análises.

**Critérios de aceite:**
- RG e CPF são obrigatórios no cadastro e o CPF é único
- Consulto e altero apenas o meu próprio perfil
- Endereço com logradouro, número, complemento, bairro, cidade, UF e CEP
- A senha nunca é devolvida pela API

## US04 - Rendimentos (limite de 3)

**Como** cliente, **quero** registrar entidade empregadora e valor dos meus rendimentos, **para** comprovar minha renda na análise financeira.

**Critérios de aceite:**
- Cada rendimento exige entidade empregadora e valor maior que zero
- Máximo de 3 rendimentos por cliente, no cadastro e depois dele
- O 4º é rejeitado com 400 e mensagem clara (regra no service)
- Posso editar e remover meus rendimentos; remover libera uma vaga

## US05 - Automóveis

**Como** usuário autenticado (cliente, empresa ou banco), **quero** cadastrar e manter meus automóveis com matrícula, ano, marca, modelo e placa, **para** que eles fiquem disponíveis para aluguel.

**Critérios de aceite:**
- Todos os campos são obrigatórios; matrícula e placa são únicas (409)
- O proprietário é o usuário logado (cliente, empresa ou banco)
- Só o proprietário altera ou remove o automóvel (403)
- Automóvel com pedidos não pode ser removido (409)

## US06 - Criar pedido

**Como** cliente, **quero** criar um pedido de aluguel escolhendo o automóvel e o período, **para** iniciar a análise financeira.

**Critérios de aceite:**
- Pedido nasce com status PENDENTE
- dataFim não pode ser anterior a dataInicio (400)
- Posso alterar o pedido enquanto ele estiver PENDENTE ou EM_ANALISE
- Só clientes criam pedidos (empresa e banco recebem 403)

## US07 - Cancelar pedido

**Como** cliente, **quero** cancelar um pedido meu, **para** desistir do aluguel quando não for mais necessário.

**Critérios de aceite:**
- Só o dono cancela; pedido de outro cliente retorna 403
- O status passa para CANCELADO e dataAtualizacao é atualizada
- REPROVADO, CANCELADO e CONTRATO_EXECUTADO não podem ser cancelados (400)
- O pedido não é apagado: continua na lista com o novo status

## US08 - Avaliar pedido

**Como** agente (empresa ou banco), **quero** avaliar financeiramente um pedido e registrar o parecer, **para** decidir se ele segue para o contrato.

**Critérios de aceite:**
- Listo os pedidos de todos os clientes
- Fluxo permitido: PENDENTE, EM_ANALISE, depois APROVADO ou REPROVADO
- Outras transições de status retornam 400
- Cliente não avalia pedido (403); posso registrar uma observação com o parecer

## US09 - Contrato de crédito

**Como** banco, **quero** conceder um contrato de crédito a um pedido aprovado, **para** financiar o aluguel do automóvel.

**Critérios de aceite:**
- Só em pedido APROVADO (400 nos demais); só o banco concede (403)
- Valor maior que zero, taxa de juros não negativa e prazo de pelo menos 1 mês
- No máximo um contrato por pedido; o pedido passa a CONTRATO_EXECUTADO
- O contrato é opcional: sem ele, o pedido aprovado permanece APROVADO

## US10 - Isolamento por cliente

**Como** cliente, **quero** consultar o status apenas dos meus pedidos, **para** ter segurança de que meus dados não são vistos por outros clientes.

**Critérios de aceite:**
- A lista dos meus pedidos traz só os do cliente autenticado
- Acessar, alterar ou cancelar pedido de outro cliente retorna 403
- A listagem geral de pedidos é exclusiva de agentes
- A verificação é feita no back-end (service), não apenas na tela


# Roteiro de Apresentação e Checklist de Entrega

## Checklist do enunciado

| Item exigido | Onde está | Situação |
|---|---|---|
| Spring Boot (Java 25) no back-end com arquitetura MVC | `backend/` | [ ] |
| React + Vite no front-end | `frontend/` | [ ] |
| Supabase (PostgreSQL) como banco | `docs/sql/schema.sql` | [ ] |
| Login com usuário e senha, acesso só para cadastrados | `security/`, `LoginPage` | [ ] |
| CRUD de cliente | `/api/perfil` e `/api/rendimentos` | [ ] |
| Protótipo: criar pedido e ver o status | tela Pedidos | [ ] |
| Diagrama de casos de uso | `docs/uml/casos-de-uso.png` | [ ] |
| Histórias de usuário | `docs/uml/historias-de-usuario.md` | [ ] |
| Diagrama de classes | `docs/uml/diagrama-classes.png` | [ ] |
| Diagrama de pacotes | `docs/uml/diagrama-pacotes.png` | [ ] |
| Todas as versões dos modelos UML no repositório | histórico do Git | [ ] |
| Modelos revisados conforme o feedback em aula | `docs/arquitetura.md`, seção 7 | [ ] |
| README no template do professor | `README.md` | [ ] |
| Repositório GitHub atualizado, sem credenciais | `.gitignore` | [ ] |

## Critérios de avaliação e como mostrar cada um

| Critério | O que mostrar |
|---|---|
| Qualidade do sistema (adequação aos requisitos) | Demo do fluxo completo: cadastro, login, perfil, pedido, avaliação, contrato |
| Alinhamento entre modelo e código | Diagrama de classes ao lado das entidades em `model/`; diagrama de pacotes ao lado da árvore `com.lab.alugacar` |
| Atualização dos modelos | Seção 7 de `docs/arquitetura.md` (evolução e ajustes da revisão final) |

## Roteiro da demonstração (8 a 10 minutos)

1. **Contexto (1 min):** problema, perfis (cliente, empresa e banco) e stack.
2. **Modelagem (2 min):** casos de uso, classes e pacotes; o que mudou da primeira versão para a final e por quê.
3. **Demo como cliente (3 min):** cadastro e login; perfil e rendimentos, tentando cadastrar o 4º; cadastrar um automóvel; criar pedido e ver `PENDENTE`; cancelar outro pedido.
4. **Demo como empresa e banco (2 min):** avaliar o pedido (`EM_ANALISE`, depois `APROVADO`); o banco concede o contrato; voltar ao cliente e mostrar `CONTRATO_EXECUTADO`.
5. **Isolamento e segurança (1 min):** acessar pedido de outro cliente (403) e rota sem token (401).
6. **Código (1 min):** `controller` -> `service` -> `repository` de um pedido, mostrando o MVC.

## Contas de demonstração

| Perfil | Username | Senha |
|---|---|---|
| Cliente | [PREENCHER] | [PREENCHER] |
| Empresa | [PREENCHER] | [PREENCHER] |
| Banco | [PREENCHER] | [PREENCHER] |

Crie as contas antes de apresentar, com pelo menos dois clientes e dois automóveis.

## Perguntas que o professor pode fazer

- Onde está a regra dos 3 rendimentos e por que ela fica no service?
- Como o sistema impede um cliente de ver o pedido de outro?
- Por que `Automovel` aponta para `Usuario` como proprietário?
- O que impede dois pedidos do mesmo carro no mesmo período? (hoje nada: ver "Limitações conhecidas" em `docs/arquitetura.md`)
- Por que JWT e não sessão? (O token fica no `localStorage` do navegador, em `frontend/src/api.js`.)
- O que mudou entre o diagrama inicial e o final?

package com.lab.alugacar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes das regras de negócio e das permissões da API (US01 a US10).
 * Rodam com o perfil "test" (H2 em memória), sem depender do Supabase.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegrasDeNegocioIntegrationTest extends ApiTestSupport {

    // ---------- US01 / US02: cadastro e login ----------

    @Test
    @DisplayName("US01: username duplicado retorna 409")
    void usernameDuplicadoDeveRetornarConflito() throws Exception {
        String username = "duplicado" + sufixo();
        postJson("/api/auth/register/cliente", payloadCliente(username), null, 201);

        postJson("/api/auth/register/cliente", payloadCliente(username), null, 409);
    }

    @Test
    @DisplayName("US01: senha com menos de 6 caracteres retorna 400")
    void senhaCurtaDeveSerRejeitada() throws Exception {
        Map<String, Object> payload = payloadCliente("curta" + sufixo());
        payload.put("senha", "123");

        postJson("/api/auth/register/cliente", payload, null, 400);
    }

    @Test
    @DisplayName("US02: senha errada retorna 401")
    void loginComSenhaErradaDeveRetornar401() throws Exception {
        Sessao cliente = novoCliente();

        postJson("/api/auth/login", Map.of(
                "username", cliente.username(),
                "senha", "senha-errada"
        ), null, 401);
    }

    @Test
    @DisplayName("US02: token inválido retorna 401")
    void tokenInvalidoDeveRetornar401() throws Exception {
        getJson("/api/perfil", "token.invalido.qualquer", 401);
    }

    @Test
    @DisplayName("US02: o perfil nunca expõe a senha")
    void perfilNaoDeveExporSenha() throws Exception {
        Sessao cliente = novoCliente();

        JsonNode perfil = getJson("/api/perfil", cliente.token(), 200);

        assertThat(perfil.has("senha")).isFalse();
        assertThat(perfil.get("tipo").asString()).isEqualTo("CLIENTE");
    }

    // ---------- US03 / US04: perfil e rendimentos ----------

    @Test
    @DisplayName("US03: cliente atualiza o próprio perfil")
    void clienteDeveAtualizarPerfil() throws Exception {
        Sessao cliente = novoCliente();

        JsonNode atualizado = putJson("/api/perfil", Map.of(
                "nome", "Nome Atualizado",
                "profissao", "Engenheira",
                "endereco", Map.of("cidade", "Contagem", "uf", "MG")
        ), cliente.token(), 200);

        assertThat(atualizado.get("nome").asString()).isEqualTo("Nome Atualizado");
        assertThat(atualizado.get("profissao").asString()).isEqualTo("Engenheira");
    }

    @Test
    @DisplayName("US03: empresa não pode usar a atualização de perfil de cliente")
    void empresaNaoDeveAtualizarPerfilDeCliente() throws Exception {
        Sessao empresa = novaEmpresa();

        putJson("/api/perfil", Map.of("nome", "Qualquer"), empresa.token(), 403);
    }

    @Test
    @DisplayName("US04 / RN03: o 4º rendimento é rejeitado")
    void naoDevePermitirQuartoRendimento() throws Exception {
        Sessao cliente = novoCliente();
        for (int i = 1; i <= 3; i++) {
            postJson("/api/rendimentos", Map.of(
                    "entidadeEmpregadora", "Empregador " + i,
                    "valor", 1000 * i
            ), cliente.token(), 201);
        }

        JsonNode erro = postJson("/api/rendimentos", Map.of(
                "entidadeEmpregadora", "Empregador 4",
                "valor", 4000
        ), cliente.token(), 400);

        assertThat(erro.get("detail").asString()).contains("máximo 3");
        assertThat(getJson("/api/rendimentos", cliente.token(), 200).size()).isEqualTo(3);
    }

    @Test
    @DisplayName("US04 / RN03: cadastro com 4 rendimentos é rejeitado")
    void cadastroComQuatroRendimentosDeveSerRejeitado() throws Exception {
        Map<String, Object> payload = payloadCliente("rend" + sufixo());
        payload.put("rendimentos", rendimentos(4));

        postJson("/api/auth/register/cliente", payload, null, 400);
    }

    @Test
    @DisplayName("US04: remover um rendimento libera uma vaga")
    void removerRendimentoDeveLiberarVaga() throws Exception {
        Sessao cliente = novoCliente();
        String primeiroId = null;
        for (int i = 1; i <= 3; i++) {
            JsonNode criado = postJson("/api/rendimentos", Map.of(
                    "entidadeEmpregadora", "Empregador " + i,
                    "valor", 1000 * i
            ), cliente.token(), 201);
            if (primeiroId == null) {
                primeiroId = criado.get("id").asString();
            }
        }

        deleteJson("/api/rendimentos/" + primeiroId, cliente.token(), 204);

        postJson("/api/rendimentos", Map.of(
                "entidadeEmpregadora", "Novo empregador",
                "valor", 2500
        ), cliente.token(), 201);
    }

    @Test
    @DisplayName("US04: rendimento com valor zero é rejeitado")
    void rendimentoComValorZeroDeveSerRejeitado() throws Exception {
        Sessao cliente = novoCliente();

        postJson("/api/rendimentos", Map.of(
                "entidadeEmpregadora", "Empregador",
                "valor", 0
        ), cliente.token(), 400);
    }

    // ---------- US05: automóveis ----------

    @Test
    @DisplayName("US05: placa duplicada retorna 409")
    void placaDuplicadaDeveRetornarConflito() throws Exception {
        Sessao cliente = novoCliente();
        String placa = novaPlaca();
        postJson("/api/automoveis", payloadAutomovel(placa), cliente.token(), 201);

        postJson("/api/automoveis", payloadAutomovel(placa), cliente.token(), 409);
    }

    @Test
    @DisplayName("US05: só o proprietário altera ou remove o automóvel")
    void soDonoDeveAlterarOuRemoverAutomovel() throws Exception {
        Sessao dono = novoCliente();
        Sessao outro = novoCliente();
        String automovelId = novoAutomovel(dono.token());

        putJson("/api/automoveis/" + automovelId, payloadAutomovel(novaPlaca()), outro.token(), 403);
        deleteJson("/api/automoveis/" + automovelId, outro.token(), 403);
        putJson("/api/automoveis/" + automovelId, payloadAutomovel(novaPlaca()), dono.token(), 200);
    }

    @Test
    @DisplayName("US05: automóvel com pedidos não pode ser removido")
    void naoDeveRemoverAutomovelComPedidos() throws Exception {
        Sessao dono = novoCliente();
        String automovelId = novoAutomovel(dono.token());
        novoPedido(dono.token(), automovelId);

        deleteJson("/api/automoveis/" + automovelId, dono.token(), 409);
    }

    @Test
    @DisplayName("US05: automóvel sem pedidos pode ser removido")
    void deveRemoverAutomovelSemPedidos() throws Exception {
        Sessao dono = novoCliente();
        String automovelId = novoAutomovel(dono.token());

        deleteJson("/api/automoveis/" + automovelId, dono.token(), 204);
        getJson("/api/automoveis/" + automovelId, dono.token(), 404);
    }

    @Test
    @DisplayName("US05: empresa também cadastra automóveis em seu nome")
    void empresaDeveCadastrarAutomovelEmSeuNome() throws Exception {
        Sessao empresa = novaEmpresa();

        JsonNode automovel = postJson("/api/automoveis", payloadAutomovel(novaPlaca()),
                empresa.token(), 201);

        assertThat(automovel.get("proprietarioTipo").asString()).isEqualTo("EMPRESA");
    }

    // ---------- US06 / US07: criar, alterar e cancelar pedido ----------

    @Test
    @DisplayName("US06: pedido novo nasce PENDENTE")
    void pedidoNovoDeveNascerPendente() throws Exception {
        Sessao cliente = novoCliente();
        String pedidoId = novoPedido(cliente.token(), novoAutomovel(cliente.token()));

        JsonNode pedido = getJson("/api/pedidos/" + pedidoId, cliente.token(), 200);

        assertThat(pedido.get("status").asString()).isEqualTo("PENDENTE");
    }

    @Test
    @DisplayName("US06 / RN06: dataFim anterior a dataInicio retorna 400")
    void naoDeveCriarPedidoComDatasInvertidas() throws Exception {
        Sessao cliente = novoCliente();
        String automovelId = novoAutomovel(cliente.token());
        Map<String, Object> payload = new HashMap<>();
        payload.put("automovelId", automovelId);
        payload.put("dataInicio", LocalDate.now().plusDays(10).toString());
        payload.put("dataFim", LocalDate.now().plusDays(5).toString());

        postJson("/api/pedidos", payload, cliente.token(), 400);
    }

    @Test
    @DisplayName("US06: só cliente cria pedido (empresa recebe 403)")
    void empresaNaoDeveCriarPedido() throws Exception {
        Sessao cliente = novoCliente();
        Sessao empresa = novaEmpresa();
        String automovelId = novoAutomovel(cliente.token());
        Map<String, Object> payload = new HashMap<>();
        payload.put("automovelId", automovelId);
        payload.put("dataInicio", LocalDate.now().plusDays(1).toString());
        payload.put("dataFim", LocalDate.now().plusDays(2).toString());

        postJson("/api/pedidos", payload, empresa.token(), 403);
    }

    @Test
    @DisplayName("US06: dono altera o pedido enquanto ele está pendente")
    void donoDeveAlterarPedidoPendente() throws Exception {
        Sessao cliente = novoCliente();
        String pedidoId = novoPedido(cliente.token(), novoAutomovel(cliente.token()));

        JsonNode atualizado = putJson("/api/pedidos/" + pedidoId, Map.of(
                "dataInicio", LocalDate.now().plusDays(2).toString(),
                "dataFim", LocalDate.now().plusDays(6).toString(),
                "observacoes", "Observação alterada"
        ), cliente.token(), 200);

        assertThat(atualizado.get("observacoes").asString()).isEqualTo("Observação alterada");
    }

    @Test
    @DisplayName("US07: cancelar um pedido pendente funciona e não pode ser repetido")
    void donoDeveCancelarPedidoPendenteUmaVez() throws Exception {
        Sessao cliente = novoCliente();
        String pedidoId = novoPedido(cliente.token(), novoAutomovel(cliente.token()));

        JsonNode cancelado = patchJson("/api/pedidos/" + pedidoId + "/cancelar", null,
                cliente.token(), 200);
        assertThat(cancelado.get("status").asString()).isEqualTo("CANCELADO");

        patchJson("/api/pedidos/" + pedidoId + "/cancelar", null, cliente.token(), 400);
    }

    @Test
    @DisplayName("US07: pedido cancelado não pode mais ser alterado")
    void naoDeveAlterarPedidoCancelado() throws Exception {
        Sessao cliente = novoCliente();
        String pedidoId = novoPedido(cliente.token(), novoAutomovel(cliente.token()));
        patchJson("/api/pedidos/" + pedidoId + "/cancelar", null, cliente.token(), 200);

        putJson("/api/pedidos/" + pedidoId, Map.of(
                "dataInicio", LocalDate.now().plusDays(2).toString(),
                "dataFim", LocalDate.now().plusDays(3).toString()
        ), cliente.token(), 400);
    }

    // ---------- US08: avaliação pelo agente ----------

    @Test
    @DisplayName("US08: agente percorre PENDENTE -> EM_ANALISE -> APROVADO")
    void agenteDeveAprovarPedido() throws Exception {
        Sessao cliente = novoCliente();
        Sessao empresa = novaEmpresa();
        String pedidoId = novoPedido(cliente.token(), novoAutomovel(cliente.token()));

        mudarStatus(pedidoId, "EM_ANALISE", empresa.token(), 200);
        mudarStatus(pedidoId, "APROVADO", empresa.token(), 200);

        JsonNode pedido = getJson("/api/pedidos/" + pedidoId, cliente.token(), 200);
        assertThat(pedido.get("status").asString()).isEqualTo("APROVADO");
    }

    @Test
    @DisplayName("US08: agente não pode pular etapas do status")
    void agenteNaoDevePularEtapas() throws Exception {
        Sessao cliente = novoCliente();
        Sessao empresa = novaEmpresa();
        String pedidoId = novoPedido(cliente.token(), novoAutomovel(cliente.token()));

        mudarStatus(pedidoId, "APROVADO", empresa.token(), 400);
        mudarStatus(pedidoId, "CANCELADO", empresa.token(), 400);
    }

    @Test
    @DisplayName("US08: cliente não pode avaliar pedido")
    void clienteNaoDeveAvaliarPedido() throws Exception {
        Sessao cliente = novoCliente();
        String pedidoId = novoPedido(cliente.token(), novoAutomovel(cliente.token()));

        mudarStatus(pedidoId, "EM_ANALISE", cliente.token(), 403);
    }

    @Test
    @DisplayName("US08: pedido reprovado não pode ser cancelado")
    void pedidoReprovadoNaoPodeSerCancelado() throws Exception {
        Sessao cliente = novoCliente();
        Sessao empresa = novaEmpresa();
        String pedidoId = novoPedido(cliente.token(), novoAutomovel(cliente.token()));
        mudarStatus(pedidoId, "EM_ANALISE", empresa.token(), 200);
        mudarStatus(pedidoId, "REPROVADO", empresa.token(), 200);

        patchJson("/api/pedidos/" + pedidoId + "/cancelar", null, cliente.token(), 400);
    }

    // ---------- US09: contrato de crédito ----------

    @Test
    @DisplayName("US09: contrato só é criado para pedido aprovado")
    void contratoSoParaPedidoAprovado() throws Exception {
        Sessao cliente = novoCliente();
        Sessao banco = novoBanco();
        String pedidoId = novoPedido(cliente.token(), novoAutomovel(cliente.token()));

        postJson("/api/contratos", payloadContrato(pedidoId), banco.token(), 400);
    }

    @Test
    @DisplayName("US09: só o banco concede contrato (empresa e cliente recebem 403)")
    void soBancoDeveCriarContrato() throws Exception {
        Sessao cliente = novoCliente();
        Sessao empresa = novaEmpresa();
        String pedidoId = novoPedido(cliente.token(), novoAutomovel(cliente.token()));

        postJson("/api/contratos", payloadContrato(pedidoId), empresa.token(), 403);
        postJson("/api/contratos", payloadContrato(pedidoId), cliente.token(), 403);
    }

    @Test
    @DisplayName("US09: contrato com valor zero é rejeitado")
    void contratoComValorInvalidoDeveSerRejeitado() throws Exception {
        Sessao cliente = novoCliente();
        Sessao empresa = novaEmpresa();
        Sessao banco = novoBanco();
        String pedidoId = novoPedido(cliente.token(), novoAutomovel(cliente.token()));
        mudarStatus(pedidoId, "EM_ANALISE", empresa.token(), 200);
        mudarStatus(pedidoId, "APROVADO", empresa.token(), 200);

        postJson("/api/contratos", Map.of(
                "pedidoId", pedidoId,
                "valor", 0,
                "taxaJuros", 1.5,
                "prazoMeses", 12
        ), banco.token(), 400);
    }

    @Test
    @DisplayName("US09: contrato aprovado leva o pedido a CONTRATO_EXECUTADO e só existe um por pedido")
    void contratoDeveExecutarPedidoEUmaSoVez() throws Exception {
        Sessao cliente = novoCliente();
        Sessao empresa = novaEmpresa();
        Sessao banco = novoBanco();
        String pedidoId = novoPedido(cliente.token(), novoAutomovel(cliente.token()));
        mudarStatus(pedidoId, "EM_ANALISE", empresa.token(), 200);
        mudarStatus(pedidoId, "APROVADO", empresa.token(), 200);

        postJson("/api/contratos", payloadContrato(pedidoId), banco.token(), 201);

        JsonNode pedido = getJson("/api/pedidos/" + pedidoId, cliente.token(), 200);
        assertThat(pedido.get("status").asString()).isEqualTo("CONTRATO_EXECUTADO");
        postJson("/api/contratos", payloadContrato(pedidoId), banco.token(), 400);
        patchJson("/api/pedidos/" + pedidoId + "/cancelar", null, cliente.token(), 400);
    }

    // ---------- US10: isolamento por cliente ----------

    @Test
    @DisplayName("US10 / RN04: cliente não vê nem cancela pedido de outro cliente")
    void clienteNaoDeveAcessarPedidoDeOutroCliente() throws Exception {
        Sessao dono = novoCliente();
        Sessao intruso = novoCliente();
        String pedidoId = novoPedido(dono.token(), novoAutomovel(dono.token()));

        getJson("/api/pedidos/" + pedidoId, intruso.token(), 403);
        patchJson("/api/pedidos/" + pedidoId + "/cancelar", null, intruso.token(), 403);
        putJson("/api/pedidos/" + pedidoId, Map.of(
                "dataInicio", LocalDate.now().plusDays(2).toString(),
                "dataFim", LocalDate.now().plusDays(3).toString()
        ), intruso.token(), 403);

        assertThat(getJson("/api/pedidos/meus", intruso.token(), 200).size()).isZero();
        assertThat(getJson("/api/pedidos/meus", dono.token(), 200).size()).isEqualTo(1);
    }

    @Test
    @DisplayName("US10: cliente não acessa a listagem geral de pedidos")
    void clienteNaoDeveListarTodosOsPedidos() throws Exception {
        Sessao cliente = novoCliente();

        getJson("/api/pedidos", cliente.token(), 403);
    }

    @Test
    @DisplayName("US10: agente enxerga os pedidos de todos os clientes")
    void agenteDeveVerPedidosDeTodosOsClientes() throws Exception {
        Sessao clienteA = novoCliente();
        Sessao clienteB = novoCliente();
        Sessao empresa = novaEmpresa();
        String pedidoA = novoPedido(clienteA.token(), novoAutomovel(clienteA.token()));
        String pedidoB = novoPedido(clienteB.token(), novoAutomovel(clienteB.token()));

        JsonNode todos = getJson("/api/pedidos", empresa.token(), 200);

        Set<String> ids = new HashSet<>();
        todos.forEach(pedido -> ids.add(pedido.get("id").asString()));
        assertThat(ids).contains(pedidoA, pedidoB);
        getJson("/api/pedidos/" + pedidoA, empresa.token(), 200);
    }
}

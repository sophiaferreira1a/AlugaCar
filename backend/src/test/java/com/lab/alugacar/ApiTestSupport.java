package com.lab.alugacar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Utilitários compartilhados pelos testes de integração da API.
 * As anotações do Spring ficam nas classes de teste concretas.
 */
abstract class ApiTestSupport {

    /** Usuário criado e autenticado durante o teste. */
    record Sessao(String username, String token) {
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ---------- HTTP ----------

    protected JsonNode getJson(String path, String token, int expectedStatus) throws Exception {
        return executar(get(path), null, token, expectedStatus);
    }

    protected JsonNode postJson(String path, Object body, String token, int expectedStatus)
            throws Exception {
        return executar(post(path), body, token, expectedStatus);
    }

    protected JsonNode putJson(String path, Object body, String token, int expectedStatus)
            throws Exception {
        return executar(put(path), body, token, expectedStatus);
    }

    protected JsonNode patchJson(String path, Object body, String token, int expectedStatus)
            throws Exception {
        return executar(patch(path), body, token, expectedStatus);
    }

    protected JsonNode deleteJson(String path, String token, int expectedStatus) throws Exception {
        return executar(delete(path), null, token, expectedStatus);
    }

    private JsonNode executar(
            MockHttpServletRequestBuilder request,
            Object body,
            String token,
            int expectedStatus
    ) throws Exception {
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body));
        }
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().is(expectedStatus))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        return content.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(content);
    }

    // ---------- Cadastro e login ----------

    protected Map<String, Object> payloadCliente(String username) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("nome", "Cliente " + username);
        payload.put("username", username);
        payload.put("senha", "senha123");
        payload.put("rg", "1234567");
        payload.put("cpf", cpfUnico());
        payload.put("profissao", "Analista");
        payload.put("endereco", Map.of("cidade", "Belo Horizonte", "uf", "MG"));
        return payload;
    }

    protected Sessao novoCliente() throws Exception {
        String username = "cliente" + sufixo();
        postJson("/api/auth/register/cliente", payloadCliente(username), null, 201);
        return login(username);
    }

    protected Sessao novaEmpresa() throws Exception {
        String username = "empresa" + sufixo();
        postJson("/api/auth/register/empresa", Map.of(
                "nome", "Empresa " + username,
                "username", username,
                "senha", "senha123",
                "cnpj", cnpjUnico(),
                "razaoSocial", "Empresa " + username + " LTDA"
        ), null, 201);
        return login(username);
    }

    protected Sessao novoBanco() throws Exception {
        String username = "banco" + sufixo();
        postJson("/api/auth/register/banco", Map.of(
                "nome", "Banco " + username,
                "username", username,
                "senha", "senha123",
                "cnpj", cnpjUnico(),
                "razaoSocial", "Banco " + username + " S.A.",
                "codigoBanco", "B" + sufixo()
        ), null, 201);
        return login(username);
    }

    protected Sessao login(String username) throws Exception {
        JsonNode resposta = postJson("/api/auth/login", Map.of(
                "username", username,
                "senha", "senha123"
        ), null, 200);
        return new Sessao(username, resposta.get("token").asString());
    }

    // ---------- Domínio ----------

    protected Map<String, Object> payloadAutomovel(String placa) {
        String sufixo = sufixo();
        Map<String, Object> payload = new HashMap<>();
        payload.put("matricula", "MAT" + sufixo);
        payload.put("ano", 2024);
        payload.put("marca", "Toyota");
        payload.put("modelo", "Corolla");
        payload.put("placa", placa);
        return payload;
    }

    protected String novaPlaca() {
        return ("T" + sufixo().substring(0, 6)).toUpperCase();
    }

    /** Cadastra um automóvel em nome do usuário do token e devolve o seu id. */
    protected String novoAutomovel(String token) throws Exception {
        JsonNode automovel = postJson("/api/automoveis", payloadAutomovel(novaPlaca()), token, 201);
        return automovel.get("id").asString();
    }

    /** Cria um pedido PENDENTE para o automóvel e devolve o seu id. */
    protected String novoPedido(String tokenCliente, String automovelId) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("automovelId", automovelId);
        payload.put("dataInicio", LocalDate.now().plusDays(1).toString());
        payload.put("dataFim", LocalDate.now().plusDays(4).toString());
        payload.put("observacoes", "Pedido de teste");
        JsonNode pedido = postJson("/api/pedidos", payload, tokenCliente, 201);
        return pedido.get("id").asString();
    }

    protected void mudarStatus(String pedidoId, String status, String tokenAgente, int esperado)
            throws Exception {
        patchJson("/api/pedidos/" + pedidoId + "/status", Map.of("status", status),
                tokenAgente, esperado);
    }

    protected Map<String, Object> payloadContrato(String pedidoId) {
        return Map.of(
                "pedidoId", pedidoId,
                "valor", 10000,
                "taxaJuros", 1.5,
                "prazoMeses", 12
        );
    }

    // ---------- Dados únicos ----------

    protected String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    protected String cpfUnico() {
        long numero = Math.abs(UUID.randomUUID().getMostSignificantBits() % 100_000_000_000L);
        return String.format("%011d", numero);
    }

    protected String cnpjUnico() {
        long numero = Math.abs(UUID.randomUUID().getMostSignificantBits() % 100_000_000_000_000L);
        return String.format("%014d", numero);
    }

    protected List<Map<String, Object>> rendimentos(int quantidade) {
        return java.util.stream.IntStream.rangeClosed(1, quantidade)
                .mapToObj(i -> Map.<String, Object>of(
                        "entidadeEmpregadora", "Empregador " + i,
                        "valor", 1000 * i))
                .toList();
    }
}

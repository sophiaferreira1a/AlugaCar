package com.lab.alugacar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlugaCarApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveExecutarFluxoCompletoDeAluguel() throws Exception {
        String sufixo = sufixo();
        String clienteUsername = "cliente" + sufixo;
        String empresaUsername = "empresa" + sufixo;
        String bancoUsername = "banco" + sufixo;

        JsonNode cliente = postJson("/api/auth/register/cliente", Map.of(
                "nome", "Cliente Integração",
                "username", clienteUsername,
                "senha", "senha123",
                "rg", "1234567",
                "cpf", cpfUnico(),
                "profissao", "Analista",
                "endereco", Map.of("cidade", "São Paulo", "uf", "SP"),
                "rendimentos", java.util.List.of(Map.of(
                        "entidadeEmpregadora", "Empresa X",
                        "valor", 5000
                ))
        ), null, 201);
        assertThat(cliente.get("tipo").asString()).isEqualTo("CLIENTE");

        JsonNode loginCliente = postJson("/api/auth/login", Map.of(
                "username", clienteUsername,
                "senha", "senha123"
        ), null, 200);
        String tokenCliente = loginCliente.get("token").asString();
        assertThat(tokenCliente).isNotBlank();

        JsonNode perfil = getJson("/api/perfil", tokenCliente, 200);
        assertThat(perfil.get("username").asString()).isEqualTo(clienteUsername);

        JsonNode automovel = postJson("/api/automoveis", Map.of(
                "matricula", "MAT" + sufixo,
                "ano", 2025,
                "marca", "Toyota",
                "modelo", "Corolla",
                "placa", placaUnica(sufixo)
        ), tokenCliente, 201);
        String automovelId = automovel.get("id").asString();

        Map<String, Object> pedidoRequest = new HashMap<>();
        pedidoRequest.put("automovelId", automovelId);
        pedidoRequest.put("dataInicio", LocalDate.now().plusDays(1).toString());
        pedidoRequest.put("dataFim", LocalDate.now().plusDays(4).toString());
        pedidoRequest.put("observacoes", "Pedido de teste");

        JsonNode pedido = postJson("/api/pedidos", pedidoRequest, tokenCliente, 201);
        String pedidoId = pedido.get("id").asString();
        assertThat(pedido.get("status").asString()).isEqualTo("PENDENTE");

        postJson("/api/auth/register/empresa", Map.of(
                "nome", "Empresa Integração",
                "username", empresaUsername,
                "senha", "senha123",
                "cnpj", cnpjUnico(),
                "razaoSocial", "Empresa Integração LTDA"
        ), null, 201);
        JsonNode loginEmpresa = postJson("/api/auth/login", Map.of(
                "username", empresaUsername,
                "senha", "senha123"
        ), null, 200);
        String tokenEmpresa = loginEmpresa.get("token").asString();

        JsonNode pedidosEmpresa = getJson("/api/pedidos", tokenEmpresa, 200);
        assertThat(pedidosEmpresa.isArray()).isTrue();

        JsonNode emAnalise = patchJson("/api/pedidos/" + pedidoId + "/status",
                Map.of("status", "EM_ANALISE"), tokenEmpresa, 200);
        assertThat(emAnalise.get("status").asString()).isEqualTo("EM_ANALISE");

        JsonNode aprovado = patchJson("/api/pedidos/" + pedidoId + "/status",
                Map.of("status", "APROVADO", "observacoes", "Crédito aprovado"), tokenEmpresa, 200);
        assertThat(aprovado.get("status").asString()).isEqualTo("APROVADO");

        postJson("/api/auth/register/banco", Map.of(
                "nome", "Banco Integração",
                "username", bancoUsername,
                "senha", "senha123",
                "cnpj", cnpjUnico(),
                "razaoSocial", "Banco Integração S.A.",
                "codigoBanco", "B" + sufixo
        ), null, 201);
        JsonNode loginBanco = postJson("/api/auth/login", Map.of(
                "username", bancoUsername,
                "senha", "senha123"
        ), null, 200);
        String tokenBanco = loginBanco.get("token").asString();

        JsonNode contrato = postJson("/api/contratos", Map.of(
                "pedidoId", pedidoId,
                "valor", 5000,
                "taxaJuros", 1.5,
                "prazoMeses", 12
        ), tokenBanco, 201);
        assertThat(contrato.get("valor").asDouble()).isEqualTo(5000.0);

        JsonNode pedidoFinal = getJson("/api/pedidos/" + pedidoId, tokenCliente, 200);
        assertThat(pedidoFinal.get("status").asString()).isEqualTo("CONTRATO_EXECUTADO");
        assertThat(pedidoFinal.get("contrato").isNull()).isFalse();
        assertThat(pedidoFinal.get("contrato").get("bancoNome").asString())
                .isEqualTo("Banco Integração");
    }

    @Test
    void deveBloquearAcessoSemToken() throws Exception {
        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveBloquearClienteNaListagemDeAgente() throws Exception {
        String sufixo = sufixo();
        String username = "cliente" + sufixo;

        postJson("/api/auth/register/cliente", Map.of(
                "nome", "Cliente Permissão",
                "username", username,
                "senha", "senha123",
                "rg", "7654321",
                "cpf", cpfUnico()
        ), null, 201);

        JsonNode login = postJson("/api/auth/login", Map.of(
                "username", username,
                "senha", "senha123"
        ), null, 200);

        mockMvc.perform(get("/api/pedidos")
                        .header("Authorization", "Bearer " + login.get("token").asString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveValidarCamposObrigatoriosNoCadastro() throws Exception {
        postJson("/api/auth/register/cliente", Map.of(
                "nome", "Sem dados"
        ), null, 400);
    }

    private JsonNode postJson(String path, Object body, String token, int expectedStatus)
            throws Exception {
        return executar(post(path), body, token, expectedStatus);
    }

    private JsonNode patchJson(String path, Object body, String token, int expectedStatus)
            throws Exception {
        return executar(patch(path), body, token, expectedStatus);
    }

    private JsonNode getJson(String path, String token, int expectedStatus) throws Exception {
        MockHttpServletRequestBuilder request = get(path);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().is(expectedStatus))
                .andReturn();
        return lerResposta(result);
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
        return lerResposta(result);
    }

    private JsonNode lerResposta(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        return content.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(content);
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String cpfUnico() {
        long numero = Math.abs(UUID.randomUUID().getMostSignificantBits() % 100_000_000_000L);
        return String.format("%011d", numero);
    }

    private String cnpjUnico() {
        long numero = Math.abs(UUID.randomUUID().getMostSignificantBits() % 100_000_000_000_000L);
        return String.format("%014d", numero);
    }

    private String placaUnica(String sufixo) {
        return ("T" + sufixo.substring(0, 6)).toUpperCase();
    }
}

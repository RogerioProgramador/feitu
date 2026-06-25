package com.feitu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feitu.dto.LoginRequest;
import com.feitu.dto.RegisterRequest;
import com.feitu.dto.WorkspaceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("/test-invite.sql")
class TarefaControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    String token;
    String workspaceId;
    String hoje = LocalDate.now().toString();

    @BeforeEach
    void setup() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new RegisterRequest("t@test.com", "senha1234", "TEST-CODE"))));

        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new LoginRequest("t@test.com", "senha1234"))))
                .andReturn();
        token = json.readTree(login.getResponse().getContentAsString()).get("token").asText();

        MvcResult ws = mvc.perform(post("/api/workspaces")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new WorkspaceRequest("WS", "#A7C7E7"))))
                .andReturn();
        workspaceId = json.readTree(ws.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void criarPontualEListarParaHoje() throws Exception {
        mvc.perform(post("/api/workspaces/" + workspaceId + "/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("nome", "Estudar Vue", "tipo", "PONTUAL"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("PONTUAL"))
                .andExpect(jsonPath("$.concluida").value(false));

        mvc.perform(get("/api/workspaces/" + workspaceId + "/tarefas")
                        .param("date", hoje)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void criarRecorrenteComDiasSemana() throws Exception {
        mvc.perform(post("/api/workspaces/" + workspaceId + "/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "nome", "Meditação",
                                "tipo", "RECORRENTE",
                                "diasSemana", List.of("SEG", "QUA", "SEX"),
                                "horario", "07:00"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("RECORRENTE"))
                .andExpect(jsonPath("$.diasSemana", hasItems("SEG", "QUA", "SEX")));
    }

    @Test
    void criarRecorrenteSemDiasRetorna422() throws Exception {
        mvc.perform(post("/api/workspaces/" + workspaceId + "/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "nome", "T",
                                "tipo", "RECORRENTE",
                                "diasSemana", List.of()
                        ))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void concluirPontualEVerificarConcluida() throws Exception {
        MvcResult criar = mvc.perform(post("/api/workspaces/" + workspaceId + "/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("nome", "T", "tipo", "PONTUAL"))))
                .andReturn();
        String id = json.readTree(criar.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(post("/api/tarefas/" + id + "/concluir")
                        .param("date", hoje)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluida").value(true));
    }

    @Test
    void reabrirPontualVoltaParaNaoConcluida() throws Exception {
        MvcResult criar = mvc.perform(post("/api/workspaces/" + workspaceId + "/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("nome", "T", "tipo", "PONTUAL"))))
                .andReturn();
        String id = json.readTree(criar.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(post("/api/tarefas/" + id + "/concluir")
                .param("date", hoje)
                .header("Authorization", "Bearer " + token));

        mvc.perform(post("/api/tarefas/" + id + "/reabrir")
                        .param("date", hoje)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluida").value(false));
    }

    @Test
    void pontualNaoAparececEmDataDiferente() throws Exception {
        mvc.perform(post("/api/workspaces/" + workspaceId + "/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("nome", "T", "tipo", "PONTUAL"))))
                .andReturn();

        String ontem = LocalDate.now().minusDays(1).toString();
        mvc.perform(get("/api/workspaces/" + workspaceId + "/tarefas")
                        .param("date", ontem)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}

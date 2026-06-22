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
    void cicloCriarIniciarPausarRetomarParar() throws Exception {
        // criar
        MvcResult criar = mvc.perform(post("/api/workspaces/" + workspaceId + "/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Outros"))
                .andExpect(jsonPath("$.estado").value("IDLE"))
                .andReturn();
        String id = json.readTree(criar.getResponse().getContentAsString()).get("id").asText();

        // iniciar
        mvc.perform(post("/api/tarefas/" + id + "/iniciar").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RUNNING"));

        // pausar
        mvc.perform(post("/api/tarefas/" + id + "/pausar").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.estado").value("PAUSED"));

        // retomar
        mvc.perform(post("/api/tarefas/" + id + "/iniciar").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.estado").value("RUNNING"));

        // parar
        mvc.perform(post("/api/tarefas/" + id + "/parar").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.estado").value("DONE"))
                .andExpect(jsonPath("$.tempoTotalSegundos", greaterThanOrEqualTo(0)));
    }

    @Test
    void reativarTarefaDone() throws Exception {
        MvcResult criar = mvc.perform(post("/api/workspaces/" + workspaceId + "/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();
        String id = json.readTree(criar.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(post("/api/tarefas/" + id + "/iniciar").header("Authorization", "Bearer " + token));
        mvc.perform(post("/api/tarefas/" + id + "/parar").header("Authorization", "Bearer " + token));

        mvc.perform(post("/api/tarefas/" + id + "/reativar").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.estado").value("IDLE"))
                .andExpect(jsonPath("$.concluidoEm").doesNotExist());
    }

    @Test
    void iniciarTarefaJaRodandoRetorna422() throws Exception {
        MvcResult criar = mvc.perform(post("/api/workspaces/" + workspaceId + "/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();
        String id = json.readTree(criar.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(post("/api/tarefas/" + id + "/iniciar").header("Authorization", "Bearer " + token));
        mvc.perform(post("/api/tarefas/" + id + "/iniciar").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void listarTarefasDoWorkspace() throws Exception {
        mvc.perform(post("/api/workspaces/" + workspaceId + "/tarefas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        mvc.perform(get("/api/workspaces/" + workspaceId + "/tarefas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}

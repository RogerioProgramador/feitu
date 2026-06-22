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

import java.time.LocalDate;

import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("/test-invite.sql")
class AnalyticsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    String token;
    String workspaceId;

    @BeforeEach
    void setup() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new RegisterRequest("an@test.com", "senha1234", "TEST-CODE"))));

        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new LoginRequest("an@test.com", "senha1234"))))
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
    void diaSemTarefasRetornaZeros() throws Exception {
        mvc.perform(get("/api/analytics/daily")
                        .param("date", LocalDate.now().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSegundos").value(0))
                .andExpect(jsonPath("$.porcWorkspace", hasSize(0)))
                .andExpect(jsonPath("$.timeline", hasSize(0)))
                .andExpect(jsonPath("$.tarefaMaisLonga").doesNotExist());
    }

    @Test
    void comTarefasRetornaTotalPositivo() throws Exception {
        // criar tarefa, iniciar e parar
        MvcResult criar = mvc.perform(post("/api/workspaces/" + workspaceId + "/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();
        String tarefaId = json.readTree(criar.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(post("/api/tarefas/" + tarefaId + "/iniciar").header("Authorization", "Bearer " + token));
        mvc.perform(post("/api/tarefas/" + tarefaId + "/parar").header("Authorization", "Bearer " + token));

        mvc.perform(get("/api/analytics/daily")
                        .param("date", LocalDate.now().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSegundos", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.porcWorkspace", hasSize(1)))
                .andExpect(jsonPath("$.tarefaMaisLonga").isNotEmpty());
    }

    @Test
    void semDateUsaHoje() throws Exception {
        mvc.perform(get("/api/analytics/daily")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(LocalDate.now().toString()));
    }
}

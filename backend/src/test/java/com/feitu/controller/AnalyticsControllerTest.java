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
class AnalyticsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    String token;
    String workspaceId;
    String hoje = LocalDate.now().toString();

    @BeforeEach
    void setup() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new RegisterRequest("an@test.com", "senha12345x", "TEST-CODE"))));

        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new LoginRequest("an@test.com", "senha12345x"))))
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
    void diaSemTarefasRetornaZerosEListasVazias() throws Exception {
        mvc.perform(get("/api/analytics/daily")
                        .param("date", hoje)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTarefas").value(0))
                .andExpect(jsonPath("$.concluidas").value(0))
                .andExpect(jsonPath("$.recorrentes", hasSize(0)))
                .andExpect(jsonPath("$.pontuais", hasSize(0)));
    }

    @Test
    void comTarefaPontualConcluidaRetornaConcluidas1() throws Exception {
        MvcResult criar = mvc.perform(post("/api/workspaces/" + workspaceId + "/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("nome", "Estudar Vue", "tipo", "PONTUAL"))))
                .andReturn();
        String tarefaId = json.readTree(criar.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(post("/api/tarefas/" + tarefaId + "/concluir")
                .param("date", hoje)
                .header("Authorization", "Bearer " + token));

        mvc.perform(get("/api/analytics/daily")
                        .param("date", hoje)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTarefas").value(1))
                .andExpect(jsonPath("$.concluidas").value(1))
                .andExpect(jsonPath("$.pontuais", hasSize(1)))
                .andExpect(jsonPath("$.pontuais[0].concluida").value(true));
    }

    @Test
    void semDateUsaHoje() throws Exception {
        mvc.perform(get("/api/analytics/daily")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(hoje));
    }

    @Test
    void novoShapeNaoContemCamposAntigos() throws Exception {
        mvc.perform(get("/api/analytics/daily")
                        .param("date", hoje)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSegundos").doesNotExist())
                .andExpect(jsonPath("$.porcWorkspace").doesNotExist())
                .andExpect(jsonPath("$.timeline").doesNotExist())
                .andExpect(jsonPath("$.tarefaMaisLonga").doesNotExist());
    }
}

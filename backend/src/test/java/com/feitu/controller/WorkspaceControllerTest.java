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
class WorkspaceControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    String token;

    @BeforeEach
    void setup() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new RegisterRequest("ws@test.com", "senha12345x", "TEST-CODE"))));

        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new LoginRequest("ws@test.com", "senha12345x"))))
                .andReturn();

        token = json.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void criarEListarWorkspace() throws Exception {
        mvc.perform(post("/api/workspaces")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new WorkspaceRequest("Trabalho", "#A7C7E7"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Trabalho"))
                .andExpect(jsonPath("$.ordem").value(1));

        mvc.perform(get("/api/workspaces").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void atualizarWorkspace() throws Exception {
        MvcResult criar = mvc.perform(post("/api/workspaces")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new WorkspaceRequest("Antigo", "#A7C7E7"))))
                .andReturn();

        String id = json.readTree(criar.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(put("/api/workspaces/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new WorkspaceRequest("Novo", "#B5EAD7"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo"));
    }

    @Test
    void deletarWorkspace() throws Exception {
        MvcResult criar = mvc.perform(post("/api/workspaces")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new WorkspaceRequest("Para deletar", "#A7C7E7"))))
                .andReturn();

        String id = json.readTree(criar.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(delete("/api/workspaces/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/workspaces").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/api/workspaces")).andExpect(status().isUnauthorized());
    }

    @Test
    void nomeEmBrancoRetorna400() throws Exception {
        mvc.perform(post("/api/workspaces")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new WorkspaceRequest("", "#A7C7E7"))))
                .andExpect(status().isBadRequest());
    }
}

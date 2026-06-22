package com.feitu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feitu.dto.LoginRequest;
import com.feitu.dto.RegisterRequest;
import com.feitu.dto.SubscribeRequest;
import com.feitu.dto.UnsubscribeRequest;
import com.feitu.repository.PushSubscriptionRepository;
import com.feitu.repository.UsuarioRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("/test-invite.sql")
class PushControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PushSubscriptionRepository subRepo;
    @Autowired UsuarioRepository usuarioRepo;

    String token;

    @BeforeEach
    void setup() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new RegisterRequest("push@test.com", "senha1234", "TEST-CODE"))));

        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new LoginRequest("push@test.com", "senha1234"))))
                .andReturn();
        token = json.readTree(login.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void subscribeCreatesPushSubscription() throws Exception {
        var req = new SubscribeRequest("https://push.example.com/sub/123",
                new SubscribeRequest.Keys("p256dh-key", "auth-key"));

        mvc.perform(post("/api/push/subscribe")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated());

        var uid = usuarioRepo.findByEmail("push@test.com").get().getId();
        assertThat(subRepo.findByUsuarioId(uid)).hasSize(1);
    }

    @Test
    void subscribeDuplicadoNaoDuplica() throws Exception {
        var req = new SubscribeRequest("https://push.example.com/sub/dup",
                new SubscribeRequest.Keys("p256dh-key", "auth-key"));

        mvc.perform(post("/api/push/subscribe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req)));
        mvc.perform(post("/api/push/subscribe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req)));

        var uid = usuarioRepo.findByEmail("push@test.com").get().getId();
        assertThat(subRepo.findByUsuarioId(uid)).hasSize(1);
    }

    @Test
    void unsubscribeRemoveSubscription() throws Exception {
        var req = new SubscribeRequest("https://push.example.com/sub/rem",
                new SubscribeRequest.Keys("p256dh-key", "auth-key"));

        mvc.perform(post("/api/push/subscribe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req)));

        mvc.perform(delete("/api/push/unsubscribe")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new UnsubscribeRequest("https://push.example.com/sub/rem"))))
                .andExpect(status().isNoContent());

        var uid = usuarioRepo.findByEmail("push@test.com").get().getId();
        assertThat(subRepo.findByUsuarioId(uid)).isEmpty();
    }
}

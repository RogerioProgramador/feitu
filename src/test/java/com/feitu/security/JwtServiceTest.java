package com.feitu.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    // 32+ chars para HMAC-SHA256
    private static final String SECRET = "test-secret-key-com-32-chars-ok!!";
    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        jwtService = new JwtService(SECRET, 3600_000L);
        userDetails = new User("user@test.com", "senha", List.of());
    }

    @Test
    void geraTokenEExtraiEmail() {
        String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@test.com");
    }

    @Test
    void tokenValidoRetornaTrue() {
        String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void tokenDeOutroUsuarioInvalido() {
        String token = jwtService.generateToken(userDetails);
        UserDetails outro = new User("outro@test.com", "senha", List.of());
        assertThat(jwtService.isTokenValid(token, outro)).isFalse();
    }

    @Test
    void tokenExpiradoLancaExcecao() {
        JwtService expirado = new JwtService(SECRET, -1L);
        String token = expirado.generateToken(userDetails);
        assertThatThrownBy(() -> expirado.isTokenValid(token, userDetails))
                .isInstanceOf(Exception.class);
    }
}

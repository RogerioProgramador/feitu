package com.feitu.service;

import com.feitu.config.BusinessException;
import com.feitu.domain.InviteCode;
import com.feitu.domain.RefreshToken;
import com.feitu.domain.Usuario;
import com.feitu.dto.AuthResponse;
import com.feitu.dto.LoginRequest;
import com.feitu.dto.RefreshRequest;
import com.feitu.dto.RegisterRequest;
import com.feitu.repository.InviteCodeRepository;
import com.feitu.repository.RefreshTokenRepository;
import com.feitu.repository.UsuarioRepository;
import com.feitu.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_FAILURES = 5;
    private static final long BLOCK_MS = 60_000;

    // long[0] = nº de falhas consecutivas, long[1] = timestamp da primeira falha da janela
    private final ConcurrentHashMap<String, long[]> loginFailures = new ConcurrentHashMap<>();

    @Value("${jwt.refresh-expiration-days:30}")
    private int refreshExpirationDays;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final InviteCodeRepository inviteCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            InviteCodeRepository inviteCodeRepository,
            RefreshTokenRepository refreshTokenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.inviteCodeRepository = inviteCodeRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        InviteCode invite = inviteCodeRepository
                .findByCodeIgnoreCaseAndAtivoTrue(req.codigoConvite())
                .orElseThrow(() -> new BusinessException("Código de convite inválido ou já utilizado"));

        if (usuarioRepository.existsByEmail(req.email())) {
            throw new BusinessException("E-mail já cadastrado");
        }

        invite.setAtivo(false);
        inviteCodeRepository.save(invite);

        Usuario usuario = new Usuario();
        usuario.setEmail(req.email());
        usuario.setSenhaHash(passwordEncoder.encode(req.senha()));
        usuarioRepository.save(usuario);

        UserDetails userDetails = userDetailsService.loadUserByUsername(req.email());
        String jwt = jwtService.generateToken(userDetails);
        String refreshToken = criarRefreshToken(req.email().toLowerCase());
        log.info("auth.register email={}", req.email().toLowerCase());
        return new AuthResponse(jwt, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        String key = req.email().toLowerCase();
        enforceLoginRateLimit(key);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.senha()));
        } catch (BadCredentialsException ex) {
            recordLoginFailure(key);
            log.warn("auth.login_failed email={} ip={}", key, getClientIp());
            throw new BusinessException("Credenciais inválidas");
        }
        loginFailures.remove(key);
        log.info("auth.login email={} ip={}", key, getClientIp());
        UserDetails userDetails = userDetailsService.loadUserByUsername(req.email());
        String jwt = jwtService.generateToken(userDetails);
        String refreshToken = criarRefreshToken(key);
        return new AuthResponse(jwt, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken token = refreshTokenRepository
                .findByTokenAndRevogadoFalse(req.refreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token inválido ou expirado"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            token.setRevogado(true);
            throw new BusinessException("Refresh token expirado");
        }

        // Rotação: invalida o token atual e emite novo par
        token.setRevogado(true);
        String email = token.getUsuarioEmail();
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String newJwt = jwtService.generateToken(userDetails);
        String newRefreshToken = criarRefreshToken(email);
        log.info("auth.refresh email={}", email);
        return new AuthResponse(newJwt, newRefreshToken);
    }

    @Transactional
    public void logout(RefreshRequest req) {
        refreshTokenRepository.findByTokenAndRevogadoFalse(req.refreshToken())
                .ifPresent(t -> {
                    t.setRevogado(true);
                    log.info("auth.logout email={}", t.getUsuarioEmail());
                });
    }

    private String criarRefreshToken(String email) {
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUsuarioEmail(email);
        token.setExpiresAt(LocalDateTime.now().plusDays(refreshExpirationDays));
        refreshTokenRepository.save(token);
        return token.getToken();
    }

    private void enforceLoginRateLimit(String email) {
        long[] state = loginFailures.get(email);
        if (state == null) return;
        if (System.currentTimeMillis() - state[1] > BLOCK_MS) {
            loginFailures.remove(email);
            return;
        }
        if (state[0] >= MAX_FAILURES) {
            throw new BusinessException("Muitas tentativas de login. Tente novamente em 1 minuto.");
        }
    }

    private void recordLoginFailure(String email) {
        loginFailures.compute(email, (k, prev) -> {
            long now = System.currentTimeMillis();
            if (prev == null || (now - prev[1]) > BLOCK_MS) {
                return new long[]{1L, now};
            }
            prev[0]++;
            return prev;
        });
    }

    private String getClientIp() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            String forwarded = attrs.getRequest().getHeader("X-Forwarded-For");
            return forwarded != null ? forwarded.split(",")[0].trim() : attrs.getRequest().getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }
}

package com.feitu.service;

import com.feitu.config.BusinessException;
import com.feitu.domain.InviteCode;
import com.feitu.domain.Usuario;
import com.feitu.dto.AuthResponse;
import com.feitu.dto.LoginRequest;
import com.feitu.dto.RegisterRequest;
import com.feitu.repository.InviteCodeRepository;
import com.feitu.repository.UsuarioRepository;
import com.feitu.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final int MAX_FAILURES = 5;
    private static final long BLOCK_MS = 60_000;

    // long[0] = nº de falhas consecutivas, long[1] = timestamp da primeira falha da janela
    private final ConcurrentHashMap<String, long[]> loginFailures = new ConcurrentHashMap<>();

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final InviteCodeRepository inviteCodeRepository;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            InviteCodeRepository inviteCodeRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.inviteCodeRepository = inviteCodeRepository;
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
        return new AuthResponse(jwtService.generateToken(userDetails));
    }

    public AuthResponse login(LoginRequest req) {
        String key = req.email().toLowerCase();
        enforceLoginRateLimit(key);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.senha()));
        } catch (BadCredentialsException ex) {
            recordLoginFailure(key);
            throw new BusinessException("Credenciais inválidas");
        }
        loginFailures.remove(key);
        UserDetails userDetails = userDetailsService.loadUserByUsername(req.email());
        return new AuthResponse(jwtService.generateToken(userDetails));
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
}

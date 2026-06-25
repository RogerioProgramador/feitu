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

@Service
public class AuthService {

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
            throw new BusinessException("E-mail já cadastrado: " + req.email());
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
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.senha()));
        } catch (BadCredentialsException ex) {
            // Relança como BusinessException para evitar que ExceptionTranslationFilter
            // intercepte BadCredentialsException antes do @RestControllerAdvice
            throw new BusinessException("Credenciais inválidas");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(req.email());
        return new AuthResponse(jwtService.generateToken(userDetails));
    }
}

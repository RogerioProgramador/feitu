# CLAUDE.md — Feitu

## Visão Geral

**Feitu** é um app de checklist diário com timer integrado. O usuário cria workspaces (abas), adiciona tarefas, cronometra cada uma e ao final do dia vê um resumo de tudo que fez e quanto tempo levou.

Repositório: `git@github.com:RogerioProgramador/feitu.git`

---

## Stack

| Camada     | Tecnologia                                      |
|------------|-------------------------------------------------|
| Backend    | Java 21 + Spring Boot 3.3 + Maven               |
| Banco      | H2 (file-based, `./data/feitu`)                 |
| Auth       | JWT (jjwt 0.12)                                 |
| Push       | `nl.martijndwars:web-push` + VAPID              |
| Frontend   | Vue 3 + Vite + TypeScript + Tailwind CSS        |
| PWA        | vite-plugin-pwa + Service Worker                |
| Deploy     | Fly.io (artefato único: Spring Boot serve dist/)|

---

## Arquitetura

O backend e frontend são empacotados num único `.jar`. O Maven Exec Plugin roda `npm run build` na fase `generate-resources`, e o resultado é copiado para `src/main/resources/static/`. Em produção, o Spring Boot serve o frontend estático e a API REST sob o mesmo processo.

### Layout de Pacotes (backend)

```
com.feitu/
├── FeituApplication.java       # main
├── config/                     # SecurityConfig, CorsConfig, JpaConfig
├── controller/                 # REST controllers (um por recurso)
├── domain/                     # Entidades JPA
├── dto/                        # Request/Response records
├── notification/               # PushService
├── repository/                 # Interfaces JPA
├── scheduler/                  # @Scheduled jobs
├── security/                   # JwtService, JwtFilter, UserDetailsServiceImpl
└── service/                    # Regras de negócio
```

### Layout Frontend

```
frontend/
├── src/
│   ├── api/          # clientes Axios por recurso
│   ├── components/   # componentes reutilizáveis
│   ├── composables/  # useAuth, useTimer, etc.
│   ├── pages/        # rotas Vue Router
│   ├── stores/       # Pinia stores
│   └── types/        # tipos TypeScript espelhando DTOs do backend
├── public/
└── vite.config.ts
```

---

## Modelo de Dados

```
Usuario          (id, email, senha_hash, criado_em, horario_notificacao)
Workspace        (id, nome, cor, ordem, usuario_id FK)
Tarefa           (id, nome, estado, workspace_id FK, criado_em, concluido_em)
SegmentoTempo    (id, tarefa_id FK, inicio, fim)
PushSubscription (id, usuario_id FK, endpoint, p256dh, auth)
```

**Estado de Tarefa:** `IDLE → RUNNING → PAUSED → DONE` (pode voltar de DONE para IDLE via reativação)

---

## Convenções

- **DTOs como records Java**: `record WorkspaceRequest(@NotBlank String nome, String cor) {}`
- **Erros**: lançar exceções específicas (`ResourceNotFoundException`, `BusinessException`), tratadas por `@RestControllerAdvice`
- **Usuário corrente**: injetado via `@AuthenticationPrincipal UserDetails` nos controllers
- **Testes de serviço**: Mockito puro (sem Spring context)
- **Testes de controller**: `@WebMvcTest` com MockMvc + `@MockBean` nos serviços
- **Testes de repositório**: `@DataJpaTest`

---

## Comandos

```bash
# Backend somente (sem build do frontend)
mvn compile -P backend-only
mvn spring-boot:run -P backend-only -Dspring-boot.run.profiles=dev

# Todos os testes
mvn verify -P backend-only

# Build completo (backend + frontend)
mvn package

# Frontend isolado
cd frontend && npm run dev        # dev server na porta 5173
cd frontend && npm run typecheck  # verificação de tipos
cd frontend && npm run test       # Vitest
```

---

## Critério de Done

Uma história está pronta quando:
1. `mvn verify -P backend-only` passa (compile + todos os testes)
2. `npm run typecheck` passa (sem erros de tipo)
3. A funcionalidade está manualmente verificável via API ou UI

---

## Variáveis de Ambiente (produção)

| Variável           | Descrição                            |
|--------------------|--------------------------------------|
| `JWT_SECRET`       | Chave HMAC-SHA256 (mín. 32 chars)    |
| `VAPID_PUBLIC_KEY` | Chave VAPID pública (base64url)      |
| `VAPID_PRIVATE_KEY`| Chave VAPID privada (base64url)      |

---

## Paleta Visual

| Nome       | Hex       | Uso                    |
|------------|-----------|------------------------|
| Background | `#F9F6F0` | Fundo geral            |
| Blue       | `#A7C7E7` | Workspaces / destaque  |
| Teal       | `#B5EAD7` | Tarefas concluídas     |
| Peach      | `#FFDAC1` | Timer ativo            |
| Lavender   | `#E2C6FF` | Analytics / gráficos   |

---

## Histórias

As histórias estão em `historias/` (não versionadas — listadas no `.gitignore`).
Sequência: E1 → E2 → E3 → E4 → E5 → E6 → E7 → E8 → E9 → E10 → E11 → E12 → E13

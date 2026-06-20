---
layout: default
title: Arquitetura
---

# Arquitetura

## Visão geral

O Feitu é empacotado como um **único artefato `.jar`**. O Maven compila o frontend Vue com `npm run build` durante o build, copia o `dist/` para `src/main/resources/static/`, e o Spring Boot serve tudo — frontend estático e API REST — no mesmo processo na porta 8080.

```
Browser / PWA
      │
      │  HTTPS (prod) / HTTP (dev proxy)
      ▼
┌─────────────────────────────┐
│   Spring Boot (porta 8080)  │
│                             │
│  ┌─────────────────────┐    │
│  │  /api/**  → REST    │    │
│  └─────────────────────┘    │
│  ┌─────────────────────┐    │
│  │  /**  → static Vue  │    │
│  └─────────────────────┘    │
│                             │
│  ┌─────────────────────┐    │
│  │  H2 file-based DB   │    │
│  │  ./data/feitu.mv.db │    │
│  └─────────────────────┘    │
└─────────────────────────────┘
```

Em desenvolvimento, o Vite roda na porta 5173 com proxy `/api → localhost:8080`, permitindo hot-reload do frontend sem rebuild do jar.

---

## Stack

| Camada      | Tecnologia                          | Versão  |
|-------------|-------------------------------------|---------|
| Backend     | Java + Spring Boot                  | 21 / 3.3.5 |
| ORM         | Spring Data JPA + Hibernate         | —       |
| Banco       | H2 (file-based em prod, in-memory em test) | 2.x |
| Auth        | Spring Security + JWT               | jjwt 0.12.6 |
| Push        | nl.martijndwars:web-push + VAPID    | 5.1.1   |
| Build       | Maven                               | 3.9+    |
| Frontend    | Vue 3 + Vite + TypeScript           | 3.5 / 8.x / 6.x |
| CSS         | Tailwind CSS                        | 3.4     |
| State       | Pinia                               | 3.x     |
| Routing     | Vue Router                          | 4.x     |
| HTTP client | Axios                               | 1.x     |
| PWA         | vite-plugin-pwa                     | 1.x     |
| Testes BE   | JUnit 5 + Mockito + MockMvc + H2    | —       |
| Testes FE   | Vitest + @vue/test-utils            | —       |

---

## Layout de pacotes — Backend

```
com.feitu/
├── FeituApplication.java          # main — spring boot entry point
├── config/
│   ├── SecurityConfig.java        # JWT filter, CORS, rotas públicas/protegidas
│   ├── GlobalExceptionHandler.java # @RestControllerAdvice → ProblemDetail
│   ├── WebConfig.java             # SPA fallback (index.html para rotas do Vue Router)
│   ├── BusinessException.java
│   └── ResourceNotFoundException.java
├── controller/
│   ├── AuthController.java        # /api/auth/register, /login
│   ├── WorkspaceController.java   # /api/workspaces
│   ├── TarefaController.java      # /api/workspaces/{id}/tarefas + /api/tarefas/{id}/*
│   ├── AnalyticsController.java   # /api/analytics/daily
│   ├── PushController.java        # /api/push/subscribe, /unsubscribe
│   └── UsuarioController.java     # /api/usuarios/me
├── domain/
│   ├── Usuario.java
│   ├── Workspace.java
│   ├── Tarefa.java
│   ├── TarefaEstado.java          # enum: IDLE, RUNNING, PAUSED, DONE
│   ├── SegmentoTempo.java
│   └── PushSubscription.java
├── dto/                           # todos são Java records
│   ├── RegisterRequest / LoginRequest / AuthResponse
│   ├── WorkspaceRequest / WorkspaceResponse / ReorderRequest
│   ├── TarefaRequest / TarefaResponse / RenomearRequest
│   ├── DailySummaryResponse / WorkspaceSummary / TimelineItem / TarefaResumo
│   ├── SubscribeRequest / UnsubscribeRequest
│   └── NotificacaoConfigRequest / UsuarioResponse
├── repository/
│   ├── UsuarioRepository
│   ├── WorkspaceRepository
│   ├── TarefaRepository
│   ├── SegmentoTempoRepository
│   └── PushSubscriptionRepository
├── security/
│   ├── JwtService.java            # geração e validação de tokens HMAC-SHA256
│   ├── JwtAuthFilter.java         # OncePerRequestFilter
│   └── UserDetailsServiceImpl.java
├── service/
│   ├── AuthService.java
│   ├── WorkspaceService.java
│   ├── TarefaService.java         # máquina de estados do timer
│   ├── SegmentoTempoService.java  # abrir/fechar segmentos, calcular total
│   ├── AnalyticsService.java
│   ├── PushSubscriptionService.java
│   └── UsuarioService.java
├── notification/
│   └── PushNotificationService.java  # VAPID, auto-remove subscriptions mortas
└── scheduler/
    └── DailyReviewScheduler.java     # @Scheduled por minuto, envia push no horário certo
```

---

## Layout de pacotes — Frontend

```
frontend/src/
├── main.ts                   # createApp + Pinia + Router + diretiva v-focus
├── App.vue                   # <RouterView /> apenas
├── style.css                 # @tailwind base/components/utilities + body
├── api/
│   ├── http.ts               # instância Axios: Bearer token + redirect 401
│   ├── workspaceApi.ts
│   ├── tarefaApi.ts
│   └── analyticsApi.ts
├── composables/
│   ├── useAuth.ts            # wrap do authStore com logout + router
│   └── useTimer.ts           # tick reativo: setInterval local quando RUNNING
├── components/
│   ├── TabBar.vue            # abas de workspace + criação inline
│   ├── TaskList.vue          # lista tarefas ativas + botão nova tarefa
│   ├── TaskItem.vue          # item com timer, botões de ação, edição de nome
│   └── CompletedSection.vue  # seção recolhível de tarefas concluídas
├── pages/
│   ├── LoginPage.vue
│   ├── RegisterPage.vue
│   ├── WorkspacePage.vue     # shell principal: header + TabBar + TaskList
│   └── AnalyticsView.vue     # resumo do dia: total, barras, timeline
├── router/
│   └── index.ts              # rotas + guard de autenticação
├── stores/
│   ├── authStore.ts          # token + email em localStorage
│   ├── workspaceStore.ts
│   └── tarefaStore.ts        # indexed por workspaceId
├── types/
│   └── index.ts              # interfaces TypeScript espelhando DTOs do backend
└── utils/
    ├── formatarTempo.ts      # segundos → "HH:MM:SS"
    └── formatarData.ts       # ISO → "DD/MM/YYYY", hojeISO()
```

---

## Modelo de dados

```
Usuario
  id               UUID  PK
  email            VARCHAR UNIQUE
  senha_hash       VARCHAR
  criado_em        TIMESTAMP
  horario_notif    TIME (default 22:00)
  ultima_notif_em  DATE (controle de deduplicação de push)

Workspace
  id          UUID  PK
  nome        VARCHAR
  cor         VARCHAR (hex, ex: #A7C7E7)
  ordem       INT
  usuario_id  UUID  FK → Usuario

Tarefa
  id            UUID  PK
  nome          VARCHAR
  estado        ENUM(IDLE, RUNNING, PAUSED, DONE)
  workspace_id  UUID  FK → Workspace
  criado_em     TIMESTAMP
  concluido_em  TIMESTAMP (null se não concluída)

SegmentoTempo
  id        UUID  PK
  tarefa_id UUID  FK → Tarefa
  inicio    TIMESTAMP WITH TIME ZONE
  fim       TIMESTAMP WITH TIME ZONE (null = segmento aberto / timer rodando)

PushSubscription
  id          UUID  PK
  usuario_id  UUID  FK → Usuario
  endpoint    VARCHAR(512)
  p256dh      VARCHAR
  auth        VARCHAR
  UNIQUE (usuario_id, endpoint)
```

---

## Segurança

- Todas as rotas `/api/**` são protegidas por JWT, exceto `/api/auth/**`, `/actuator/health`
- O filtro `JwtAuthFilter` extrai o token do header `Authorization: Bearer <token>` e popula o `SecurityContext`
- Sessão STATELESS (sem cookies, sem `HttpSession`)
- CORS configurado para `localhost:5173` (dev) e `*.fly.dev` (prod)
- Erros de autenticação retornam `401` via `unauthorizedEntryPoint` (sem redirect)
- Senhas com BCrypt

---

## Build integrado

O `pom.xml` tem dois profiles:

| Profile         | Comportamento                                              |
|-----------------|------------------------------------------------------------|
| (padrão)        | Executa `npm ci && npm run build` em `frontend/`, copia `dist/` para `static/` |
| `backend-only`  | Pula o build do frontend — para desenvolvimento e CI backend |

O `WebConfig.java` adiciona um fallback SPA: qualquer rota não encontrada em `static/` retorna `index.html`, permitindo que o Vue Router gerencie as rotas do lado do cliente.

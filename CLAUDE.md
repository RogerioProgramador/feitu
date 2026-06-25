# CLAUDE.md — Feitu

## Visão Geral

**Feitu** é um checklist diário de tarefas. O usuário organiza tarefas em workspaces (máx. 3), marca como feitas no dia, e vê um resumo ao final. Não há timer — a unidade é o checkbox.

Repositório: `git@github.com:RogerioProgramador/feitu.git`
Deploy: `https://feitu.fly.dev`

---

## Stack

| Camada   | Tecnologia                                            |
|----------|-------------------------------------------------------|
| Backend  | Java 21 + Spring Boot 3.3 + Maven                    |
| Banco    | H2 file-based, caminho via `DB_PATH` env (padrão `./data/feitu`) |
| Auth     | JWT (jjwt 0.12) + registro por invite code           |
| Push     | `nl.martijndwars:web-push` + VAPID                   |
| Frontend | Vue 3 + Vite + TypeScript + Tailwind CSS + Pinia     |
| PWA      | vite-plugin-pwa + Service Worker                     |
| Deploy   | Fly.io — JAR único, Spring Boot serve o `dist/`      |

---

## Arquitetura

Backend e frontend empacotados num único `.jar`. O Maven Exec Plugin roda `npm run build` em `generate-resources`; o `dist/` vai para `src/main/resources/static/`. Em produção, Spring Boot serve frontend estático e API REST no mesmo processo.

### Layout de Pacotes (backend)

```
com.feitu/
├── FeituApplication.java
├── config/          # SecurityConfig, CorsConfig, JpaConfig, BusinessException, ResourceNotFoundException
├── controller/      # WorkspaceController, TarefaController, AnalyticsController, AuthController, ...
├── domain/          # Usuario, Workspace, Tarefa, ConclusaoRecorrente, PushSubscription, InviteCode
├── dto/             # records Java (request/response)
├── notification/    # PushService
├── repository/      # interfaces JPA
├── scheduler/       # DailyReviewScheduler (notificações push)
├── security/        # JwtService, JwtFilter, UserDetailsServiceImpl
└── service/         # WorkspaceService, TarefaService, AnalyticsService, ...
```

### Layout Frontend

```
frontend/src/
├── api/          # clientes Axios (workspaceApi, tarefaApi, analyticsApi, ...)
├── components/   # TabBar, TaskList, TaskItem, CriacaoAvancadaSheet, ...
├── pages/        # LoginPage, RegisterPage, WorkspacePage, AnalyticsView, SettingsPage
├── stores/       # workspaceStore, tarefaStore, settingsStore, authStore
└── types/        # tipos TypeScript espelhando DTOs do backend
```

---

## Modelo de Dados

```
Usuario             (id, email, senha_hash, criado_em, horario_notificacao)
InviteCode          (id, code, ativo)
Workspace           (id, nome, cor, ordem, usuario_id FK)   — máx 3 por usuário
Tarefa              (id, nome, descricao, tipo, diasSemana, horario, data,
                     concluida, concluida_em, criado_em, workspace_id FK)
ConclusaoRecorrente (id, tarefa_id FK, data, concluida_em)  — UNIQUE(tarefa_id, data)
PushSubscription    (id, usuario_id FK, endpoint, p256dh, auth)
```

**Tipos de tarefa:**
- `PONTUAL` — criada para um dia específico (`data = LocalDate`); `concluida` é flag booleana
- `RECORRENTE` — repete nos dias da semana configurados (`diasSemana = "SEG,QUA,SEX"`); conclusão registrada em `ConclusaoRecorrente`

**Tarefas do dia** (`date`):
- Pontuais: `tarefa.data = date`
- Recorrentes: `diasSemana LIKE '%DIA%'` E `criadoEm ≤ date` (convertido UTC → UTC-3 para comparação)

---

## Fluxo de UX

1. **Home (`/workspaces`)** — data grande, tabs de workspace (máx 3, sem `+`), lista de tarefas do dia dividida em Recorrentes / Pontuais
2. **Tarefa** — checkbox toggle; ⋮ abre popover com "Ver descrição" e "Excluir tarefa"; "Ver descrição" abre bottom sheet com edição inline de descrição
3. **Criar tarefa** — quick-add (nome + Enter) ou bottom sheet avançado (tipo, dias da semana, horário)
4. **Resumo (`/analytics`)** — estado A (parcial), B (tudo feito, animação spring), C (sem tarefas); navegação por data
5. **Configurações (`/settings`)** — WORKSPACES (criar/deletar com limite 3), TEMA (Claro/Escuro/Sistema), FUSO HORÁRIO, NOTIFICAÇÃO DIÁRIA, CONTA (sair)

---

## Convenções

- **DTOs como records Java**: `record WorkspaceRequest(@NotBlank String nome, String cor) {}`
- **Erros**: `ResourceNotFoundException` (404) e `BusinessException` (422), tratados por `@RestControllerAdvice`
- **Usuário corrente**: `@AuthenticationPrincipal UserDetails` nos controllers
- **Testes de serviço**: Mockito puro (sem Spring context)
- **Testes de controller**: `@WebMvcTest` + MockMvc + `@MockBean`
- **Testes de repositório**: `@DataJpaTest`

---

## Comandos

```bash
# Backend somente
mvn compile -P backend-only
mvn spring-boot:run -P backend-only -Dspring-boot.run.profiles=dev

# Testes
mvn verify -P backend-only

# Build completo (backend + frontend)
mvn package

# Frontend isolado
cd frontend && npm run dev        # dev server :5173, proxy /api → :8080
cd frontend && npm run typecheck  # vue-tsc --noEmit
cd frontend && npm run test       # Vitest
```

---

## Critério de Done

1. `mvn verify -P backend-only` — compile + todos os testes
2. `npm run typecheck` — zero erros de tipo
3. Funcionalidade verificável manualmente em produção

---

## Variáveis de Ambiente

| Variável            | Descrição                                          |
|---------------------|----------------------------------------------------|
| `JWT_SECRET`        | Chave HMAC-SHA256 (mín. 32 chars)                 |
| `VAPID_PUBLIC_KEY`  | Chave VAPID pública (base64url)                   |
| `VAPID_PRIVATE_KEY` | Chave VAPID privada (base64url)                   |
| `DB_PATH`           | Caminho do arquivo H2 (padrão: `./data/feitu`)    |

---

## Paleta Visual

| Token              | Hex       | Uso                               |
|--------------------|-----------|-----------------------------------|
| `feitu-bg`         | `#E8E4DC` | Fundo geral (light mode)         |
| `feitu-surface`    | `#F7F4EE` | Cards e inputs                   |
| `feitu-blue`       | `#A7C7E7` | Workspace dots / destaque suave  |
| `feitu-blue-deep`  | `#5E8BB6` | CTA, bordas ativas, arco do logo |
| `feitu-teal`       | `#B5EAD7` | Checkbox concluído               |
| `feitu-teal-deep`  | `#2F7D63` | Check SVG                        |
| `feitu-peach-deep` | `#E07B4F` | Excluir, checkmark do logo       |
| `feitu-text`       | `#36332E` | Texto principal                  |
| `night-bg`         | `#16161F` | Fundo dark mode                  |
| `night-card`       | `#262635` | Cards dark mode                  |

---

## Histórias

Em `historias/` (gitignored — documentação local). Sequência implementada:
E1 (modelo) → E2 (auth) → E3 (workspaces) → E4 (tarefas v2) → E5 (analytics) →
E6 (push) → E7 (scaffold) → E8 (auth UI) → E9 (home + tarefas) → E10 (resumo) →
E11 (configurações + dark mode) → E12 (CI/CD) → E13 (Fly.io)

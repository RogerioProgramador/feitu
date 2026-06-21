# Feitu

App de checklist diário com timer integrado. Crie workspaces, adicione tarefas, cronometre cada uma e veja ao final do dia quanto tempo você dedicou a cada coisa.

[![CI](https://github.com/RogerioProgramador/feitu/actions/workflows/ci.yml/badge.svg)](https://github.com/RogerioProgramador/feitu/actions/workflows/ci.yml)

---

## Funcionalidades

- **Workspaces** — organize tarefas em abas coloridas, reordene por drag ou setas
- **Tarefas com timer** — ciclo `IDLE → RUNNING → PAUSED → DONE`; tarefas concluídas podem ser reativadas
- **Segmentos de tempo** — cada início/pausa vira um segmento rastreado separadamente
- **Analytics diário** — total do dia, tempo por workspace (barra proporcional), tarefa mais longa e timeline completa; navegação por data
- **Push notifications** — lembrete diário no horário configurado pelo usuário via Web Push API (VAPID)
- **PWA instalável** — funciona como app nativo via Service Worker
- **Autenticação JWT** — registro/login com token sem estado

---

## Stack

| Camada     | Tecnologia                                      |
|------------|-------------------------------------------------|
| Backend    | Java 21 + Spring Boot 3.3 + Maven               |
| Banco      | H2 file-based (`./data/feitu`)                  |
| Auth       | JWT (jjwt 0.12)                                 |
| Push       | VAPID + Web Push API                            |
| Frontend   | Vue 3 + Vite + TypeScript + Tailwind CSS        |
| PWA        | vite-plugin-pwa + Service Worker                |
| Deploy     | Fly.io (artefato único: Spring Boot serve dist/)|

---

## Arquitetura

Backend e frontend são empacotados num único `.jar`. O Maven Exec Plugin roda `npm run build` na fase `generate-resources` e o resultado vai para `src/main/resources/static/`. Em produção, o Spring Boot serve o frontend estático e a API REST sob o mesmo processo na porta 8080.

```
POST   /api/auth/register          Cadastro
POST   /api/auth/login             Login → JWT

GET    /api/workspaces             Listar workspaces
POST   /api/workspaces             Criar workspace
PUT    /api/workspaces/{id}        Editar workspace
PUT    /api/workspaces/reorder     Reordenar
DELETE /api/workspaces/{id}        Deletar

GET    /api/workspaces/{id}/tarefas   Listar tarefas
POST   /api/workspaces/{id}/tarefas   Criar tarefa
PATCH  /api/tarefas/{id}/nome         Renomear
POST   /api/tarefas/{id}/iniciar      Iniciar timer
POST   /api/tarefas/{id}/pausar       Pausar timer
POST   /api/tarefas/{id}/parar        Concluir (DONE)
POST   /api/tarefas/{id}/reativar     Reativar (DONE → IDLE)
DELETE /api/tarefas/{id}              Deletar

GET    /api/analytics/daily?date=     Resumo diário

GET    /api/usuarios/me               Perfil do usuário
PUT    /api/usuarios/me/notificacao   Configurar horário de notificação

POST   /api/push/subscribe            Registrar dispositivo para push
DELETE /api/push/unsubscribe          Remover registro
```

---

## Rodando localmente

**Pré-requisitos:** Java 21+, Node.js 20+, Maven 3.9+

```bash
# Backend (porta 8080)
mvn spring-boot:run -P backend-only -Dspring-boot.run.profiles=dev

# Frontend (porta 5173, em outro terminal)
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:5173`. O Vite faz proxy de `/api` para o backend.

---

## Testes

```bash
# Backend (compile + todos os testes)
mvn verify -P backend-only

# Frontend
cd frontend && npm run typecheck && npm run test
```

---

## Build de produção

```bash
# Gera target/feitu-0.0.1-SNAPSHOT.jar com frontend embutido
mvn package
java -jar target/feitu-0.0.1-SNAPSHOT.jar
```

---

## Deploy

```bash
flyctl auth login
flyctl apps create feitu
flyctl volumes create feitu_data --region gru --size 1
npx web-push generate-vapid-keys
flyctl secrets set JWT_SECRET=... VAPID_PUBLIC_KEY=... VAPID_PRIVATE_KEY=...
git push origin main   # dispara deploy automático via GitHub Actions
```

Veja o [guia de deploy completo](docs/deploy.html) para detalhes.

---

## Variáveis de ambiente (produção)

| Variável            | Descrição                           |
|---------------------|-------------------------------------|
| `JWT_SECRET`        | Chave HMAC-SHA256 (mín. 32 chars)   |
| `VAPID_PUBLIC_KEY`  | Chave VAPID pública (base64url)     |
| `VAPID_PRIVATE_KEY` | Chave VAPID privada (base64url)     |

---

## Documentação

- [Como funciona](docs/como-funciona.html)
- [Arquitetura](docs/arquitetura.html)
- [API Reference](docs/api.html)
- [Desenvolvimento local](docs/desenvolvimento.html)
- [Deploy](docs/deploy.html)

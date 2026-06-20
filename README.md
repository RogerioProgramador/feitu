# Feitu

App de checklist diário com timer integrado. Crie workspaces, adicione tarefas, cronometre cada uma e veja ao final do dia quanto tempo você dedicou a cada coisa.

[![CI](https://github.com/RogerioProgramador/feitu/actions/workflows/ci.yml/badge.svg)](https://github.com/RogerioProgramador/feitu/actions/workflows/ci.yml)

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
# Backend
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

Veja o [guia de deploy completo](docs/deploy.md) ou siga o checklist rápido:

```bash
flyctl auth login
flyctl apps create feitu
flyctl volumes create feitu_data --region gru --size 1
npx web-push generate-vapid-keys
flyctl secrets set JWT_SECRET=... VAPID_PUBLIC_KEY=... VAPID_PRIVATE_KEY=...
git push origin main   # dispara deploy automático via GitHub Actions
```

---

## Documentação

Documentação completa disponível em: **https://rogeriioprogramador.github.io/feitu**

- [Como funciona](docs/como-funciona.md)
- [Arquitetura](docs/arquitetura.md)
- [API Reference](docs/api.md)
- [Desenvolvimento local](docs/desenvolvimento.md)
- [Deploy](docs/deploy.md)

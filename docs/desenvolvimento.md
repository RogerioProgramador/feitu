---
layout: default
title: Desenvolvimento local
---

# Desenvolvimento local

## Pré-requisitos

| Ferramenta | Versão mínima | Verificar                  |
|------------|---------------|----------------------------|
| Java       | 21            | `java -version`            |
| Maven      | 3.9           | `mvn -version`             |
| Node.js    | 20            | `node -v`                  |
| npm        | 10            | `npm -v`                   |

> O projeto foi desenvolvido com **Java 25** e Mockito em modo experimental (`-Dnet.bytebuddy.experimental=true` já configurado no `pom.xml`). Funciona normalmente com Java 21+.

---

## Setup inicial

```bash
# Clone o repositório
git clone git@github.com:RogerioProgramador/feitu.git
cd feitu

# Instale as dependências do frontend
cd frontend && npm install && cd ..
```

---

## Rodando em desenvolvimento

Você precisa de dois terminais:

**Terminal 1 — Backend:**
```bash
mvn spring-boot:run -P backend-only -Dspring-boot.run.profiles=dev
```

O Spring Boot sobe na porta `8080`. O H2 usa banco em arquivo `./data/feitu.mv.db` (criado automaticamente). O console do H2 fica acessível em `http://localhost:8080/h2-console` com `jdbc:h2:file:./data/feitu`.

**Terminal 2 — Frontend:**
```bash
cd frontend
npm run dev
```

O Vite sobe na porta `5173` com hot-reload. O proxy `/api → localhost:8080` está configurado em `vite.config.ts` — você faz chamadas para `/api/...` no frontend e elas chegam ao Spring Boot.

Acesse: **http://localhost:5173**

---

## Testes

**Backend:**
```bash
# Todos os testes (unitários + integração)
mvn verify -P backend-only

# Só compilar, sem testes
mvn compile -P backend-only
```

Os testes de integração usam H2 in-memory (perfil `test`, configurado em `src/test/resources/application-test.properties`) e `@DirtiesContext` para isolamento entre testes.

**Frontend:**
```bash
cd frontend

# Verificação de tipos TypeScript (sem build)
npm run typecheck

# Testes Vitest
npm run test
```

---

## Build do artefato único

```bash
# Compila frontend + backend, gera jar com tudo embutido
mvn package

# Rode localmente
java -jar target/feitu-0.0.1-SNAPSHOT.jar
```

O Maven Exec Plugin roda `npm ci && npm run build` em `frontend/` antes de compilar o Java. O resultado em `frontend/dist/` é copiado para `src/main/resources/static/` e embutido no jar.

---

## Critério de done

Uma funcionalidade está pronta quando:

1. `mvn verify -P backend-only` passa — compile + todos os testes
2. `npm run typecheck` passa — sem erros de tipo TypeScript
3. A funcionalidade está manualmente verificável via UI ou API

---

## Variáveis de ambiente (desenvolvimento)

Em desenvolvimento, valores padrão são usados automaticamente (definidos em `application.properties`):

| Variável              | Padrão em dev                       |
|-----------------------|-------------------------------------|
| `JWT_SECRET`          | `dev-secret-...` (32+ chars)        |
| `JWT_EXPIRATION_MS`   | `86400000` (24h)                    |
| `VAPID_PUBLIC_KEY`    | vazio (push desabilitado)           |
| `VAPID_PRIVATE_KEY`   | vazio (push desabilitado)           |
| `H2_CONSOLE_ENABLED`  | `false` (ative via `-Dspring.h2.console.enabled=true`) |

Para habilitar o console H2 em dev:
```bash
mvn spring-boot:run -P backend-only \
  -Dspring-boot.run.profiles=dev \
  -Dspring.h2.console.enabled=true
```

---

## Estrutura de testes

```
src/test/java/com/feitu/
├── security/
│   └── JwtServiceTest.java          # unitário — geração e validação de tokens
├── service/
│   ├── AuthServiceTest.java         # unitário — Mockito
│   ├── WorkspaceServiceTest.java    # unitário — Mockito
│   ├── TarefaServiceTest.java       # unitário — máquina de estados
│   ├── SegmentoTempoServiceTest.java
│   ├── AnalyticsServiceTest.java
│   └── DailyReviewSchedulerTest.java
└── controller/
    ├── AuthControllerTest.java      # integração — MockMvc + H2 in-memory
    └── WorkspaceControllerTest.java # integração — MockMvc + H2 in-memory

frontend/src/pages/
├── LoginPage.test.ts                # Vitest + @vue/test-utils
└── RegisterPage.test.ts
```

---

## Adicionando uma nova feature

1. **Defina o domínio** — entidade em `domain/`, repositório em `repository/`
2. **Implemente o serviço** em `service/` com testes unitários Mockito
3. **Exponha via controller** em `controller/` com testes MockMvc
4. **Adicione os tipos** em `frontend/src/types/index.ts`
5. **Crie a API client** em `frontend/src/api/`
6. **Implemente a store Pinia** em `frontend/src/stores/`
7. **Crie os componentes/páginas** em `frontend/src/components/` ou `pages/`
8. Verifique: `mvn verify -P backend-only` + `npm run typecheck` + `npm run test`

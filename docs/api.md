---
layout: default
title: API Reference
---

# API Reference

Base URL: `/api`

Todas as rotas (exceto `/auth/**`) exigem o header:
```
Authorization: Bearer <token>
```

Erros são retornados como [ProblemDetail (RFC 7807)](https://www.rfc-editor.org/rfc/rfc7807):
```json
{
  "type": "about:blank",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "mensagem de erro"
}
```

---

## Auth

### `POST /api/auth/register`

Cria uma nova conta.

**Body:**
```json
{
  "email": "usuario@exemplo.com",
  "senha": "minimo8chars"
}
```

**Resposta `201 Created`:**
```json
{
  "token": "eyJhbGci..."
}
```

**Erros:** `400` (validação), `422` (e-mail já cadastrado)

---

### `POST /api/auth/login`

Autentica e retorna o token.

**Body:**
```json
{
  "email": "usuario@exemplo.com",
  "senha": "minimo8chars"
}
```

**Resposta `200 OK`:**
```json
{
  "token": "eyJhbGci..."
}
```

**Erros:** `401` (credenciais inválidas)

---

## Workspaces

### `GET /api/workspaces`

Lista os workspaces do usuário autenticado, ordenados por `ordem`.

**Resposta `200 OK`:**
```json
[
  {
    "id": "uuid",
    "nome": "Trabalho",
    "cor": "#A7C7E7",
    "ordem": 1
  }
]
```

---

### `POST /api/workspaces`

Cria um novo workspace. A `ordem` é atribuída automaticamente (último+1).

**Body:**
```json
{
  "nome": "Trabalho",
  "cor": "#A7C7E7"
}
```

**Resposta `201 Created`:** objeto `WorkspaceResponse`

---

### `PUT /api/workspaces/{id}`

Atualiza nome e/ou cor de um workspace.

**Body:**
```json
{
  "nome": "Trabalho Remoto",
  "cor": "#B5EAD7"
}
```

**Resposta `200 OK`:** objeto `WorkspaceResponse`

**Erros:** `404` (não encontrado ou não pertence ao usuário)

---

### `PUT /api/workspaces/reorder`

Reordena os workspaces. O array `ids` define a nova ordem (posição 0 = primeiro).

**Body:**
```json
{
  "ids": ["uuid-b", "uuid-a", "uuid-c"]
}
```

**Resposta `204 No Content`**

---

### `DELETE /api/workspaces/{id}`

Remove o workspace e todas as suas tarefas e segmentos.

**Resposta `204 No Content`**

**Erros:** `404`

---

## Tarefas

### `GET /api/workspaces/{workspaceId}/tarefas`

Lista todas as tarefas do workspace (qualquer estado).

**Resposta `200 OK`:**
```json
[
  {
    "id": "uuid",
    "nome": "Revisar PRs",
    "estado": "IDLE",
    "tempoTotalSegundos": 0,
    "criadoEm": "2026-06-20T10:00:00",
    "concluidoEm": null
  }
]
```

`tempoTotalSegundos` é a soma de todos os segmentos fechados. Para tarefas `RUNNING`, o frontend incrementa localmente a cada segundo.

---

### `POST /api/workspaces/{workspaceId}/tarefas`

Cria uma nova tarefa. Se `nome` for omitido, o backend usa `"Nova tarefa"`.

**Body (opcional):**
```json
{
  "nome": "Escrever testes"
}
```

**Resposta `201 Created`:** objeto `TarefaResponse`

---

### `PATCH /api/tarefas/{id}/nome`

Renomeia uma tarefa.

**Body:**
```json
{
  "nome": "Escrever testes unitários"
}
```

**Resposta `200 OK`:** objeto `TarefaResponse`

---

### `POST /api/tarefas/{id}/iniciar`

Inicia ou retoma o timer. Transições válidas: `IDLE → RUNNING`, `PAUSED → RUNNING`.

**Resposta `200 OK`:** objeto `TarefaResponse`

**Erros:** `422` (estado inválido para esta transição)

---

### `POST /api/tarefas/{id}/pausar`

Pausa o timer. Transição válida: `RUNNING → PAUSED`.

**Resposta `200 OK`:** objeto `TarefaResponse`

**Erros:** `422`

---

### `POST /api/tarefas/{id}/parar`

Conclui a tarefa. Transições válidas: `RUNNING → DONE`, `PAUSED → DONE`.

**Resposta `200 OK`:** objeto `TarefaResponse` (com `concluidoEm` preenchido)

**Erros:** `422`

---

### `POST /api/tarefas/{id}/reativar`

Reabre uma tarefa concluída. Transição válida: `DONE → IDLE` (limpa `concluidoEm`).

**Resposta `200 OK`:** objeto `TarefaResponse`

**Erros:** `422`

---

### `DELETE /api/tarefas/{id}`

Remove a tarefa e todos os seus segmentos de tempo.

**Resposta `204 No Content`**

---

## Analytics

### `GET /api/analytics/daily`

Retorna o resumo do dia para o usuário autenticado.

**Query params:**

| Parâmetro | Tipo   | Obrigatório | Descrição                           |
|-----------|--------|-------------|-------------------------------------|
| `date`    | string | não         | `YYYY-MM-DD`. Padrão: hoje          |

**Resposta `200 OK`:**
```json
{
  "data": "2026-06-20",
  "totalSegundos": 14400,
  "porcWorkspace": [
    {
      "workspaceId": "uuid",
      "nome": "Trabalho",
      "cor": "#A7C7E7",
      "segundos": 9000
    },
    {
      "workspaceId": "uuid",
      "nome": "Estudos",
      "cor": "#B5EAD7",
      "segundos": 5400
    }
  ],
  "timeline": [
    {
      "tarefaId": "uuid",
      "tarefaNome": "Revisar PRs",
      "workspaceCor": "#A7C7E7",
      "inicio": "2026-06-20T09:00:00Z",
      "fim": "2026-06-20T10:30:00Z"
    }
  ],
  "tarefaMaisLonga": {
    "id": "uuid",
    "nome": "Revisar PRs",
    "segundos": 5400
  }
}
```

`porcWorkspace` vem ordenado do maior para o menor tempo. `timeline` vem em ordem cronológica de `inicio`. `tarefaMaisLonga` pode ser `null` se nenhuma tarefa foi concluída no dia.

---

## Push Notifications

### `POST /api/push/subscribe`

Registra uma subscription de push (idempotente — se já existe para o mesmo endpoint, atualiza).

**Body:**
```json
{
  "endpoint": "https://fcm.googleapis.com/fcm/send/...",
  "keys": {
    "p256dh": "BNcR...",
    "auth": "tBHI..."
  }
}
```

**Resposta `201 Created`**

---

### `DELETE /api/push/unsubscribe`

Remove a subscription do endpoint informado.

**Body:**
```json
{
  "endpoint": "https://fcm.googleapis.com/fcm/send/..."
}
```

**Resposta `204 No Content`**

---

## Usuário

### `GET /api/usuarios/me`

Retorna os dados do usuário autenticado.

**Resposta `200 OK`:**
```json
{
  "id": "uuid",
  "email": "usuario@exemplo.com",
  "horarioNotificacao": "22:00"
}
```

---

### `PUT /api/usuarios/me/notificacao`

Configura o horário da notificação push diária.

**Body:**
```json
{
  "horario": "21:30"
}
```

**Resposta `200 OK`:** objeto `UsuarioResponse`

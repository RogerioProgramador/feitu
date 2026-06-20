---
layout: default
title: Como funciona
---

# Como funciona

## Workspaces

Workspaces são abas de organização. Cada workspace tem um nome e uma cor, e contém suas próprias tarefas. Você pode ter quantos quiser — a sugestão é separar por contexto: Trabalho, Estudos, Projetos Pessoais, etc.

A ordem dos workspaces pode ser reorganizada arrastando as abas (na interface, a ordem é persistida no backend).

---

## Tarefas e o ciclo de vida do timer

Cada tarefa tem um **estado** que controla o que pode acontecer com ela:

```
        ┌──────────────────────────────┐
        │                              │
        ▼                              │  reativar
      IDLE ──▶ RUNNING ──▶ PAUSED     │
                  │           │        │
                  └─────┬─────┘        │
                        │ parar        │
                        ▼             │
                       DONE ──────────┘
```

| Estado    | Descrição                                      | Ações disponíveis       |
|-----------|------------------------------------------------|-------------------------|
| `IDLE`    | Tarefa criada, timer nunca iniciado            | Iniciar                 |
| `RUNNING` | Timer rodando agora                            | Pausar, Parar           |
| `PAUSED`  | Timer pausado, tempo acumulado preservado      | Retomar (→RUNNING), Parar|
| `DONE`    | Concluída, com `concluidoEm` registrado        | Reativar (→IDLE)        |

### Como o tempo é calculado

O backend não usa uma flag de "duração acumulada" — ele armazena **segmentos de tempo** individuais:

```
SegmentoTempo
  inicio: Instant   ← quando o timer foi iniciado/retomado
  fim:    Instant   ← quando foi pausado/parado (null = segmento aberto)
```

O `tempoTotalSegundos` retornado pela API é a soma de todos os segmentos fechados (`fim != null`) da tarefa. Um segmento com `fim = null` significa que o timer está rodando agora.

O frontend recebe o `tempoTotalSegundos` do servidor e usa um `setInterval` local para incrementar o display a cada segundo quando o estado é `RUNNING` — sem ficar fazendo polling ao servidor.

---

## Resumo diário (Analytics)

Ao acessar a tela de Resumo, o frontend consulta `GET /api/analytics/daily?date=YYYY-MM-DD`. O backend:

1. Busca todos os `SegmentoTempo` do usuário cujo `inicio` está dentro do dia solicitado (meia-noite a meia-noite no UTC-3)
2. Agrupa por workspace para calcular o tempo total em cada um
3. Identifica a tarefa com maior tempo acumulado
4. Monta a timeline em ordem cronológica de início

O frontend exibe:
- **Total do dia** em `HH:MM:SS`
- **Barras horizontais** por workspace (proporcionais ao tempo, em ordem decrescente)
- **Tarefa mais longa** com destaque
- **Timeline** cronológica com cor do workspace e horários início–fim

---

## Notificações push

O Feitu suporta Web Push Notifications via VAPID. O fluxo é:

```
1. Usuário abre o app → browser pede permissão de notificação
2. Se aceito → frontend gera uma PushSubscription via Service Worker
3. Frontend envia endpoint + chaves (p256dh, auth) para POST /api/push/subscribe
4. Backend persiste em PushSubscription associado ao usuário
5. Todo minuto, o scheduler verifica se algum usuário tem horário_notificacao == agora
6. Se sim, e se ainda não enviou hoje → envia a notificação push
7. A notificação aparece mesmo com o app fechado
```

O horário da notificação padrão é **22:00** e pode ser alterado em configurações (PUT `/api/usuarios/me/notificacao`).

Se as chaves VAPID não estiverem configuradas no servidor, o push é desabilitado silenciosamente — o resto do app funciona normalmente.

---

## PWA (Progressive Web App)

O frontend é configurado como PWA via `vite-plugin-pwa`. Isso permite:

- **Instalar** o app no celular/desktop como se fosse nativo (ícone na tela inicial)
- **Funcionar offline** para visualização (as páginas já visitadas ficam em cache via Service Worker com estratégia NetworkFirst para a API)
- **Receber notificações push** mesmo com o app fechado (via Service Worker)

O Service Worker usa a estratégia `NetworkFirst` para todas as chamadas `/api/` — tenta o servidor primeiro, cai para o cache se offline.

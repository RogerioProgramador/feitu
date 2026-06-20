---
layout: default
title: Feitu — Checklist com timer
---

# Feitu

**Feitu** é um app de checklist diário com timer integrado. A ideia é simples: ao invés de só marcar tarefas como feitas, você também cronometra cada uma — e ao final do dia tem um resumo real de onde foi o seu tempo.

---

## O que você pode fazer

- **Criar workspaces** para organizar tarefas por contexto (ex: Trabalho, Estudos, Projetos Pessoais)
- **Adicionar tarefas** com um clique dentro de cada workspace
- **Iniciar, pausar e parar** o timer de cada tarefa individualmente
- **Ver o resumo do dia**: total de horas, tempo por workspace, tarefa que consumiu mais tempo e timeline cronológica
- **Receber notificação push** no horário configurado lembrando de fazer o resumo do dia
- **Funciona como PWA**: instale no celular ou desktop direto pelo browser, funciona offline para visualização

---

## Fluxo básico

```
1. Crie um workspace  →  "Trabalho"
2. Adicione uma tarefa  →  "Revisar PRs"
3. Clique ▶ para iniciar o timer
4. Clique ⏸ para pausar (timer para, retoma de onde parou)
5. Clique ⏹ para concluir (tarefa vai para a seção "Concluídas")
6. Repita durante o dia
7. Acesse o Resumo para ver o que foi feito e quanto tempo levou
```

---

## Paleta visual

| Nome       | Cor                                                                 | Uso                     |
|------------|---------------------------------------------------------------------|-------------------------|
| Background | `#F9F6F0` ![](https://via.placeholder.com/12/F9F6F0/F9F6F0.png)   | Fundo geral             |
| Blue       | `#A7C7E7` ![](https://via.placeholder.com/12/A7C7E7/A7C7E7.png)   | Workspaces / destaque   |
| Teal       | `#B5EAD7` ![](https://via.placeholder.com/12/B5EAD7/B5EAD7.png)   | Tarefas concluídas      |
| Peach      | `#FFDAC1` ![](https://via.placeholder.com/12/FFDAC1/FFDAC1.png)   | Timer ativo             |
| Lavender   | `#E2C6FF` ![](https://via.placeholder.com/12/E2C6FF/E2C6FF.png)   | Analytics / gráficos    |

---

## Links rápidos

- [Como funciona em detalhe](como-funciona) — estados do timer, modelo de dados, notificações
- [Arquitetura técnica](arquitetura) — stack, pacotes, IPC, banco de dados
- [API Reference](api) — todos os endpoints REST documentados
- [Desenvolvimento local](desenvolvimento) — como rodar e contribuir
- [Deploy](deploy) — passo a passo para produção no Fly.io

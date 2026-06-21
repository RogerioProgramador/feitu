# Deploy última issue

Executa o ciclo completo de deploy para a issue mais recente com label `claude-working` no repositório `RogerioProgramador/feitu`:

1. Use `mcp__github__list_issues` para buscar issues abertas com label `claude-working`, ordenadas por `UPDATED_AT DESC`. Pegue a primeira (mais recente).

2. Use `mcp__github__issue_read` com `get_comments` para encontrar o comentário do bot `github-actions[bot]` que contém o nome da branch (formato `claude/issue-N-YYYYMMDD-HHMM`).

3. Use `mcp__github__list_pull_requests` para verificar se já existe um PR aberto para essa branch. Se existir, use o número dele. Se não existir, use `mcp__github__create_pull_request` para criar o PR com:
   - `head`: a branch encontrada no comentário
   - `base`: main
   - `title`: mesmo título da issue
   - `body`: "Closes #N" (N = número da issue)

4. Use `mcp__github__pull_request_read` com `get_check_runs` para monitorar o CI. Aguarde até todos os checks estarem `completed`. Se algum falhar, reporte o erro e pare.

5. Se CI passou, use `mcp__github__merge_pull_request` com `merge_method: squash`.

6. A issue fecha automaticamente via "Closes #N". Confirme ao usuário que o deploy foi concluído.

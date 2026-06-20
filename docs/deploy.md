---
layout: default
title: Deploy
---

# Deploy

O Feitu roda no **Fly.io** como um único container. O GitHub Actions faz build e deploy automaticamente a cada push para `main`.

---

## Pré-requisitos

- Conta no [Fly.io](https://fly.io)
- `flyctl` instalado:
  ```bash
  # Windows
  winget install Fly.io.flyctl

  # macOS/Linux
  curl -L https://fly.io/install.sh | sh
  ```
- Repo no GitHub com o secret `FLY_API_TOKEN` configurado (passo 5 abaixo)

---

## Checklist de primeiro deploy

### 1. Autenticar no Fly.io

```bash
flyctl auth login
```

Abre o browser para login. Necessário apenas uma vez por máquina.

---

### 2. Criar o app e o volume

```bash
# Cria o app (o nome "feitu" deve bater com fly.toml: app = "feitu")
flyctl apps create feitu

# Cria o volume persistente para o banco H2 (gru = São Paulo)
flyctl volumes create feitu_data --region gru --size 1
```

O banco H2 é armazenado no volume em `/data/feitu.mv.db`. O volume sobrevive a deploys e restarts.

---

### 3. Gerar chaves VAPID

As notificações push exigem um par de chaves VAPID gerado uma única vez:

```bash
npx web-push generate-vapid-keys
```

Saída:
```
Public Key:  BAxxx...
Private Key: yyy...
```

Guarde os dois valores.

---

### 4. Configurar os secrets no Fly.io

```bash
flyctl secrets set \
  JWT_SECRET="sua-string-aleatoria-com-minimo-32-chars" \
  VAPID_PUBLIC_KEY="coloque-a-public-key-aqui" \
  VAPID_PRIVATE_KEY="coloque-a-private-key-aqui"
```

Para gerar o `JWT_SECRET` localmente (PowerShell):
```powershell
[System.Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Max 256 }))
```

> **Nota:** Se não quiser notificações push, defina `VAPID_PUBLIC_KEY` e `VAPID_PRIVATE_KEY` como string vazia — o app desabilita push silenciosamente.

---

### 5. Configurar o token do Fly.io no GitHub

O workflow `deploy.yml` precisa de um token para fazer deploy:

```bash
# Gera um token com validade longa
flyctl tokens create deploy -x 999999h
```

Copie o valor e adicione ao GitHub:

1. Acesse o repositório no GitHub
2. **Settings → Secrets and variables → Actions**
3. **New repository secret**
   - Name: `FLY_API_TOKEN`
   - Value: (cole o token gerado)

---

### 6. Primeiro push — dispara o deploy

```bash
git push origin main
```

O workflow `deploy.yml` é acionado automaticamente. Acompanhe em:

- **GitHub → Actions → Deploy**
- `flyctl logs` no terminal

O workflow faz:
1. Checkout do código
2. Build do frontend (`npm ci && npm run build`)
3. Build do `.jar` com frontend embutido (`mvn package -DskipTests`)
4. `flyctl deploy --remote-only` (Fly.io constrói o Docker image remotamente)

---

### 7. Verificar que está no ar

```bash
# Health check
curl https://feitu.fly.dev/actuator/health
# Esperado: {"status":"UP"}

# Abrir no browser
flyctl open
```

---

## Deploys subsequentes

Qualquer `git push origin main` dispara o CI (`ci.yml`) e, se passar, o deploy (`deploy.yml`).

Para deploy manual:
```bash
mvn package -DskipTests   # (ou deixe o Actions fazer)
flyctl deploy
```

---

## Configuração do Fly.io (`fly.toml`)

```toml
app = "feitu"
primary_region = "gru"          # São Paulo

[[mounts]]
  source = "feitu_data"
  destination = "/data"         # onde o H2 persiste os dados

[[vm]]
  memory = "256mb"
  cpu_kind = "shared"
  cpus = 1
```

O container expõe a porta `8080`. O Fly.io redireciona 80→443 (HTTPS) automaticamente.

O health check é feito em `GET /actuator/health` a cada 30 segundos, com 60 segundos de grace period na inicialização.

---

## Limites de memória JVM

O `Dockerfile` define:
```
JAVA_OPTS="-Xmx128m -Xms64m -XX:+UseContainerSupport"
```

Com 256 MB de RAM no Fly.io, isso deixa ~128 MB para o sistema operacional e overhead do container. Se necessário, ajuste `memory` no `fly.toml` e `Xmx` no `Dockerfile`.

---

## Logs e debugging em produção

```bash
# Logs em tempo real
flyctl logs

# Acessar o console H2 (temporariamente, com cuidado)
flyctl secrets set H2_CONSOLE_ENABLED=true
# Acesse: https://feitu.fly.dev/h2-console
# JDBC URL: jdbc:h2:file:/data/feitu
# Depois de usar:
flyctl secrets unset H2_CONSOLE_ENABLED

# SSH no container
flyctl ssh console
```

---

## Habilitar GitHub Pages (para esta documentação)

1. No GitHub, acesse **Settings → Pages**
2. **Source:** Deploy from a branch
3. **Branch:** `main` → pasta `/docs`
4. Salve

A documentação ficará disponível em `https://rogeriioprogramador.github.io/feitu` em alguns minutos.

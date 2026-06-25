# Feitu — Frontend

Vue 3 + Vite + TypeScript + Tailwind CSS + Pinia.

## Desenvolvimento

```bash
npm install
npm run dev        # dev server :5173 (proxy /api → :8080)
npm run typecheck  # vue-tsc --noEmit
npm run test       # Vitest
npm run build      # build de produção (chamado pelo mvn package)
```

O build de produção é invocado automaticamente pelo Maven durante `mvn package`.
Para rodar localmente com backend, inicie o backend com `mvn spring-boot:run -P backend-only -Dspring-boot.run.profiles=dev` e então `npm run dev`.

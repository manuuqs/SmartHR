# SmartHR
# TFG — Microservicios para gestión de personal con IA (Spring AI + RAG)

Este repositorio contiene la arquitectura base del proyecto: backend (Spring Boot), microservicio IA Assistant, frontend (React + Vite) y base de datos PostgreSQL con PGVector, orquestados con Docker Compose.

## Estructura
```
backend/
ia-assistant/
frontend/
docs/
docker-compose.yml
```

## Prerrequisitos
- JDK 17
- Maven 3.9+
- Node.js 20+
- Docker + Docker Compose

## Puesta en marcha — Sprint 1
1. Construir backend y IA Assistant:
   ```bash
   mvn -q -f backend/pom.xml package
   mvn -q -f ia-assistant/pom.xml package
   ```
2. Construir frontend:
   ```bash
   cd frontend && npm ci && npm run build && cd ..
   ```
3. Levantar servicios:
   ```bash
   docker compose up -d --build
   ```
4. Endpoints:
    - Backend: http://localhost:8080
    - IA Assistant: http://localhost:9090
    - Frontend: http://localhost:3000
    - PostgreSQL: localhost:5432 (tfg_user/tfg_pass)

## Crear repositorio en GitHub (opción web)
1. Crea un repositorio vacío en GitHub (público o privado).
2. En local:
   ```bash
   git init
   git add .
   git commit -m "chore: inicializa estructura Sprint 1"
   git branch -M main
   git remote add origin https://github.com/<usuario>/<repo>.git
   git push -u origin main
   ```

## Convenciones
- Branches: `feat/*`, `fix/*`, `chore/*`
- Commits: [Conventional Commits](https://www.conventionalcommits.org/)
- PRs: revisión por pares cuando sea posible

## Próximos sprints
- Sprint 2: CRUD de gestión de personal
- Sprint 3: Frontend React
- Sprint 4: IA Assistant (Spring AI + RAG)
- Sprint 5: Despliegue y pruebas

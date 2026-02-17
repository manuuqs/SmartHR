# SmartHR — Personnel Management with AI Assistant

> **Bachelor's Thesis (TFG) · Universidad Internacional de La Rioja**  
> Microservices-based HR management application with a conversational AI assistant powered by Spring AI + RAG

[![Java](https://img.shields.io/badge/Java-17-blue?logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-RAG-green?logo=spring)](https://spring.io/projects/spring-ai)
[![React](https://img.shields.io/badge/React-Vite-61DAFB?logo=react)](https://vitejs.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://www.docker.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-PGVector-336791?logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](LICENSE)

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Services & Endpoints](#services--endpoints)
- [AI Assistant](#ai-assistant)
- [Security](#security)
- [Testing](#testing)
- [CI/CD Pipeline](#cicd-pipeline)
- [Database Schema](#database-schema)

---

## Overview

**SmartHR** is a modular HR management platform that goes beyond traditional CRUD applications by integrating a conversational AI assistant capable of answering complex natural language queries over internal company data.

The system is built on a **microservices architecture**, fully containerised with Docker, and uses **Spring AI + RAG (Retrieval-Augmented Generation)** to provide grounded, context-aware responses — without hallucinations.

| Traditional HR System | SmartHR |
|---|---|
| Fixed endpoints, exact-match filters | Natural language queries |
| Multiple requests for complex data | Single contextualised query |
| Technical API knowledge required | Intuitive chat interface |
| No semantic understanding | Semantic search via embeddings |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          smarthr_net                            │
│                                                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌───────────────────┐ │
│  │   Frontend   │    │   Backend    │    │   IA Assistant    │ │
│  │  React+Vite  │◄──►│ Spring Boot  │◄──►│  Spring AI + RAG  │ │
│  │  :3000       │    │  :8080       │    │  :9090            │ │
│  └──────────────┘    └──────┬───────┘    └────────┬──────────┘ │
│                             │                     │            │
│                      ┌──────▼─────────────────────▼──────────┐ │
│                      │         PostgreSQL + PGVector          │ │
│                      │               :5432                    │ │
│                      └────────────────────────────────────────┘ │
│                                                                 │
│  ┌──────────────┐                                               │
│  │    Ollama    │ ◄── llama3.2:3b · mxbai-embed-large          │
│  │  (LLM local) │                                               │
│  └──────────────┘                                               │
└─────────────────────────────────────────────────────────────────┘
```

The **IA Assistant** microservice runs independently and connects to the same PostgreSQL instance as the backend, using a dedicated `vector_store` table managed by PGVector for semantic indexing of all employee data.

---

## Features

### Core HR Management
- ✅ Full employee lifecycle management (create, edit, delete)
- ✅ Department, job position, skill and project catalogues
- ✅ Contract and compensation history per employee
- ✅ Leave request management (submit, approve, reject)
- ✅ Performance review tracking
- ✅ Role-based access control (RRHH / Employee)

### AI Assistant
- ✅ Natural language queries over internal HR data
- ✅ RAG with grounding — answers based on real data, not hallucinations
- ✅ Semantic search via PGVector embeddings
- ✅ Intelligent query routing (small talk / aggregation / RAG)
- ✅ Conversation memory within active session
- ✅ Dynamic filters (location, department, skills, projects)
- ✅ Access restricted by role (RRHH sees all; employees see their own data)
- ✅ Real-time incremental sync (upsert on create/update/delete)

### Developer Experience
- ✅ Fully dockerised — one command to run everything
- ✅ Swagger/OpenAPI documentation on backend
- ✅ CI/CD pipeline with GitHub Actions
- ✅ JaCoCo coverage reports (>80% target)
- ✅ MapStruct for entity↔DTO mapping
- ✅ JWT authentication with BCrypt password encoding

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring Data JPA, Spring Security |
| AI / RAG | Spring AI, Ollama (`llama3.2:3b`, `mxbai-embed-large`) |
| Vector Store | PostgreSQL + PGVector |
| Frontend | React 18, Vite, Tailwind CSS |
| Database | PostgreSQL 16 |
| Auth | JWT (JJWT), BCrypt |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Testing | JUnit 5, Mockito, JaCoCo |
| Build | Maven 3.9, npm |
| Infrastructure | Docker, Docker Compose, GitHub Actions |

---

## Project Structure

```
SmartHR/
├── backend/                  # Main Spring Boot microservice (port 8080)
│   └── src/main/java/
│       ├── domain/           # JPA entities
│       ├── web/controllers/  # REST controllers
│       ├── service/          # Business logic
│       ├── repository/       # Spring Data JPA repositories
│       ├── dto/              # Data Transfer Objects (MapStruct)
│       └── security/         # JWT filter, SecurityConfig, CORS
│
├── assistant/                # IA Assistant microservice (port 9090)
│   └── src/main/java/
│       ├── rag/              # RagService, sync & indexing logic
│       ├── router/           # SmartHRQueryRouter (SMALL_TALK / AGGREGATION / RAG)
│       ├── utils/            # Text normalisation, entity extraction
│       └── security/         # JWT validation
│
├── frontend/                 # React + Vite application (port 3000)
│   └── src/
│       ├── components/       # EmployeeCard, ThemeSwitch, forms…
│       └── pages/            # EmployeeDashboard, HRDashboard, Login
│
├── docker-compose.yml        # Full stack orchestration
├── .github/workflows/        # CI/CD pipeline (GitHub Actions)
└── README.md
```

---

## Prerequisites

| Tool | Version |
|---|---|
| JDK | 17+ |
| Maven | 3.9+ |
| Node.js | 20+ |
| Docker + Docker Compose | Latest stable |

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/manuuqs/SmartHR.git
cd SmartHR
```

### 2. Build the services

```bash
# Backend
mvn -q -f backend/pom.xml package -DskipTests

# IA Assistant
mvn -q -f assistant/pom.xml package -DskipTests

# Frontend
cd frontend && npm ci && npm run build && cd ..
```

### 3. Start all containers

```bash
docker compose up -d --build
```

This launches all services on `smarthr_net`:

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| IA Assistant | http://localhost:9090 |
| PostgreSQL | localhost:5432 |
| PgAdmin | http://localhost:5050 |

### 4. Pull AI models (first run only)

```bash
docker exec smarthr_ollama ollama pull llama3.2:3b
docker exec smarthr_ollama ollama pull mxbai-embed-large
```

### 5. Log in

Use the credentials configured during setup. Two roles are available:

- **ROLE_RRHH** — full access to all employees, projects and the AI assistant
- **ROLE_EMPLOYEE** — access restricted to own profile and personal AI queries

---

## Services & Endpoints

### Backend (`:8080`)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/login` | Authenticate and receive JWT |
| GET/POST/PUT/DELETE | `/api/employees/**` | Employee CRUD |
| GET | `/api/employees/me/full` | Full profile for authenticated employee |
| GET/POST/PUT/DELETE | `/api/projects/**` | Project management |
| GET/POST/PUT/DELETE | `/api/leave-requests/**` | Leave request management |
| PATCH | `/api/leave-requests/{id}/status` | Approve / Reject a leave request |
| GET | `/api/departments/**` | Department catalogue |
| GET | `/api/skills/**` | Skills catalogue |
| GET | `/public/completeRag` | Internal — full data snapshot for RAG sync |

Full interactive documentation available at **`/swagger-ui.html`**.

### IA Assistant (`:9090`)

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/assistant/chat` | ROLE_RRHH | Natural language query over all data |
| POST | `/api/assistant/chat/employee` | ROLE_EMPLOYEE | Query restricted to own data |
| POST | `/internal/rag/upsert-employee` | Internal | Sync employee on create/update |
| POST | `/internal/rag/upsert-leave-request` | Internal | Sync leave request |
| POST | `/internal/rag/delete-employee` | Internal | Remove employee from vector store |

---

## AI Assistant

The assistant uses **Retrieval-Augmented Generation (RAG)** to ensure every response is grounded in real data stored in the system.

### How it works

```
User query
    │
    ▼
SmartHRQueryRouter
    ├── SMALL_TALK  →  Direct greeting response
    ├── AGGREGATION →  Redirect to RRHH panel
    └── RAG         →  RagService
                            │
                            ├── Intent detection
                            ├── Query enrichment (rewriting)
                            ├── Entity extraction (names, locations, skills…)
                            ├── PGVector semantic search (with filters)
                            └── LLM generation with grounding context
                                        │
                                        ▼
                                Grounded response
```

### Supported query types

| Intent | Example |
|---|---|
| Employee by name | "¿Qué proyectos tiene Manuel Quijada?" |
| Skills + location | "Empleados con Kubernetes en Madrid" |
| Employee by project | "¿Quiénes trabajan en el Portal Web?" |
| Leave requests | "Solicitudes de ausencia pendientes" |
| Own data (employee role) | "¿Cuál es mi proyecto actual?" |

### Data synchronisation

On startup, the assistant indexes a full snapshot of the database into PGVector. After that, every create/update/delete in the backend triggers an incremental upsert to keep the vector store in sync — no full reindex needed.

---

## Security

Authentication is handled via **JWT** (JSON Web Tokens):

1. Client sends credentials to `POST /auth/login`
2. Backend returns a signed JWT containing username, roles and expiry
3. Every subsequent request must include `Authorization: Bearer <token>`
4. `JwtFilter` intercepts each request, validates the token and injects the security context
5. `SecurityConfig` enforces role-based access per endpoint

Password storage uses **BCrypt** hashing. CORS is configured to allow the React frontend at `http://localhost:3000`.

---

## Testing

```bash
# Run all backend tests
mvn test -f backend/pom.xml

# Run all assistant tests
mvn test -f assistant/pom.xml

# Generate JaCoCo coverage report
mvn verify -f backend/pom.xml
# Report: backend/target/site/jacoco/index.html
```

Coverage target: **≥ 80%** on critical components (`EmployeeService`, `RagService`, `JwtUtil`, `AssistantChatUtils`…).

---

## CI/CD Pipeline

The GitHub Actions pipeline runs automatically on every push to `main`:

```
Push to main
    │
    ├── Build Backend      (Maven)
    ├── Test Backend       (JUnit 5 + JaCoCo)
    ├── Build Assistant    (Maven)
    ├── Test Assistant     (JUnit 5 + JaCoCo)
    ├── Build Frontend     (npm + Vite)
    └── Docker Build       (all service images)
```

---

## Database Schema

The relational model is defined from the domain entities, enforcing referential integrity via foreign keys and unique constraints.

**Catalogues:** `departments` · `job_positions` · `skills`

**Core tables:** `employees` · `users` · `users_roles`

**N:M relations:** `employee_skills` · `assignments`

**HR records:** `contracts` · `compensations` · `leave_requests` · `performance_reviews`

**AI layer:** `vector_store` (PGVector — embeddings + metadata)

Key constraints:
- `employees.email` — UNIQUE
- `(employee_id, skill_id)` in `employee_skills` — UNIQUE
- `(employee_id, project_id)` in `assignments` — UNIQUE
- `job_position_id` in `assignments` references `job_positions` (role on project)

---

## Branch & Commit Conventions

- **Branches:** `feat/*` · `fix/*` · `chore/*`
- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/)

---

## Author


**Manuel Quijada Salas**  
TFG — Grado en Ingeniería Informática  
Universidad Internacional de La Rioja (UNIR)  
Director: Juan Agustín Fraile Nieto

---

## License

Distributed under the [Apache 2.0 License](LICENSE).
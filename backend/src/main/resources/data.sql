
-- =====================
-- DEPARTMENTS
-- =====================
INSERT INTO departments (name, description) VALUES
                                                ('Desarrollo', 'Departamento de desarrollo de software'),
                                                ('Marketing', 'Departamento de marketing y comunicación'),
                                                ('Recursos Humanos', 'Gestión del personal y contratación'),
                                                ('Data', 'Departamento de análisis de datos')
    ON CONFLICT (name) DO NOTHING;

-- =====================
-- JOB POSITIONS
-- =====================
INSERT INTO job_positions (title, description) VALUES
                                                   ('Backend Developer', 'Desarrollo backend'),
                                                   ('Frontend Developer', 'Desarrollo frontend'),
                                                   ('Data Scientist', 'Análisis de datos'),
                                                   ('DevOps Engineer', 'Automatización y despliegue'),
                                                   ('QA Tester', 'Pruebas y calidad'),
                                                   ('RRHH Manager', 'Gestión de personal y políticas')
    ON CONFLICT (title) DO NOTHING;

-- =====================
-- SKILLS
-- =====================
INSERT INTO skills (name, description) VALUES
                                           ('Java', 'Lenguaje Java'),
                                           ('Spring Boot', 'Framework Spring'),
                                           ('React', 'Frontend'),
                                           ('Docker', 'Contenedores'),
                                           ('Kubernetes', 'Orquestación'),
                                           ('Python', 'Lenguaje para análisis'),
                                           ('SQL', 'Consultas en bases de datos')
    ON CONFLICT (name) DO NOTHING;

-- =====================
-- EMPLOYEES
-- =====================
INSERT INTO employees (name, location, email, hire_date, department_id, job_position_id)
VALUES
    ('Manuel Quijada', 'Madrid', 'manuel@smarthr.dev', '2024-01-01',
     (SELECT id FROM departments WHERE name='Desarrollo'),
     (SELECT id FROM job_positions WHERE title='Backend Developer')),
    ('Julene Peña', 'Barcelona', 'julene@smarthr.dev', '2023-09-15',
     (SELECT id FROM departments WHERE name='Desarrollo'),
     (SELECT id FROM job_positions WHERE title='Frontend Developer')),
    ('Alfonso Sampedro', 'Madrid', 'alfonso@smarthr.dev', '2022-06-10',
     (SELECT id FROM departments WHERE name='Data'),
     (SELECT id FROM job_positions WHERE title='Data Scientist')),
    ('Laura Gómez', 'Valencia', 'laura@smarthr.dev', '2023-03-20',
     (SELECT id FROM departments WHERE name='Desarrollo'),
     (SELECT id FROM job_positions WHERE title='DevOps Engineer')),
    ('Carlos Ruiz', 'Sevilla', 'carlos@smarthr.dev', '2024-02-01',
     (SELECT id FROM departments WHERE name='Recursos Humanos'),
     (SELECT id FROM job_positions WHERE title='RRHH Manager'))
    ON CONFLICT (email) DO NOTHING;

-- =====================
-- USERS vinculados a EMPLOYEES
-- =====================
-- Contraseña encriptada genérica para pruebas
INSERT INTO users (username, password, employee_id)
SELECT 'MA001', '$2a$10$wrwoO0puF6HsqwuVDc4x4.RA1yiRfy0yQKn.v/aWZsTp.bKZSWUnu', id FROM employees WHERE email='manuel@smarthr.dev'
    ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, employee_id)
SELECT 'JU001', '$2a$10$wrwoO0puF6HsqwuVDc4x4.RA1yiRfy0yQKn.v/aWZsTp.bKZSWUnu', id FROM employees WHERE email='julene@smarthr.dev'
    ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, employee_id)
SELECT 'AL001', '$2a$10$wrwoO0puF6HsqwuVDc4x4.RA1yiRfy0yQKn.v/aWZsTp.bKZSWUnu', id FROM employees WHERE email='alfonso@smarthr.dev'
    ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, employee_id)
SELECT 'LA001', '$2a$10$wrwoO0puF6HsqwuVDc4x4.RA1yiRfy0yQKn.v/aWZsTp.bKZSWUnu', id FROM employees WHERE email='laura@smarthr.dev'
    ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, employee_id)
SELECT 'CA001', '$2a$10$wrwoO0puF6HsqwuVDc4x4.RA1yiRfy0yQKn.v/aWZsTp.bKZSWUnu', id FROM employees WHERE email='carlos@smarthr.dev'
    ON CONFLICT (username) DO NOTHING;

-- =====================
-- ROLES para cada usuario
-- =====================
INSERT INTO users_roles (user_id, roles)
SELECT id, 'ROLE_EMPLOYEE' FROM users WHERE username IN ('MA001','JU001','AL001','LA001')
    ON CONFLICT DO NOTHING;

INSERT INTO users_roles (user_id, roles)
SELECT id, 'ROLE_RRHH' FROM users WHERE username='CA001'
    ON CONFLICT DO NOTHING;


-- =====================
-- PROJECTS (antes de assignments)
-- =====================

INSERT INTO projects (code, name, start_date, client, ubication) VALUES
                                                                     ('PRJ001', 'Sistema RRHH', '2024-01-10', 'SMARTHR', 'MADRID'),
                                                                     ('PRJ002', 'Portal Web Corporativo', '2023-11-01', 'NIKE', 'REMOTE'),
                                                                     ('PRJ003', 'Migración Cloud', '2024-02-15', 'ACCENTURE', 'BARCELONA'),
                                                                     ('PRJ004', 'Optimizacion de procesos', '2024-02-18', 'SALESFORCE', 'MADRID'),
                                                                     ('PRJ005', 'Desarrollo APIs', '2024-05-20', 'IBM', 'MADRID'),
                                                                     ('PRJ006', 'Migración Cloud', '2024-08-15', 'MICROSOFT', 'REMOTE')
    ON CONFLICT (code) DO NOTHING;


-- =====================
-- ASSIGNMENTS (cada uno separado)
-- =====================

INSERT INTO assignments (employee_id, project_id, job_position_id, start_date)
VALUES (
           (SELECT id FROM employees WHERE email='manuel@smarthr.dev'),
           (SELECT id FROM projects WHERE code='PRJ005'),
           (SELECT id FROM job_positions WHERE title='Backend Developer'),
           '2024-01-10'
       )
    ON CONFLICT (employee_id, project_id) DO NOTHING;



INSERT INTO assignments (employee_id, project_id, job_position_id, start_date)
VALUES (
           (SELECT id FROM employees WHERE email='julene@smarthr.dev'),
           (SELECT id FROM projects WHERE code='PRJ002'),
           (SELECT id FROM job_positions WHERE title='Frontend Developer'),
           '2023-11-01'
       )
    ON CONFLICT (employee_id, project_id) DO NOTHING;


INSERT INTO assignments (employee_id, project_id, job_position_id, start_date)
VALUES (
           (SELECT id FROM employees WHERE email='laura@smarthr.dev'),
           (SELECT id FROM projects WHERE code='PRJ003'),
           (SELECT id FROM job_positions WHERE title='DevOps Engineer'),
           '2024-02-15'
       )
    ON CONFLICT (employee_id, project_id) DO NOTHING;

INSERT INTO assignments (employee_id, project_id, job_position_id, start_date)
VALUES (
           (SELECT id FROM employees WHERE email='carlos@smarthr.dev'),
           (SELECT id FROM projects WHERE code='PRJ001'),
           (SELECT id FROM job_positions WHERE title='RRHH Manager'),
           '2024-01-10'
       )
    ON CONFLICT (employee_id, project_id) DO NOTHING;

INSERT INTO assignments (employee_id, project_id, job_position_id, start_date)
VALUES (
           (SELECT id FROM employees WHERE email='alfonso@smarthr.dev'),
           (SELECT id FROM projects WHERE code='PRJ004'),
           (SELECT id FROM job_positions WHERE title='RRHH Manager'),
           '2024-01-10'
       )
    ON CONFLICT (employee_id, project_id) DO NOTHING;


-- =====================
-- EMPLOYEE SKILLS
-- =====================
-- Manuel
INSERT INTO employee_skills (employee_id, skill_id, level)
SELECT e.id, s.id, 5
FROM employees e
         JOIN skills s ON s.name IN ('Java','Spring Boot','Docker','Kubernetes','SQL','Python')
WHERE e.email='manuel@smarthr.dev'
    ON CONFLICT DO NOTHING;

-- Julene
INSERT INTO employee_skills (employee_id, skill_id, level)
SELECT e.id, s.id, 5
FROM employees e
         JOIN skills s ON s.name IN ('Docker','React')
WHERE e.email='mjulene@smarthr.dev'
    ON CONFLICT DO NOTHING;

-- Alfonso
INSERT INTO employee_skills (employee_id, skill_id, level)
SELECT e.id, s.id, 5
FROM employees e
         JOIN skills s ON s.name IN ('Python','SQL','Kubernetes')
WHERE e.email='alfonso@smarthr.dev'
    ON CONFLICT DO NOTHING;

-- Laura
INSERT INTO employee_skills (employee_id, skill_id, level)
SELECT e.id, s.id, 5
FROM employees e
         JOIN skills s ON s.name IN ('Docker','Kubernetes')
WHERE e.email='laura@smarthr.dev'
    ON CONFLICT DO NOTHING;

-- Carlos
INSERT INTO employee_skills (employee_id, skill_id, level)
SELECT e.id, s.id, 5
FROM employees e
         JOIN skills s ON s.name IN ('Java','SQL')
WHERE e.email='carlos@smarthr.dev'
    ON CONFLICT DO NOTHING;

-- =====================
-- CONTRACTS
-- =====================
INSERT INTO contracts (employee_id, type, start_date, weekly_hours)
SELECT id, 'PERMANENT', '2024-01-01', 40 FROM employees WHERE email IN ('manuel@smarthr.dev','laura@smarthr.dev')
    ON CONFLICT DO NOTHING;

INSERT INTO contracts (employee_id, type, start_date, end_date, weekly_hours)
SELECT id, 'TEMPORARY', '2023-09-15', '2024-09-15', 350 FROM employees WHERE email='julene@smarthr.dev'
    ON CONFLICT DO NOTHING;

INSERT INTO contracts (employee_id, type, start_date, weekly_hours)
SELECT id, 'PERMANENT', '2022-05-01', 40 FROM employees WHERE email IN ('alfonso@smarthr.dev')
    ON CONFLICT DO NOTHING;

INSERT INTO contracts (employee_id, type, start_date, weekly_hours)
SELECT id, 'PERMANENT', '2020-03-15', 40 FROM employees WHERE email IN ('carlos@smarthr.dev')
    ON CONFLICT DO NOTHING;



-- =====================
-- COMPENSATIONS
-- =====================
INSERT INTO compensations (employee_id, base_salary, bonus, effective_from)
SELECT id, 3500.00, 500.00, '2024-01-01' FROM employees WHERE email='manuel@smarthr.dev'
    ON CONFLICT DO NOTHING;

INSERT INTO compensations (employee_id, base_salary, bonus, effective_from)
SELECT id, 2800.00, 200, '2023-09-15' FROM employees WHERE email='julene@smarthr.dev'
    ON CONFLICT DO NOTHING;

INSERT INTO compensations (employee_id, base_salary, bonus, effective_from)
SELECT id, 1000.10, 50, '2023-09-15' FROM employees WHERE email='laura@smarthr.dev'
    ON CONFLICT DO NOTHING;

INSERT INTO compensations (employee_id, base_salary, bonus, effective_from)
SELECT id, 5000.00, 200, '2023-09-15' FROM employees WHERE email='alfonso@smarthr.dev'
    ON CONFLICT DO NOTHING;

INSERT INTO compensations (employee_id, base_salary, bonus, effective_from)
SELECT id, 3200.00, 550, '2023-09-15' FROM employees WHERE email='carlos@smarthr.dev'
    ON CONFLICT DO NOTHING;

-- =====================
-- LEAVE REQUESTS
-- =====================
INSERT INTO leave_requests (employee_id, type, status, start_date, end_date, comments)
SELECT id, 'VACACIONES', 'APPROVED', '2024-08-01', '2024-08-15', 'Vacaciones de verano'
FROM employees WHERE email='manuel@smarthr.dev'
    ON CONFLICT DO NOTHING;

INSERT INTO leave_requests (employee_id, type, status, start_date, end_date, comments)
SELECT id, 'ENFERMEDAD', 'PENDING', '2024-02-10', '2024-02-12', 'Gripe común'
FROM employees WHERE email='julene@smarthr.dev'
    ON CONFLICT DO NOTHING;

INSERT INTO leave_requests (employee_id, type, status, start_date, end_date, comments)
SELECT id, 'EXCEDENCIA', 'REJECTED', '2025-02-10', '2026-02-10', 'Excedencia por motivos personales'
FROM employees WHERE email='julene@smarthr.dev'
    ON CONFLICT DO NOTHING;

-- =====================
-- PERFORMANCE REVIEWS
-- =====================
INSERT INTO performance_reviews (employee_id, review_date, rating, comments)
SELECT id, '2024-06-01', 'EXCELLENT', 'Gran liderazgo en el proyecto'
FROM employees WHERE email='manuel@smarthr.dev'
    ON CONFLICT DO NOTHING;

INSERT INTO performance_reviews (employee_id, review_date, rating, comments)
SELECT id, '2024-06-01', 'VERY_GOOD', 'Buen trabajo en frontend'
FROM employees WHERE email='julene@smarthr.dev'
    ON CONFLICT DO NOTHING;

INSERT INTO performance_reviews (employee_id, review_date, rating, comments)
SELECT id, '2024-06-01', 'GOOD', 'Cumple objetivos como freelance'
FROM employees WHERE email='alfonso@smarthr.dev'
    ON CONFLICT DO NOTHING;


-- ============================================
-- RAG PGVector SmartHR Assistant - OBLIGATORIO
-- ============================================

-- ============================================
-- RAG PGVector SmartHR Assistant - OLLAMA 1024
-- ============================================

-- Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;

-- Tabla vector_store compatible con IDs deterministas
CREATE TABLE IF NOT EXISTS vector_store (
                                            id TEXT PRIMARY KEY,                     -- ⬅️ ID DE NEGOCIO (employee:1, project:PRJ001, etc.)
                                            content TEXT NOT NULL,                   -- texto embebido
                                            metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
                                            embedding VECTOR(1024)                   -- dimensión correcta para Ollama
    );

-- Índice vectorial (búsqueda semántica)
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx
    ON vector_store
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- Índice por metadata (filtrado, debug, limpieza)
CREATE INDEX IF NOT EXISTS vector_store_metadata_idx
    ON vector_store
    USING GIN (metadata);

-- Índice específico por entityId (muy útil)
CREATE INDEX IF NOT EXISTS vector_store_entity_idx
    ON vector_store
    USING GIN ((metadata->'entityId'));








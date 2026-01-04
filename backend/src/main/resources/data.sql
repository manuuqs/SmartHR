
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
                                                   ('QA Tester', 'Pruebas y calidad')
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
-- EMPLOYEES (5 nuevos)
-- =====================
INSERT INTO employees (name, role, location, email, hire_date, department_id, job_position_id)
VALUES
    ('Manuel Quijada', 'Backend Developer', 'Madrid', 'manuel@smarthr.dev', '2024-01-01',
     (SELECT id FROM departments WHERE name='Desarrollo'),
     (SELECT id FROM job_positions WHERE title='Backend Developer')),
    ('Julene Peña', 'Frontend Developer', 'Barcelona', 'julene@smarthr.dev', '2023-09-15',
     (SELECT id FROM departments WHERE name='Desarrollo'),
     (SELECT id FROM job_positions WHERE title='Frontend Developer')),
    ('Alfonso Sampedro', 'Data Scientist', 'Madrid', 'alfonso@smarthr.dev', '2022-06-10',
     (SELECT id FROM departments WHERE name='Data'),
     (SELECT id FROM job_positions WHERE title='Data Scientist')),
    ('Laura Gómez', 'DevOps Engineer', 'Valencia', 'laura@smarthr.dev', '2023-03-20',
     (SELECT id FROM departments WHERE name='Desarrollo'),
     (SELECT id FROM job_positions WHERE title='DevOps Engineer')),
    ('Carlos Ruiz', 'QA Tester', 'Sevilla', 'carlos@smarthr.dev', '2024-02-01',
     (SELECT id FROM departments WHERE name='Recursos Humanos'),
     (SELECT id FROM job_positions WHERE title='QA Tester'))
    ON CONFLICT (email) DO NOTHING;

-- =====================
-- EMPLOYEE SKILLS
-- =====================
-- Manuel
INSERT INTO employee_skills (employee_id, skill_id, level)
SELECT e.id, s.id, 5 FROM employees e, skills s
WHERE e.email='manuel@smarthr.dev' AND s.name IN ('Java','Spring Boot','Docker')
    ON CONFLICT DO NOTHING;

-- Julene
INSERT INTO employee_skills (employee_id, skill_id, level)
SELECT e.id, s.id, 4 FROM employees e, skills s
WHERE e.email='julene@smarthr.dev' AND s.name IN ('React','Docker')
    ON CONFLICT DO NOTHING;

-- Alfonso
INSERT INTO employee_skills (employee_id, skill_id, level)
SELECT e.id, s.id, 5 FROM employees e, skills s
WHERE e.email='alfonso@smarthr.dev' AND s.name IN ('Python','SQL','Kubernetes')
    ON CONFLICT DO NOTHING;

-- Laura
INSERT INTO employee_skills (employee_id, skill_id, level)
SELECT e.id, s.id, 4 FROM employees e, skills s
WHERE e.email='laura@smarthr.dev' AND s.name IN ('Docker','Kubernetes')
    ON CONFLICT DO NOTHING;

-- Carlos
INSERT INTO employee_skills (employee_id, skill_id, level)
SELECT e.id, s.id, 3 FROM employees e, skills s
WHERE e.email='carlos@smarthr.dev' AND s.name IN ('Java','SQL')
    ON CONFLICT DO NOTHING;

-- =====================
-- PROJECTS
-- =====================
INSERT INTO projects (code, name, start_date, client) VALUES
                                                          ('PRJ001', 'Sistema RRHH', '2024-01-10', 'Cliente A'),
                                                          ('PRJ002', 'Portal Web Corporativo', '2023-11-01', 'Cliente B'),
                                                          ('PRJ003', 'Migración Cloud', '2024-02-15', 'Cliente C')
    ON CONFLICT (code) DO NOTHING;

-- =====================
-- ASSIGNMENTS
-- =====================
INSERT INTO assignments (employee_id, project_id, role_on_project, start_date)
VALUES
    ((SELECT id FROM employees WHERE email='manuel@smarthr.dev'), (SELECT id FROM projects WHERE code='PRJ001'), 'Líder Técnico', '2024-01-10'),
    ((SELECT id FROM employees WHERE email='julene@smarthr.dev'), (SELECT id FROM projects WHERE code='PRJ002'), 'Frontend Developer', '2023-11-01'),
    ((SELECT id FROM employees WHERE email='laura@smarthr.dev'), (SELECT id FROM projects WHERE code='PRJ003'), 'DevOps', '2024-02-15')
    ON CONFLICT DO NOTHING;

-- =====================
-- CONTRACTS
-- =====================
INSERT INTO contracts (employee_id, type, start_date, weekly_hours)
SELECT id, 'PERMANENT', '2024-01-01', 40 FROM employees WHERE email IN ('manuel@smarthr.dev','laura@smarthr.dev')
    ON CONFLICT DO NOTHING;

INSERT INTO contracts (employee_id, type, start_date, end_date, weekly_hours)
SELECT id, 'TEMPORARY', '2023-09-15', '2024-09-15', 40 FROM employees WHERE email='julene@smarthr.dev'
    ON CONFLICT DO NOTHING;

-- =====================
-- COMPENSATIONS
-- =====================
INSERT INTO compensations (employee_id, base_salary, bonus, effective_from)
SELECT id, 3500.00, 500.00, '2024-01-01' FROM employees WHERE email='manuel@smarthr.dev'
    ON CONFLICT DO NOTHING;

INSERT INTO compensations (employee_id, base_salary, bonus, effective_from)
SELECT id, 2800.00, NULL, '2023-09-15' FROM employees WHERE email='julene@smarthr.dev'
    ON CONFLICT DO NOTHING;

-- =====================
-- LEAVE REQUESTS
-- =====================
INSERT INTO leave_requests (employee_id, type, status, start_date, end_date, comments)
SELECT id, 'VACATION', 'APPROVED', '2024-08-01', '2024-08-15', 'Vacaciones de verano'
FROM employees WHERE email='manuel@smarthr.dev'
    ON CONFLICT DO NOTHING;

INSERT INTO leave_requests (employee_id, type, status, start_date, end_date, comments)
SELECT id, 'SICKNESS', 'PENDING', '2024-02-10', '2024-02-12', 'Gripe común'
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


-- =====================
-- USUARIO DE PRUEBA (RRHH)
-- =====================
INSERT INTO users (username, password)
VALUES (
           'rrhh_admin',
           '$2a$10$7QJkYQkYQkYQkYQkYQkYQkYQkYQkYQkYQkYQkYQkYQkYQkYQkYQkYQ' -- Contraseña encriptada
       )
    ON CONFLICT (username) DO NOTHING;

INSERT INTO users_roles (user_id, roles)
SELECT id, 'ROLE_RRHH' FROM users WHERE username='rrhh_admin'
    ON CONFLICT DO NOTHING;



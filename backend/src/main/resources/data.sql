-- =====================
-- DEPARTMENTS
-- =====================
INSERT INTO departments (name, description) VALUES
                                                ('Desarrollo', 'Departamento de desarrollo de software'),
                                                ('Marketing', 'Departamento de marketing y comunicación'),
                                                ('Recursos Humanos', 'Gestión del personal y contratación')
    ON CONFLICT (name) DO NOTHING;

-- =====================
-- JOB POSITIONS
-- =====================
INSERT INTO job_positions (title, description) VALUES
                                                   ('Backend Developer', 'Desarrollo backend'),
                                                   ('Frontend Developer', 'Desarrollo frontend'),
                                                   ('Data Scientist', 'Análisis de datos')
    ON CONFLICT (title) DO NOTHING;

-- =====================
-- SKILLS
-- =====================
INSERT INTO skills (name, description) VALUES
                                           ('Java', 'Lenguaje Java'),
                                           ('Spring Boot', 'Framework Spring'),
                                           ('React', 'Frontend'),
                                           ('Docker', 'Contenedores')
    ON CONFLICT (name) DO NOTHING;

-- =====================
-- EMPLOYEES
-- =====================
INSERT INTO employees (name, role, location, email, hire_date, department_id, job_position_id)
VALUES
    (
        'Manuel Quijada',
        'Backend Developer',
        'Madrid',
        'manuel@smarthr.dev',
        '2024-01-01',
        (SELECT id FROM departments WHERE name='Desarrollo'),
        (SELECT id FROM job_positions WHERE title='Backend Developer')
    )
    ON CONFLICT (email) DO NOTHING;

-- =====================
-- EMPLOYEE SKILLS
-- =====================
INSERT INTO employee_skills (employee_id, skill_id, level)
SELECT e.id, s.id, 5
FROM employees e, skills s
WHERE e.email='manuel@smarthr.dev'
  AND s.name IN ('Java','Spring Boot')
    ON CONFLICT DO NOTHING;

-- =====================
-- PROJECTS
-- =====================
INSERT INTO projects (code, name, start_date, client) VALUES
    ('PRJ001', 'Sistema RRHH', '2024-01-10', 'Cliente A')
    ON CONFLICT (code) DO NOTHING;

-- =====================
-- ASSIGNMENTS
-- =====================
INSERT INTO assignments (employee_id, project_id, role_on_project, start_date)
VALUES
    (
        (SELECT id FROM employees WHERE email='manuel@smarthr.dev'),
        (SELECT id FROM projects WHERE code='PRJ001'),
        'Líder Técnico',
        '2024-01-10'
    )
    ON CONFLICT DO NOTHING;

-- =====================
-- CONTRACTS
-- =====================
INSERT INTO contracts (employee_id, type, start_date, weekly_hours)
SELECT id, 'PERMANENT', '2024-01-01', 40
FROM employees WHERE email='manuel@smarthr.dev';

-- =====================
-- COMPENSATIONS
-- =====================
INSERT INTO compensations (employee_id, base_salary, bonus, effective_from)
SELECT id, 3500.00, 500.00, '2024-01-01'
FROM employees WHERE email='manuel@smarthr.dev';

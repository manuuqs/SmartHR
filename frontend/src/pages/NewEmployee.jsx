import React, { useEffect, useState } from "react";
import "../styles/NewEmployee.css";

export default function NewEmployee() {
    const baseUrl = import.meta.env.VITE_API_BASE_URL;
    const token = localStorage.getItem("token");

    const [projects, setProjects] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [skills, setSkills] = useState([]);

    const [form, setForm] = useState({
        name: "",
        surname: "",
        email: "",
        username: "",
        password: "",
        location: "",
        hireDate: "",
        departmentId: "",
        jobPositionTitle: "",
        weeklyHours: 40,
        projectId: "",
        role: "ROLE_EMPLOYEE",
        skillIds: [],

        // Contrato
        contractType: "PERMANENT",
        contractStartDate: "",
        contractEndDate: "",

        // Asignación (solo puesto; fechas = contrato)
        assignmentJobPosition: ""
    });

    const handleChange = (e) => {
        const { name, value, options, type } = e.target;

        if (type === "select-multiple") {
            const selected = Array.from(options)
                .filter((o) => o.selected)
                .map((o) => parseInt(o.value, 10));
            setForm((prev) => ({ ...prev, [name]: selected }));
        } else {
            setForm((prev) => ({ ...prev, [name]: value }));
        }
    };

    useEffect(() => {
        const fetchData = async () => {
            const headers = { Authorization: `Bearer ${token}` };

            const [projRes, deptRes, skillsRes] = await Promise.all([
                fetch(`${baseUrl}/api/projects`, { headers }),
                fetch(`${baseUrl}/api/departments`, { headers }),
                fetch(`${baseUrl}/api/skills`, { headers }),
            ]);

            const projData = await projRes.json();
            const deptData = await deptRes.json();
            const skillsData = await skillsRes.json();

            setProjects(projData.content ?? projData);
            setDepartments(deptData.content ?? deptData);
            setSkills(skillsData.content ?? skillsData);
        };

        fetchData();
    }, [baseUrl, token]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const headers = {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            };

            const completeBody = {
                name: form.name,
                surname: form.surname,
                email: form.email,
                username: form.username,
                password: form.password,
                location: form.location,
                hireDate: form.hireDate,
                departmentId: parseInt(form.departmentId),
                jobPositionTitle: form.jobPositionTitle,
                role: form.role,
                weeklyHours: form.weeklyHours,

                contractType: form.contractType,
                contractStartDate: form.contractStartDate,
                contractEndDate: form.contractEndDate || null,

                projectId: form.projectId ? parseInt(form.projectId) : null,
                assignmentJobPosition: form.assignmentJobPosition || null,

                skillIds: form.skillIds
            };

            const res = await fetch(`${baseUrl}/api/employees/complete`, {
                method: "POST",
                headers,
                body: JSON.stringify(completeBody),
            });

            if (!res.ok) {
                const error = await res.text();
                alert(`Error: ${error}`);
                return;
            }

            alert("Empleado creado completamente");
            window.location.href = "/rrhh";
        } catch (err) {
            console.error(err);
            alert("Error de conexión");
        }
    };


    return (
        <div className="new-employee-container">
            <form className="new-employee-form" onSubmit={handleSubmit}>
                <div className="new-employee-card">
                    <h2 className="new-employee-title">Nuevo Empleado</h2>

                    <div className="new-employee-grid">
                        <input
                            className="new-employee-input"
                            name="name"
                            placeholder="Nombre"
                            onChange={handleChange}
                        />
                        <input
                            className="new-employee-input"
                            name="surname"
                            placeholder="Apellidos"
                            onChange={handleChange}
                        />
                        <input
                            className="new-employee-input"
                            name="email"
                            placeholder="Email"
                            onChange={handleChange}
                        />
                        <input
                            className="new-employee-input"
                            name="username"
                            placeholder="Username"
                            onChange={handleChange}
                        />
                        <input
                            className="new-employee-input"
                            type="password"
                            name="password"
                            placeholder="Contraseña"
                            onChange={handleChange}
                        />
                        <input
                            className="new-employee-input"
                            name="location"
                            placeholder="Ubicación"
                            onChange={handleChange}
                        />

                        {/* Fechas claras */}
                        <div className="new-employee-field">
                            <label>Fecha de alta del empleado</label>
                            <input
                                className="new-employee-input"
                                type="date"
                                name="hireDate"
                                onChange={handleChange}
                            />
                        </div>

                        <div className="new-employee-field">
                            <label>Inicio del contrato (y de la asignación)</label>
                            <input
                                className="new-employee-input"
                                type="date"
                                name="contractStartDate"
                                onChange={handleChange}
                            />
                        </div>

                        <div className="new-employee-field">
                            <label>Fin del contrato (y de la asignación, opcional)</label>
                            <input
                                className="new-employee-input"
                                type="date"
                                name="contractEndDate"
                                onChange={handleChange}
                            />
                        </div>

                        <select
                            className="new-employee-input"
                            name="departmentId"
                            onChange={handleChange}
                        >
                            <option value="">Departamento</option>
                            {departments.map((d) => (
                                <option key={d.id} value={d.id}>
                                    {d.name}
                                </option>
                            ))}
                        </select>

                        <input
                            className="new-employee-input"
                            name="jobPositionTitle"
                            placeholder="Puesto (empleado)"
                            onChange={handleChange}
                        />

                        {/* Puesto en el proyecto (si quieres diferenciarlo) */}
                        <input
                            className="new-employee-input"
                            name="assignmentJobPosition"
                            placeholder="Puesto en el proyecto (opcional)"
                            onChange={handleChange}
                        />

                        <select
                            className="new-employee-input"
                            name="projectId"
                            onChange={handleChange}
                        >
                            <option value="">Proyecto</option>
                            {projects.map((p) => (
                                <option key={p.id} value={p.id}>
                                    {p.name} – {p.client} – {p.ubication}
                                </option>
                            ))}
                        </select>

                        <select
                            className="new-employee-input"
                            name="role"
                            onChange={handleChange}
                            value={form.role}
                        >
                            <option value="ROLE_EMPLOYEE">Empleado</option>
                            <option value="ROLE_RRHH">RRHH</option>
                        </select>

                        {/* Tipo de contrato */}
                        <select
                            className="new-employee-input"
                            name="contractType"
                            onChange={handleChange}
                            value={form.contractType}
                        >
                            <option value="PERMANENT">Contrato indefinido</option>
                            <option value="TEMPORARY">Temporal</option>
                            <option value="INTERN">Becario</option>
                            <option value="FREELANCE">Freelance</option>
                        </select>

                        {/* Skills múltiples */}
                        <select
                            className="new-employee-input"
                            name="skillIds"
                            multiple
                            onChange={handleChange}
                        >
                            {skills.map((s) => (
                                <option key={s.id} value={s.id}>
                                    {s.name}
                                </option>
                            ))}
                        </select>
                    </div>

                    <button className="new-employee-btn" type="submit">
                        Crear empleado
                    </button>
                </div>
            </form>
        </div>
    );
}

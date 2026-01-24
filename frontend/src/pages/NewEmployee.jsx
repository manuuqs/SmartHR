import React, { useEffect, useState } from "react";
import "../styles/NewEmployee.css";
import ThemeSwitch from "../components/ThemeSwitch.jsx";
import EmployeeCard from "../components/EmployeeCard";
import Loader from "../components/Loader.jsx"; // Si lo tienes
import { useNavigate } from "react-router-dom";

export default function NewEmployee() {
    const navigate = useNavigate();
    const baseUrl = import.meta.env.VITE_API_BASE_URL;
    const token = localStorage.getItem("token");

    // Estado para EmployeeCard (usuario logueado)
    const [me, setMe] = useState({ name: "", jobPositionTitle: "" });
    const [loadingMe, setLoadingMe] = useState(true);

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
        contractType: "PERMANENT",
        contractStartDate: "",
        contractEndDate: "",
        assignmentJobPosition: ""
    });

    // Cargar usuario logueado para EmployeeCard
    useEffect(() => {
        if (!token) return;

        fetch(`${baseUrl}/api/employees/me/full`, {
            headers: { Authorization: `Bearer ${token}` },
        })
            .then((res) => res.json())
            .then((data) => {
                setMe({
                    name: data.employee.name,
                    jobPositionTitle: data.employee.jobPositionTitle,
                });
            })
            .catch(() => {})
            .finally(() => setLoadingMe(false));
    }, [token, baseUrl]);

    // Cargar listas para selects
    useEffect(() => {
        if (!token) return;

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

            alert("✅ Empleado creado completamente");
            navigate("/rrhh");
        } catch (err) {
            console.error(err);
            alert("❌ Error de conexión");
        }
    };

    const handleLogout = () => {
        localStorage.clear();
        window.location.href = "/";
    };

    if (loadingMe) return <Loader />;

    return (
        <div className="new-employee-page">
            {/* ThemeSwitch arriba derecha */}
            <div className="theme-switch-wrapper">
                <ThemeSwitch />
            </div>

            {/* EmployeeCard arriba izquierda */}
            {me && (
                <div className="employee-card-wrapper">
                    <EmployeeCard
                        employee={me}
                        onLogout={handleLogout}
                    />
                </div>
            )}

            {/* Contenido centrado del formulario */}
            <div className="new-employee-container">
                <form className="new-employee-form" onSubmit={handleSubmit}>
                    <div className="new-employee-card">
                        <h2 className="new-employee-title">Nuevo Empleado</h2>

                        <div className="new-employee-grid">
                            {/* Datos personales */}
                            <input
                                className="new-employee-input"
                                name="name"
                                placeholder="Nombre *"
                                value={form.name}
                                onChange={handleChange}
                                required
                            />
                            <input
                                className="new-employee-input"
                                name="surname"
                                placeholder="Apellidos *"
                                value={form.surname}
                                onChange={handleChange}
                                required
                            />
                            <input
                                className="new-employee-input"
                                name="email"
                                type="email"
                                placeholder="Email *"
                                value={form.email}
                                onChange={handleChange}
                                required
                            />
                            <input
                                className="new-employee-input"
                                name="username"
                                placeholder="Username *"
                                value={form.username}
                                onChange={handleChange}
                                required
                            />
                            <input
                                className="new-employee-input"
                                type="password"
                                name="password"
                                placeholder="Contraseña *"
                                value={form.password}
                                onChange={handleChange}
                                required
                            />
                            <input
                                className="new-employee-input"
                                name="location"
                                placeholder="Ubicación *"
                                value={form.location}
                                onChange={handleChange}
                                required
                            />

                            {/* Fechas */}
                            <div className="new-employee-field">
                                <label>Fecha de alta *</label>
                                <input
                                    className="new-employee-input"
                                    type="date"
                                    name="hireDate"
                                    value={form.hireDate}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="new-employee-field">
                                <label>Inicio contrato (y asignación) *</label>
                                <input
                                    className="new-employee-input"
                                    type="date"
                                    name="contractStartDate"
                                    value={form.contractStartDate}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="new-employee-field">
                                <label>Fin contrato (opcional)</label>
                                <input
                                    className="new-employee-input"
                                    type="date"
                                    name="contractEndDate"
                                    value={form.contractEndDate}
                                    onChange={handleChange}
                                />
                            </div>

                            {/* Selects */}
                            <select
                                className="new-employee-input"
                                name="departmentId"
                                value={form.departmentId}
                                onChange={handleChange}
                                required
                            >
                                <option value="">Departamento *</option>
                                {departments.map((d) => (
                                    <option key={d.id} value={d.id}>
                                        {d.name}
                                    </option>
                                ))}
                            </select>

                            <input
                                className="new-employee-input"
                                name="jobPositionTitle"
                                placeholder="Puesto del empleado *"
                                value={form.jobPositionTitle}
                                onChange={handleChange}
                                required
                            />

                            <input
                                className="new-employee-input"
                                name="assignmentJobPosition"
                                placeholder="Puesto en proyecto (opcional)"
                                value={form.assignmentJobPosition}
                                onChange={handleChange}
                            />

                            <select
                                className="new-employee-input"
                                name="projectId"
                                value={form.projectId}
                                onChange={handleChange}
                            >
                                <option value="">Proyecto (opcional)</option>
                                {projects.map((p) => (
                                    <option key={p.id} value={p.id}>
                                        {p.name} – {p.client} – {p.ubication}
                                    </option>
                                ))}
                            </select>

                            <select
                                className="new-employee-input"
                                name="role"
                                value={form.role}
                                onChange={handleChange}
                                required
                            >
                                <option value="ROLE_EMPLOYEE">Empleado</option>
                                <option value="ROLE_RRHH">RRHH</option>
                            </select>

                            <select
                                className="new-employee-input"
                                name="contractType"
                                value={form.contractType}
                                onChange={handleChange}
                                required
                            >
                                <option value="PERMANENT">Contrato indefinido</option>
                                <option value="TEMPORARY">Temporal</option>
                                <option value="INTERN">Becario</option>
                                <option value="FREELANCE">Freelance</option>
                            </select>

                            <select
                                className="new-employee-input"
                                name="skillIds"
                                multiple
                                value={form.skillIds}
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
                            Crear Empleado Completo
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

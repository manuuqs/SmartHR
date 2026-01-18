import React, { useEffect, useState } from "react";
import "../styles/NewEmployee.css";

export default function NewEmployee() {
    const baseUrl = import.meta.env.VITE_API_BASE_URL;
    const token = localStorage.getItem("token");

    const [projects, setProjects] = useState([]);
    const [departments, setDepartments] = useState([]);

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
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    useEffect(() => {
        const fetchData = async () => {
            const headers = { Authorization: `Bearer ${token}` };

            const projRes = await fetch(`${baseUrl}/api/projects`, { headers });
            const deptRes = await fetch(`${baseUrl}/api/departments`, { headers });

            setProjects((await projRes.json()).content ?? []);
            setDepartments((await deptRes.json()).content ?? []);
        };

        fetchData();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const res = await fetch(`${baseUrl}/api/employees`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(form),
        });

        if (!res.ok) {
            alert("Error al crear empleado");
            return;
        }

        alert("Empleado creado correctamente");
        window.location.href = "/rrhh";
    };

    return (
        <div className="new-employee-container">
            <form className="new-employee-form" onSubmit={handleSubmit}>
                <div className="new-employee-card">
                    <h2 className="new-employee-title">Nuevo Empleado</h2>

                    <div className="new-employee-grid">
                        <input className="new-employee-input" name="name" placeholder="Nombre" onChange={handleChange} />
                        <input className="new-employee-input" name="surname" placeholder="Apellidos" onChange={handleChange} />

                        <input className="new-employee-input" name="email" placeholder="Email" onChange={handleChange} />
                        <input className="new-employee-input" name="username" placeholder="Username" onChange={handleChange} />

                        <input className="new-employee-input" type="password" name="password" placeholder="Contraseña" onChange={handleChange} />
                        <input className="new-employee-input" name="location" placeholder="Ubicación" onChange={handleChange} />

                        <input className="new-employee-input" type="date" name="hireDate" onChange={handleChange} />

                        <select className="new-employee-input" name="departmentId" onChange={handleChange}>
                            <option value="">Departamento</option>
                            {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                        </select>

                        <input className="new-employee-input" name="jobPositionTitle" placeholder="Puesto" onChange={handleChange} />

                        <select className="new-employee-input" name="projectId" onChange={handleChange}>
                            <option value="">Proyecto</option>
                            {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                        </select>

                        <select className="new-employee-input" name="role" onChange={handleChange}>
                            <option value="ROLE_EMPLOYEE">Empleado</option>
                            <option value="ROLE_RRHH">RRHH</option>
                            <option value="ROLE_ADMIN">Admin</option>
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

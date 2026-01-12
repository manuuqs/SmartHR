import React, { useEffect, useState } from "react";
import Layout from "../components/Layout";
import EmployeeCard from "../components/EmployeeCard";
import InfoCard from "../components/InfoCard";
import "../styles/EmployeeDashboard.css";
import mockData from "../mocks/employeeFullMock.json";

export default function EmployeeDashboard() {
    const [employeeData, setEmployeeData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [section, setSection] = useState("profile");

    const token = localStorage.getItem("token");
    const url = `${import.meta.env.VITE_API_BASE_URL}/api/employees/me/full`;

    // Ejemplo de fetch real (comentado)
    // useEffect(() => {
    //   fetch(url, { headers: { Authorization: `Bearer ${token}` } })
    //     .then((res) => {
    //       if (!res.ok) throw new Error("Error al cargar datos");
    //       return res.json();
    //     })
    //     .then((data) => {
    //       const normalizedData = {
    //         employee: {
    //           id: data.employee.id,
    //           name: data.employee.name,
    //           email: data.employee.email,
    //           location: data.employee.location,
    //           hireDate: data.employee.hireDate,
    //           department: { name: data.employee.departmentName },
    //           jobPosition: { title: data.employee.jobPositionTitle },
    //         },
    //         skills: data.skills,
    //         assignments: data.assignments,
    //         contracts: data.contracts,
    //         compensations: data.compensations,
    //         reviews: data.performanceReviews,
    //         leaveRequests: data.leaveRequests,
    //       };
    //       setEmployeeData(normalizedData);
    //     })
    //     .catch((err) => console.error(err))
    //     .finally(() => setLoading(false));
    // }, [token]);

    // Simulación con mockData
    useEffect(() => {
        setTimeout(() => {
            const normalizedData = {
                employee: {
                    id: mockData.employee.id,
                    name: mockData.employee.name,
                    email: mockData.employee.email,
                    location: mockData.employee.location,
                    hireDate: mockData.employee.hireDate,
                    department: { name: mockData.employee.departmentName },
                    jobPosition: { title: mockData.employee.jobPositionTitle },
                },
                skills: mockData.skills,
                assignments: mockData.assignments,
                contracts: mockData.contracts,
                compensations: mockData.compensations,
                reviews: mockData.performanceReviews,
                leaveRequests: mockData.leaveRequests,
            };
            setEmployeeData(normalizedData);
            setLoading(false);
        }, 500);
    }, []);

    if (loading) return <p>Cargando...</p>;
    if (!employeeData) return <p>Error al cargar datos</p>;

    const handleLogout = () => {
        localStorage.removeItem("token");
        window.location.href = "/";
    };

    return (
        <Layout
            employeeCard={
                <EmployeeCard
                    employee={{
                        name: employeeData.employee.name,
                        jobPositionTitle: employeeData.employee.jobPosition.title,
                    }}
                    onLogout={handleLogout}
                />
            }
        >
            <div className="dashboard-layout">
                {/* MENÚ LATERAL */}
                <div className="input">
                    {["profile", "skills", "projects", "contract", "salary"].map((sec) => {
                        const labels = {
                            profile: "👤 Perfil",
                            skills: "🛠 Skills",
                            projects: "📁 Proyectos",
                            contract: "💼 Contrato",
                            salary: "💰 Compensación",
                        };
                        return (
                            <button
                                key={sec}
                                className={`value ${section === sec ? "active" : ""}`}
                                onClick={() => setSection(sec)}
                            >
                                {labels[sec]}
                            </button>
                        );
                    })}
                </div>

                {/* CONTENIDO */}
                <div className="dashboard-content">
                    {section === "profile" && (
                        <InfoCard title="📄 Información Personal">
                            <div className="grid">
                                <input value={employeeData.employee.name} disabled />
                                <input value={employeeData.employee.email} disabled />
                                <input value={employeeData.employee.location} disabled />
                                <input value={employeeData.employee.hireDate} disabled />
                            </div>
                        </InfoCard>
                    )}

                    {section === "skills" && (
                        <InfoCard title="🛠 Skills">
                            {employeeData.skills.map((s) => (
                                <div key={s.id} className="skill-row">
                                    <span>{s.skillName}</span>
                                    <progress max="5" value={s.level} />
                                    <span>{s.level}/5</span>
                                </div>
                            ))}
                        </InfoCard>
                    )}

                    {section === "projects" && (
                        <InfoCard title="📁 Proyectos">
                            {employeeData.assignments.map((a) => (
                                <div key={a.projectId ?? a.projectName} className="list-card">
                                    <strong>{a.projectName}</strong>
                                    <p>{a.jobPosition}</p>
                                    <p>
                                        {a.startDate} → {a.endDate ?? "Actual"}
                                    </p>
                                </div>
                            ))}
                        </InfoCard>
                    )}

                    {section === "contract" && (
                        <InfoCard title="💼 Contrato">
                            {employeeData.contracts.map((c) => (
                                <div key={c.id} className="grid">
                                    <input value={c.type} disabled />
                                    <input value={`${c.weeklyHours} h/sem`} disabled />
                                </div>
                            ))}
                        </InfoCard>
                    )}

                    {section === "salary" && (
                        <InfoCard title="💰 Compensación">
                            {employeeData.compensations.map((c) => (
                                <div key={c.id} className="grid">
                                    <input value={`${c.baseSalary} €`} disabled />
                                    <input value={`${c.bonus} €`} disabled />
                                </div>
                            ))}
                        </InfoCard>
                    )}
                </div>
            </div>
        </Layout>
    );
}

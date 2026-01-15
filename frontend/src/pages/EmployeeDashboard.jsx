import React, { useEffect, useState } from "react";
import EmployeeCard from "../components/EmployeeCard";
import InfoCard from "../components/InfoCard";
import ThemeSwitch from "../components/ThemeSwitch.jsx";
import "../styles/EmployeeDashboard.css";
import mockData from "../mocks/employeeFullMock.json";

import dockerIcon from "../assets/dockerIcon.png";
import javaIcon from "../assets/javaIcon.png";
import kubernetesIcon from "../assets/kubernetesIcon.png";
import pythonIcon from "../assets/pythonIcon.png";
import reactIcon from "../assets/reactIcon.png";
import springIcon from "../assets/springIcon.png";
import sqlIcon from "../assets/sqlIcon.png";


export default function EmployeeDashboard() {
    const [employeeData, setEmployeeData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [section, setSection] = useState("profile");

    const token = localStorage.getItem("token");
    const url = `${import.meta.env.VITE_API_BASE_URL}/api/employees/me/full`;

    const skillIconMap = {
        Java: javaIcon,
        "Spring Boot": springIcon,
        React: reactIcon,
        Docker: dockerIcon,
        Kubernetes: kubernetesIcon,
        Python: pythonIcon,
        SQL: sqlIcon,
    };

    useEffect(() => {
        if (!token) {
            setEmployeeData(null);
            setLoading(false);
            return;
        }

        setLoading(true);

        fetch(url, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        })
            .then((res) => {
                if (!res.ok) throw new Error("Error al cargar datos");
                return res.json();
            })
            .then((data) => {
                const normalizedData = {
                    employee: {
                        id: data.employee.id,
                        name: data.employee.name,
                        email: data.employee.email,
                        location: data.employee.location,
                        hireDate: data.employee.hireDate,
                        department: { name: data.employee.departmentName },
                        jobPosition: { title: data.employee.jobPositionTitle },
                    },
                    skills: data.skills,
                    assignments: data.assignments,
                    contracts: data.contracts,
                    compensations: data.compensations,
                    reviews: data.performanceReviews,
                    leaveRequests: data.leaveRequests,
                };

                setEmployeeData(normalizedData);
            })
            .catch((err) => {
                console.error(err);
                setEmployeeData(null);
            })
            .finally(() => setLoading(false));
    }, [token, url]);

    const handleLogout = () => {
        localStorage.clear();
        setEmployeeData(null);
        window.location.href = "/";
    };

    if (loading) return <p>Cargando...</p>;
    if (!employeeData) return <p>Error al cargar datos</p>;

    // useEffect(() => {
    //     setTimeout(() => {
    //         const normalizedData = {
    //             employee: {
    //                 id: mockData.employee.id,
    //                 name: mockData.employee.name,
    //                 email: mockData.employee.email,
    //                 location: mockData.employee.location,
    //                 hireDate: mockData.employee.hireDate,
    //                 department: { name: mockData.employee.departmentName },
    //                 jobPosition: { title: mockData.employee.jobPositionTitle },
    //             },
    //             skills: mockData.skills,
    //             assignments: mockData.assignments.map(a => ({
    //                 id: a.id,
    //                 jobPosition: a.jobPosition,
    //                 startDate: a.startDate,
    //                 endDate: a.endDate,
    //                 project: {
    //                     id: a.project.id,
    //                     code: a.project.code,
    //                     name: a.project.name,
    //                     client: a.project.client,
    //                     ubication: a.project.ubication,
    //                     startDate: a.project.startDate,
    //                     endDate: a.project.endDate
    //                 }
    //             })),
    //             contracts: mockData.contracts,
    //             compensations: mockData.compensations,
    //             reviews: mockData.performanceReviews,
    //             leaveRequests: mockData.leaveRequests
    //         };
    //
    //         setEmployeeData(normalizedData);
    //         setLoading(false);
    //     }, 500);
    // }, []);


    return (
        <div className="dashboard-container">
            {/* ThemeSwitch arriba a la derecha */}
            <div className="theme-switch-wrapper">
                <ThemeSwitch />
            </div>

            <div className="employee-card-wrapper">
                <EmployeeCard
                    employee={{
                        name: employeeData.employee.name,
                        jobPositionTitle: employeeData.employee.jobPosition.title,
                    }}
                    onLogout={handleLogout}
                />
            </div>

                {/* Menú lateral */}
                <div className="input">
                    {["profile", "skills", "projects", "contract", "salary", "reviews", "leaves"].map((sec) => {
                        const labels = {
                            profile: "👤 Perfil",
                            skills: "🛠 Skills",
                            projects: "📁 Proyectos",
                            contract: "💼 Contrato",
                            salary: "💰 Compensación",
                            reviews: "⭐ Evaluaciones de desempeño",
                            leaves: "🏖 Solicitudes de ausencia",
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

            {/* Contenido principal */}
            <div className="dashboard-layout">
                <div className="dashboard-content">
                    {section === "profile" && (
                        <InfoCard title="📄 Información Personal">
                            <div className="grid">
                                <input value={employeeData.employee.name} readOnly />
                                <input value={employeeData.employee.email} readOnly />
                                <input value={employeeData.employee.location} readOnly />
                                <input value={employeeData.employee.hireDate} readOnly />
                                <input value={employeeData.employee.department.name} readOnly />
                            </div>
                        </InfoCard>
                    )}
                    {section === "skills" && (
                        <InfoCard title="🛠 Skills">
                            {employeeData.skills.map((s) => (
                                <div key={s.id} className="skill-row">
                                    <span className="skill-name">{s.skillName}</span>

                                    <progress max="5" value={s.level} />

                                    <span className="skill-level">{s.level}/5</span>

                                    {skillIconMap[s.skillName] && (
                                        <img
                                            src={skillIconMap[s.skillName]}
                                            alt={s.skillName}
                                            className="skill-icon"
                                        />
                                    )}
                                </div>
                            ))}
                        </InfoCard>
                    )}
                    {section === "projects" && (
                        <InfoCard title="📁 Proyectos">
                            {employeeData.assignments.map((a) => (
                                <div key={a.id} className="list-card">
                                    <strong>{a.project.name}</strong>
                                    <p>Código: {a.project.code}</p>
                                    <p>Cliente: {a.project.client}</p>
                                    <p>Ubicación: {a.project.ubication}</p>
                                    <p>Puesto: {a.jobPosition}</p>
                                    <p>
                                        Asignación: {a.startDate} → {a.endDate ?? "Actual"}
                                    </p>
                                </div>
                            ))}
                        </InfoCard>
                    )}

                    {section === "contract" && (
                        <InfoCard title="💼 Contrato">
                            {employeeData.contracts.map((c) => (
                                <div key={c.id} className="grid">
                                    <input value={c.type} readOnly />
                                    <input value={`${c.weeklyHours} h/sem`} readOnly />
                                    <input value={`Inicio: ${c.startDate}`} readOnly />
                                </div>
                            ))}
                        </InfoCard>
                    )}
                    {section === "salary" && (
                        <InfoCard title="💰 Compensación">
                            {employeeData.compensations.map((c) => (
                                <div key={c.id} className="grid">
                                    <div>
                                        <strong>Salario base </strong>
                                        <input value={`${c.baseSalary} €`} readOnly />
                                    </div>
                                    <div>
                                        <strong>Bono </strong>
                                        <input value={`${c.bonus} €`} readOnly />
                                    </div>
                                </div>
                            ))}
                        </InfoCard>
                    )}
                    {section === "reviews" && (
                        <InfoCard title="⭐ Evaluaciones de desempeño">
                            {employeeData.reviews.length === 0 ? (
                                <p>No hay evaluaciones registradas</p>
                            ) : (
                                employeeData.reviews.map((r) => (
                                    <div key={r.id} className="list-card">
                                        <strong>{r.rating}</strong>
                                        <p>Fecha: {r.reviewDate}</p>
                                        <p>{r.comments}</p>
                                    </div>
                                ))
                            )}
                        </InfoCard>
                    )}
                    {section === "leaves" && (
                        <InfoCard title="🏖 Solicitudes de ausencia">
                            {employeeData.leaveRequests.length === 0 ? (
                                <p>No hay solicitudes registradas</p>
                            ) : (
                                employeeData.leaveRequests.map((l) => {
                                    const statusEmoji =
                                        l.status === "APPROVED"
                                            ? "🟢"
                                            : l.status === "PENDING"
                                                ? "🟡"
                                                : "🔴";

                                    return (
                                        <div key={l.id} className="list-card">
                                            <strong>
                                                {statusEmoji} {l.type}
                                            </strong>
                                            <p>
                                                {l.startDate} → {l.endDate}
                                            </p>
                                            <p>Estado: {l.status}</p>
                                            {l.comments && <p>{l.comments}</p>}
                                        </div>
                                    );
                                })
                            )}
                        </InfoCard>
                    )}
                </div>
            </div>
        </div>
    );
}

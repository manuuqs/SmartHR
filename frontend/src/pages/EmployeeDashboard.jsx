import React, { useEffect, useState } from "react";
import EmployeeCard from "../components/EmployeeCard";
import InfoCard from "../components/InfoCard";
import ThemeSwitch from "../components/ThemeSwitch.jsx";
import "../styles/EmployeeDashboard.css";
import mockData from "../mocks/employeeFullMock.json";

export default function EmployeeDashboard() {
    const [employeeData, setEmployeeData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [section, setSection] = useState("profile");

    const token = localStorage.getItem("token");
    // const url = `${import.meta.env.VITE_API_BASE_URL}/api/employees/me/full`;
    //
    // useEffect(() => {
    //     fetch(url, { headers: { Authorization: Bearer ${token} } })
    //         .then(res => { if (!res.ok) throw new Error("Error al cargar datos");
    //             return res.json(); })
    //         .then(data => {
    //             const normalizedData = {
    //                 employee: {
    //                     id: data.employee.id,
    //                     name: data.employee.name,
    //                     email: data.employee.email,
    //                     location: data.employee.location,
    //                     hireDate: data.employee.hireDate,
    //                     department: { id: data.employee.departmentId,
    //                         name: data.employee.departmentName },
    //                     jobPosition: { id: data.employee.jobPositionId,
    //                         title: data.employee.jobPositionTitle }
    //                 },
    //                 skills: data.skills.map(s => ({
    //                     id: s.id,
    //                     skillId: s.skillId,
    //                     name: s.skillName,
    //                     level: s.level })),
    //                 assignments: data.assignments.map(a => ({
    //                     id: a.id,
    //                     projectId: a.projectId,
    //                     projectCode: a.projectCode,
    //                     projectName: a.projectName,
    //                     jobPosition: a.jobPosition,
    //                     startDate: a.startDate,
    //                     endDate: a.endDate })),
    //                 contracts: data.contracts.map(c => ({
    //                     id: c.id,
    //                     type: c.type,
    //                     startDate: c.startDate,
    //                     endDate: c.endDate,
    //                     weeklyHours: c.weeklyHours })),
    //                 compensations: data.compensations.map(c => ({
    //                     id: c.id,
    //                     baseSalary: c.baseSalary,
    //                     bonus: c.bonus,
    //                     effectiveFrom: c.effectiveFrom })),
    //                 reviews: data.performanceReviews.map(r => ({
    //                     id: r.id,
    //                     date: r.reviewDate,
    //                     rating: r.rating,
    //                     comments: r.comments })),
    //                 leaveRequests: data.leaveRequests.map(l => ({
    //                     id: l.id,
    //                     type: l.type,
    //                     status: l.status,
    //                     startDate: l.startDate,
    //                     endDate: l.endDate,
    //                     comments: l.comments })) };
    //             setEmployeeData(normalizedData); }) .catch(err => console.error(err)) .finally(() => setLoading(false)); }, [token]);

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
                                    <input value={c.type} readOnly />
                                    <input value={`${c.weeklyHours} h/sem`} readOnly />
                                </div>
                            ))}
                        </InfoCard>
                    )}
                    {section === "salary" && (
                        <InfoCard title="💰 Compensación">
                            {employeeData.compensations.map((c) => (
                                <div key={c.id} className="grid">
                                    <input value={`${c.baseSalary} €`} readOnly />
                                    <input value={`${c.bonus} €`} readOnly />
                                </div>
                            ))}
                        </InfoCard>
                    )}
                </div>
            </div>
        </div>
    );
}

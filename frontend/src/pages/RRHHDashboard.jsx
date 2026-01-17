import React, { useEffect, useState } from "react";
import EmployeeCard from "../components/EmployeeCard";
import ThemeSwitch from "../components/ThemeSwitch.jsx";
import Loader from "../components/Loader.jsx";
import "../styles/RRHHDashboard.css";

import InfoCard from "../components/InfoCard";

import dockerIcon from "../assets/dockerIcon.png";
import javaIcon from "../assets/javaIcon.png";
import kubernetesIcon from "../assets/kubernetesIcon.png";
import pythonIcon from "../assets/pythonIcon.png";
import reactIcon from "../assets/reactIcon.png";
import springIcon from "../assets/springIcon.png";
import sqlIcon from "../assets/sqlIcon.png";

const skillIconMap = {
    Java: javaIcon,
    "Spring Boot": springIcon,
    React: reactIcon,
    Docker: dockerIcon,
    Kubernetes: kubernetesIcon,
    Python: pythonIcon,
    SQL: sqlIcon,
};



export default function RRHHDashboard() {
    const [me, setMe] = useState({ name: "", jobPositionTitle: "" });
    const [employeeData, setEmployeeData] = useState(null);
    const [usernameInput, setUsernameInput] = useState("");
    const [section, setSection] = useState("profile");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const token = localStorage.getItem("token");
    const baseUrl = import.meta.env.VITE_API_BASE_URL;

    const [projectInput, setProjectInput] = useState("");
    const [projectResults, setProjectResults] = useState(null);

    /* ==========================
       Usuario logueado (EmployeeCard)
       ========================== */
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
            .catch(() => setError("No se pudo cargar el usuario"))
            .finally(() => setLoading(false));
    }, [token, baseUrl]);

    const handleLogout = () => {
        localStorage.clear();
        window.location.href = "/";
    };

    /* ==========================
          Buscar empleado
          ========================== */
    const handleEmployeeSearch = async (e) => {
        if (e.key !== "Enter" || !usernameInput.trim()) return;

        setLoading(true);

        try {
            const res = await fetch(
                `${baseUrl}/api/employees/user?username=${usernameInput}`,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            if (!res.ok) {
                // 👇 empleado no encontrado → no hacemos nada
                setEmployeeData(null);
                return;
            }

            const data = await res.json();
            setEmployeeData(data);
            setSection("profile");
        } catch (err) {
            console.error(err);
            setEmployeeData(null);
        } finally {
            setLoading(false);
        }
    };


    /* ==========================
       Buscar proyecto
       ========================== */
    const handleProjectSearch = async () => {
        setError("");
        setLoading(true);
        setEmployeeData(null);
        setProjectResults(null);

        try {
            const res = await fetch(
                `${baseUrl}/api/projects`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!res.ok) throw new Error("Error al cargar proyectos");

            const data = await res.json();
            setProjectResults(data.content ?? []);
        } catch (err) {
            console.error(err);
            setError("No se pudieron cargar los proyectos");
            setProjectResults([]);
        } finally {
            setLoading(false);
        }
    };


    if (loading) return <Loader />;
    if (error) return <p style={{ padding: "1rem" }}>{error}</p>;


    return (
        <div className="rrhh-container">
            {/* Theme */}
            <div className="theme-switch-wrapper">
                <ThemeSwitch />
            </div>

            {/* EmployeeCard fijo */}
            {me && (
                <div className="employee-card-wrapper">
                    <EmployeeCard employee={me} onLogout={handleLogout} />
                </div>
            )}

            {/* Sidebar */}
            <aside className="rrhh-sidebar">
                <div className="rrhh-search-row">
                    <input
                        className="rrhh-search-input"
                        placeholder="Buscar empleado (username)"
                        value={usernameInput}
                        onChange={(e) => setUsernameInput(e.target.value)}
                        onKeyDown={handleEmployeeSearch}
                    />

                    <div className="rrhh-project-button">
                        <button
                            className="rrhh-project-button__btn"
                            onClick={handleProjectSearch}
                            type="button"
                        >
                            <div className="rrhh-project-button__container">
                                <div className="rrhh-project-button__folder rrhh-project-button__folder--one"></div>
                                <div className="rrhh-project-button__folder rrhh-project-button__folder--two"></div>
                                <div className="rrhh-project-button__folder rrhh-project-button__folder--three"></div>
                                <div className="rrhh-project-button__folder rrhh-project-button__folder--four"></div>
                            </div>

                            <div className="rrhh-project-button__active-line"></div>
                            <span className="rrhh-project-button__text">Proyectos</span>
                        </button>

                        {/* Nuevo empleado */}
                        <button className="rrhh-action-btn">
                            Nuevo Empleado
                        </button>

                        {/* Ausencias pendientes */}
                        <button className="rrhh-action-btn">
                            Ausencias Pendientes
                        </button>

                    </div>
                </div>

                {employeeData && (
                    <div className="input">
                        {["profile", "skills", "projects", "contract", "salary", "reviews", "leaves"].map(
                            (sec) => (
                                <button
                                    key={sec}
                                    className={`value ${section === sec ? "active" : ""}`}
                                    onClick={() => setSection(sec)}
                                >
                                    {{
                                        profile: "👤 Perfil",
                                        skills: "🛠 Skills",
                                        projects: "📁 Proyectos",
                                        contract: "💼 Contrato",
                                        salary: "💰 Compensación",
                                        reviews: "⭐ Evaluaciones",
                                        leaves: "🏖 Ausencias",
                                    }[sec]}
                                </button>
                            )
                        )}
                    </div>
                )}
            </aside>

            {/* Main content */}
            <main className="rrhh-main-content">
                {employeeData && (
                    <DashboardSections employeeData={employeeData} section={section} />
                )}

                {!employeeData && projectResults && (
                    <InfoCard title="📁 Resultados de proyectos">
                        {projectResults.length === 0 ? (
                            <p>No se encontraron proyectos</p>
                        ) : (
                            projectResults.map((p) => (
                                <div key={p.id} className="list-card">
                                    <strong>{p.name}</strong>
                                    <p>Código: {p.code}</p>
                                    <p>Cliente: {p.client}</p>
                                    <p>Ubicación: {p.ubication}</p>
                                    <p>
                                        {p.startDate} → {p.endDate ?? "Actual"}
                                    </p>
                                </div>
                            ))
                        )}
                    </InfoCard>
                )}

                {!employeeData && !projectResults && <p></p>}
            </main>
        </div>
    );
}

/* ======================================================
   Secciones del empleado (idénticas a EmployeeDashboard)
   ====================================================== */
function DashboardSections({ employeeData, section }) {
    return (
        <div className="dashboard-layout">
            <div className="dashboard-content">

                {section === "profile" && (
                    <InfoCard title="📄 Información Personal">
                        <div className="grid">
                            <input value={employeeData.employee.name} readOnly />
                            <input value={employeeData.employee.email} readOnly />
                            <input value={employeeData.employee.location} readOnly />
                            <input value={employeeData.employee.hireDate} readOnly />
                            <input value={employeeData.employee.departmentName} readOnly />
                            <input value={employeeData.employee.jobPositionTitle} readOnly />
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
                                {skillIconMap[s.skillName] && (
                                    <img src={skillIconMap[s.skillName]} alt={s.skillName} />
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
                                <p>{a.project.client}</p>
                                <p>{a.startDate} → {a.endDate ?? "Actual"}</p>
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
                                <input value={c.startDate} readOnly />
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

                {section === "reviews" && (
                    <InfoCard title="⭐ Evaluaciones">
                        {employeeData.performanceReviews.map((r) => (
                            <div key={r.id} className="list-card">
                                <strong>{r.rating}</strong>
                                <p>{r.comments}</p>
                            </div>
                        ))}
                    </InfoCard>
                )}

                {section === "leaves" && (
                    <InfoCard title="🏖 Ausencias">
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
    );

}

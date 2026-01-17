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

    const [projectResults, setProjectResults] = useState(null);
    const [pendingLeaves, setPendingLeaves] = useState(null);

    const token = localStorage.getItem("token");
    const baseUrl = import.meta.env.VITE_API_BASE_URL;

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
        setProjectResults(null);
        setPendingLeaves(null);

        try {
            const res = await fetch(
                `${baseUrl}/api/employees/user?username=${usernameInput}`,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            if (!res.ok) {
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
        setPendingLeaves(null);

        try {
            const res = await fetch(`${baseUrl}/api/projects`, {
                headers: { Authorization: `Bearer ${token}` },
            });

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

    /* ==========================
       Cargar ausencias pendientes
    ========================== */
    const handlePendingLeaves = async () => {
        setError("");
        setLoading(true);
        setEmployeeData(null);
        setProjectResults(null);

        try {
            const res = await fetch(`${baseUrl}/api/leave-requests/pending`, {
                headers: { Authorization: `Bearer ${token}` },
            });

            if (!res.ok) throw new Error("Error al cargar las ausencias pendientes");

            const data = await res.json();
            setPendingLeaves(data);
        } catch (err) {
            console.error(err);
            setError("No se pudieron cargar las ausencias pendientes");
            setPendingLeaves([]);
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
                    {/* Input de empleado */}
                    <input
                        className="rrhh-search-input"
                        placeholder="Buscar empleado (username)"
                        value={usernameInput}
                        onChange={(e) => setUsernameInput(e.target.value)}
                        onKeyDown={handleEmployeeSearch}
                    />

                    {/* Botones a la derecha */}
                    <div className="rrhh-actions">
                        <button>Nuevo Empleado</button>
                        <button className="rrhh-action-btn" onClick={handlePendingLeaves}>
                            Ausencias Pendientes
                        </button>
                    </div>

                    {/* Botón de Proyectos */}
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
                    </div>
                </div>

                {/* Secciones del empleado */}
                {employeeData && (
                    <div className="input">
                        {[
                            "profile",
                            "skills",
                            "projects",
                            "contract",
                            "salary",
                            "reviews",
                            "leaves",
                        ].map((sec) => (
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
                        ))}
                    </div>
                )}
            </aside>

            {/* Main content */}
            <main className="rrhh-main-content">
                {/* Perfil de empleado */}
                {employeeData && (
                    <DashboardSections employeeData={employeeData} section={section} />
                )}

                {/* Resultados de proyectos */}
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

                {/* Ausencias pendientes */}
                {!employeeData && pendingLeaves && (
                    <InfoCard title="🏖 Ausencias Pendientes">
                        {pendingLeaves.length === 0 ? (
                            <p>No hay solicitudes pendientes</p>
                        ) : (
                            pendingLeaves.map((l) => (
                                <div key={l.id} className="list-card" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                                    <div>
                                        <strong>
                                            {l.status === "PENDING"
                                                ? "🟡"
                                                : l.status === "APPROVED"
                                                    ? "🟢"
                                                    : "🔴"}{" "}
                                            {l.type}
                                        </strong>
                                        <p>Empleado: {l.employeeName}</p>
                                        <p>
                                            {l.startDate} → {l.endDate}
                                        </p>
                                        {l.comments && <p>{l.comments}</p>}
                                    </div>

                                    {/* Botones Aprobar / Denegar */}
                                    <div className="like-unlike-radio">
                                        <div>
                                            <input
                                                name={`feedback-${l.id}`}
                                                value="approve"
                                                id={`approve-${l.id}`}
                                                className="custom-radio-fb"
                                                type="radio"
                                                onChange={() => handleLeaveDecision(l.id, "APPROVED")}
                                            />
                                            <label htmlFor={`approve-${l.id}`} className="feedback-label">
                                                <svg
                                                    className="icon"
                                                    width="27"
                                                    height="27"
                                                    viewBox="0 0 27 27"
                                                    fill="currentColor"
                                                    xmlns="http://www.w3.org/2000/svg"
                                                >
                                                    <path
                                                        fillRule="evenodd"
                                                        clipRule="evenodd"
                                                        d="M0.7229 26.5H5.92292V10.9008H0.7229V26.5ZM26.6299 15.2618L24.372 23.7566C23.9989 25.3696 22.5621 26.5 20.9072 26.5H8.52293V10.9278L10.7573 2.87293C10.9669 1.50799 12.1418 0.5 13.524 0.5C15.0699 0.5 16.323 1.7527 16.323 3.29837V10.8998H23.1651C25.4519 10.9009 27.1453 13.0335 26.6299 15.2618Z"
                                                        fill="currentColor"
                                                    ></path>
                                                </svg>
                                                Aprobar
                                            </label>
                                        </div>
                                        <div>
                                            <input
                                                name={`feedback-${l.id}`}
                                                value="deny"
                                                id={`deny-${l.id}`}
                                                className="custom-radio-fb"
                                                type="radio"
                                                onChange={() => handleLeaveDecision(l.id, "REJECTED")}
                                            />
                                            <label htmlFor={`deny-${l.id}`} className="feedback-label">
                                                <svg
                                                    className="icon"
                                                    width="27"
                                                    height="27"
                                                    viewBox="0 0 27 27"
                                                    fill="currentColor"
                                                    xmlns="http://www.w3.org/2000/svg"
                                                >
                                                    <path
                                                        fillRule="evenodd"
                                                        clipRule="evenodd"
                                                        d="M26.7229 0.5L21.5229 0.5L21.5229 16.0992L26.7229 16.0992L26.7229 0.5ZM0.815853 11.7382L3.07376 3.24339C3.44687 1.63037 4.88372 0.500027 6.53861 0.500027L18.9229 0.500028L18.9229 16.0722L16.6885 24.1271C16.4789 25.492 15.304 26.5 13.9218 26.5C12.3759 26.5 11.1228 25.2473 11.1228 23.7016L11.1228 16.1002L4.28068 16.1002C1.99391 16.0991 0.300502 13.9664 0.815853 11.7382Z"
                                                        fill="currentColor"
                                                    ></path>
                                                </svg>
                                                Denegar
                                            </label>
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </InfoCard>
                )}

                {/* Estado vacío */}
                {!employeeData && !projectResults && !pendingLeaves && <p></p>}
            </main>
        </div>
    );
}

/* ======================================================
   Secciones del empleado
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

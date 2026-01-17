
import React, { useEffect, useState } from "react";
import EmployeeCard from "../components/EmployeeCard";
import ThemeSwitch from "../components/ThemeSwitch.jsx";
import "../styles/RRHHDashboard.css";
import Loader from "../components/Loader.jsx";

export default function RRHHDashboard() {
    const [me, setMe] = useState({ name: "", jobPositionTitle: "" });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const token = localStorage.getItem("token");
    const url = `${import.meta.env.VITE_API_BASE_URL}/api/employees/me/full`; // <- mismo endpoint que employee

    useEffect(() => {
        if (!token) {
            setError("No hay sesión iniciada");
            setLoading(false);
            return;
        }
        setLoading(true);
        setError("");

        fetch(url, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        })
            .then((res) => {
                if (!res.ok) throw new Error("Error al cargar datos de empleado");
                return res.json();
            })
            .then((data) => {
                // Normaliza igual que en tu EmployeeDashboard: employee.name + jobPosition.title
                const normalized = {
                    name: data?.employee?.name ?? "",
                    jobPositionTitle: data?.employee?.jobPositionTitle ?? data?.employee?.jobPosition?.title ?? "",
                };
                setMe(normalized);
            })
            .catch((err) => {
                console.error(err);
                setError("No se pudo cargar la información del empleado");
            })
            .finally(() => setLoading(false));
    }, [token, url]);

    const handleLogout = () => {
        localStorage.clear();
        window.location.href = "/";
    };

    if (loading) return <Loader />;
    if (error) return <p style={{ padding: "1rem" }}>{error}</p>;

    return (
        <div className="rrhh-container">
            {/* ThemeSwitch arriba a la derecha (el componente ya es fixed internamente) */}
            <div className="theme-switch-wrapper">
                <ThemeSwitch />
            </div>

            {/* EmployeeCard flotando arriba a la izquierda */}
            <div className="employee-card-wrapper">
                <EmployeeCard
                    employee={{
                        name: me.name,
                        jobPositionTitle: me.jobPositionTitle,
                    }}
                    onLogout={handleLogout}
                />
            </div>

            {/* Lienzo vacío para el contenido futuro del RRHH Dashboard */}
            <main className="rrhh-empty-main">
                <input
                    placeholder="Username"
                    className="rrhh-search-input"
                    name="text"
                    type="text"
                />
            </main>
        </div>
    );
}


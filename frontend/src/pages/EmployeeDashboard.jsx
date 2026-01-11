
import Layout from "../components/Layout";
import EmployeeCard from "../components/EmployeeCard";
import InfoCard from "../components/InfoCard.jsx";
import React, { useEffect, useState } from "react";

export default function EmployeeDashboard() {
    const [employeeData, setEmployeeData] = useState(null);
    const [loading, setLoading] = useState(true);

    const token = localStorage.getItem("token");
    const url = `${import.meta.env.VITE_API_BASE_URL}/api/employees/me/full`;

    useEffect(() => {
        fetch(url, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        })
            .then(res => {
                if (!res.ok) throw new Error("Error al cargar datos");
                return res.json();
            })
            .then(data => {
                const normalizedData = {
                    employee: {
                        id: data.employee.id,
                        name: data.employee.name,
                        email: data.employee.email,
                        location: data.employee.location,
                        hireDate: data.employee.hireDate,
                        department: {
                            id: data.employee.departmentId,
                            name: data.employee.departmentName
                        },
                        jobPosition: {
                            id: data.employee.jobPositionId,
                            title: data.employee.jobPositionTitle
                        }
                    },
                    skills: data.skills.map(s => ({
                        id: s.id,
                        skillId: s.skillId,
                        name: s.skillName,
                        level: s.level
                    })),
                    assignments: data.assignments.map(a => ({
                        id: a.id,
                        projectId: a.projectId,
                        projectCode: a.projectCode,
                        projectName: a.projectName,
                        jobPosition: a.jobPosition,
                        startDate: a.startDate,
                        endDate: a.endDate
                    })),
                    contracts: data.contracts.map(c => ({
                        id: c.id,
                        type: c.type,
                        startDate: c.startDate,
                        endDate: c.endDate,
                        weeklyHours: c.weeklyHours
                    })),
                    compensations: data.compensations.map(c => ({
                        id: c.id,
                        baseSalary: c.baseSalary,
                        bonus: c.bonus,
                        effectiveFrom: c.effectiveFrom
                    })),
                    reviews: data.performanceReviews.map(r => ({
                        id: r.id,
                        date: r.reviewDate,
                        rating: r.rating,
                        comments: r.comments
                    })),
                    leaveRequests: data.leaveRequests.map(l => ({
                        id: l.id,
                        type: l.type,
                        status: l.status,
                        startDate: l.startDate,
                        endDate: l.endDate,
                        comments: l.comments
                    }))
                };

                setEmployeeData(normalizedData);
            })
            .catch(err => console.error(err))
            .finally(() => setLoading(false));
    }, [token]);

    if (loading) return <p>Cargando...</p>;
    if (!employeeData) return <p>Error al cargar datos</p>;

    const handleLogout = () => {
        // Eliminar token y redirigir
        localStorage.removeItem("token");
        sessionStorage.removeItem("token");
        window.location.href = "/";
    };

    return (
        <Layout
            employeeCard={
                <EmployeeCard
                    employee={{
                        name: employeeData.employee.name,
                        jobPositionTitle: employeeData.employee.jobPosition.title
                    }}
                    onLogout={handleLogout}
                />
            }
        >
            <div style={{ padding: "1rem" }}>
                <h1>👷 Panel Empleado</h1>
                <p>Bienvenido al dashboard de empleados.</p>
            </div>
        </Layout>
    );
}

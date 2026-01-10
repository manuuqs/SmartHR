import Layout from "../components/Layout";
import EmployeeCard from "../components/EmployeeCard";

export default function EmployeeDashboard() {
    const handleLogout = () => {
        console.log("Cerrar sesión");
        // Redirigir o limpiar estado de usuario
    };

    return (
        <Layout employeeCard={<EmployeeCard onLogout={handleLogout} />}>
            <div style={{ padding: "1rem" }}>
                <h1>👷 Panel Empleado</h1>
                <p>Bienvenido al dashboard de empleados.</p>
            </div>
        </Layout>
    );
}

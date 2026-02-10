import React, { useState } from "react";
import "../styles/NewEmployee.css"; // reutilizamos estilos

export default function LeaveRequestForm({ employee, onClose, onCreated }) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL;
    const token = localStorage.getItem("token"); // <-- token del login

    const [form, setForm] = useState({
        employeeId: employee.id,
        employeeName: employee.name,
        type: "VACACIONES", // valor inicial del enum
        status: "PENDING",  // siempre pendiente
        startDate: "",
        endDate: "",
        comments: "",
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const headers = {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`, // <-- token obligatorio para roles
            };

            const res = await fetch(`${baseUrl}/api/leave-requests`, {
                method: "POST",
                headers,
                body: JSON.stringify(form),
            });

            if (!res.ok) {
                const errorText = await res.text();
                throw new Error(errorText || "Error al crear solicitud");
            }

            const data = await res.json();

            // Actualizar lista de solicitudes en el dashboard
            onCreated(data);

            // Cerrar modal
            onClose();

            alert("✅ Solicitud creada correctamente");
        } catch (err) {
            console.error(err);
            alert("❌ Error: " + err.message);
        }
    };

    return (
        <div className="modal-backdrop">
            <div className="new-employee-card" style={{ maxWidth: "500px" }}>
                <h2 className="new-employee-title">Nueva Solicitud de Ausencia</h2>
                <form className="new-employee-form" onSubmit={handleSubmit}>
                    <div className="new-employee-grid">
                        <select
                            className="new-employee-input"
                            name="type"
                            value={form.type}
                            onChange={handleChange}
                            required
                        >
                            <option value="VACACIONES">Vacaciones</option>
                            <option value="ENFERMEDAD">Enfermedad</option>
                            <option value="CONSULTAMEDICA">Consulta médica</option>
                            <option value="ASUNTOSPROPIOS">Asuntos propios</option>
                            <option value="EXCEDENCIA">Excedencia</option>
                            <option value="OTROS">Otros</option>
                        </select>

                        <div className="new-employee-field">
                            <label>Fecha de inicio *</label>
                            <input
                                className="new-employee-input"
                                type="date"
                                name="startDate"
                                value={form.startDate}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="new-employee-field">
                            <label>Fecha de fin *</label>
                            <input
                                className="new-employee-input"
                                type="date"
                                name="endDate"
                                value={form.endDate}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <textarea
                            className="new-employee-input"
                            name="comments"
                            placeholder="Comentarios (opcional)"
                            value={form.comments}
                            onChange={handleChange}
                        />
                    </div>

                    <div style={{ display: "flex", justifyContent: "space-between", marginTop: "12px" }}>
                        <button className="new-request-btn" type="submit">
                            Crear
                        </button>
                        <button
                            type="button"
                            className="new-employee-btn"
                            onClick={onClose}
                        >
                            Cancelar
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

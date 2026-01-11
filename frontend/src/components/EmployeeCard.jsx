
import React from "react";
import "../styles/EmployeeCard.css";
import iconoUser from "../assets/iconoUser.png"; // imagen de perfil

export default function EmployeeCard({ employee, onLogout }) {
    return (
        <div className="card small">
            <center>
                {/* Imagen de perfil */}
                <div className="profileimage">
                    <img src={iconoUser} alt="Perfil" className="pfp" />
                </div>

                {/* Nombre y puesto */}
                <div className="Name">
                    <p>{employee.name}</p>
                    <span className="subtitle">
                        {employee.jobPositionTitle}
                    </span>
                </div>

                {/* Botón de Log Out */}
                <div className="logoutbar">
                    <button
                        className="logout-btn"
                        onClick={onLogout}
                    >
                        Log Out
                    </button>
                </div>
            </center>
        </div>
    );
}

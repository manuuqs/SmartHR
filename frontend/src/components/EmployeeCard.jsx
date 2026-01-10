import React from "react";
import "../styles/EmployeeCard.css";
import iconoUser from "../assets/iconoUser.png"; // importa tu imagen

export default function EmployeeCard({ onLogout }) {
    return (
        <div className="card small">
            <center>
                {/* Imagen de perfil */}
                <div className="profileimage">
                    <img src={iconoUser} alt="Perfil" className="pfp" />
                </div>

                {/* Nombre y rol */}
                <div className="Name">
                    <p>John Doe</p>
                    <span className="subtitle">Java Developer</span>
                </div>

                {/* Botón de Log Out */}
                <div className="logoutbar">
                    <button className="logout-btn" onClick={onLogout}>
                        Log Out
                    </button>
                </div>
            </center>
        </div>
    );
}

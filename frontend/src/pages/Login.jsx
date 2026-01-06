
import { useState } from "react";
import "./../styles/Login.css";

export default function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password }),
            });
            if (!response.ok) throw new Error("Error en login");
            const data = await response.json();
            localStorage.setItem("token", data.token);
            alert("Login correcto");
        } catch (error) {
            alert("Credenciales inválidas");
        }
    };

    return (
        <div className="login-container">
            <div className="login-box">
                <h2>Inicio de sesión</h2>
                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="Usuario"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                    <input
                        type="password"
                        placeholder="Contraseña"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                    <button type="submit">Entrar</button>
                </form>
            </div>
        </div>
    );
}
``

import { useState } from "react";
import "../styles/Login.css";
import ThemeSwitch from "../components/ThemeSwitch";

export default function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");

        try {
            const response = await fetch(
                `${import.meta.env.VITE_API_BASE_URL}/auth/login`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        username,
                        password,
                    }),
                }
            );

            if (!response.ok) {
                throw new Error("Credenciales incorrectas");
            }

            const data = await response.json();

            // 🔐 Ejemplo: guardar token
            localStorage.setItem("token", data.token);

            console.log("Login correcto", data);
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="container">
            <ThemeSwitch />
            <div className="login-slider">
                <form className="form" onSubmit={handleSubmit}>
                    <span className="title">Login</span>

                    <div className="form_control">
                        <input
                            type="text"
                            className="input"
                            required
                            placeholder=" "
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                        />
                        <label className="label">Username</label>
                    </div>

                    <div className="form_control">
                        <input
                            type="password"
                            className="input"
                            required
                            placeholder=" "
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                        <label className="label">Password</label>
                    </div>

                    {error && <span className="error">{error}</span>}

                    <button type="submit">Login</button>
                </form>
            </div>
        </div>
    );
}

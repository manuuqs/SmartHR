import { useState } from "react";
import "../styles/AIChat.css";

export default function AIChat() {
    const [messages, setMessages] = useState([
        { from: "assistant", text: "Hola 👋 ¿En qué puedo ayudarte?" }
    ]);
    const [input, setInput] = useState("");
    const [open, setOpen] = useState(true);
    const [loading, setLoading] = useState(false);

    const token = localStorage.getItem("token");
    const baseUrl = import.meta.env.VITE_API_ASSISTANT_URL;

    const sendMessage = async () => {
        if (!input.trim()) return;

        const userText = input;
        setInput("");
        setMessages(prev => [...prev, { from: "user", text: userText }]);
        setLoading(true);

        try {
            const res = await fetch(`${baseUrl}/api/assistant/chat`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    message: userText
                })
            });

            if (!res.ok) throw new Error("Error asistente");

            const data = await res.json();

            setMessages(prev => [
                ...prev,
                { from: "assistant", text: data.response }
            ]);
        } catch (err) {
            setMessages(prev => [
                ...prev,
                {
                    from: "assistant",
                    text: "⚠️ El asistente no está disponible ahora mismo"
                }
            ]);
        }
        finally {
            setLoading(false);
        }
    };

    if (!open) return (
        <div className="ai-chat-open-btn" onClick={() => setOpen(true)}>
            🤖
        </div>
    );

    return (
        <div className="container ai-chat-fixed">
            <div className="nav-bar">
                <a>Chat IA</a>
                <div className="close" onClick={() => setOpen(false)}>
                    <div className="line one"></div>
                    <div className="line two"></div>
                </div>
            </div>

            <div className="messages-area">
                {messages.map((m, i) => (
                    <div
                        key={i}
                        className={`message ${m.from}`}
                    >
                        {m.text}
                    </div>
                ))}
                {loading && (
                    <div className="message assistant">Escribiendo…</div>
                )}
            </div>

            <div className="sender-area">
                <div className="input-place">
                    <input
                        placeholder="Escribe un mensaje…"
                        className="send-input"
                        value={input}
                        onChange={e => setInput(e.target.value)}
                        onKeyDown={e => e.key === "Enter" && sendMessage()}
                        type="text"
                    />
                    <div className="send" onClick={sendMessage}>
                        <svg
                            className="send-icon"
                            viewBox="0 0 512 512"
                        >
                            <path
                                fill="#6B6C7B"
                                d="M481.508,210.336L68.414,38.926c-17.403-7.222-37.064-4.045-51.309,8.287C2.86,59.547-3.098,78.551,1.558,96.808L38.327,241h180.026c8.284,0,15.001,6.716,15.001,15.001s-6.716,15.001-15.001,15.001H38.327L1.558,415.193c-4.656,18.258,1.301,37.262,15.547,49.595c14.274,12.357,33.937,15.495,51.31,8.287l413.094-171.409C500.317,293.862,512,276.364,512,256.001S500.317,218.139,481.508,210.336z"
                            />
                        </svg>
                    </div>
                </div>
            </div>
        </div>
    );
}

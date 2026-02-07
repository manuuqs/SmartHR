import { useState, useEffect } from "react";

import "../styles/AIChat.css";

export default function AIChat({ employeeName }) {

    const [messages, setMessages] = useState(() => {
        const saved = localStorage.getItem("aiChatMessages");
        return saved
            ? JSON.parse(saved)
            : [{ from: "assistant", text: "Hola 👋 ¿En qué puedo ayudarte?" }];
    });

    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);

    const [open, setOpen] = useState(() => {
        const saved = localStorage.getItem("aiChatOpen");
        return saved ? JSON.parse(saved) : true;
    });


    const token = localStorage.getItem("token");
    const baseUrl = import.meta.env.VITE_API_ASSISTANT_URL;

    useEffect(() => {
        localStorage.setItem("aiChatMessages", JSON.stringify(messages));
    }, [messages]);

    useEffect(() => {
        localStorage.setItem("aiChatOpen", JSON.stringify(open));
    }, [open]);


    const sendMessage = async () => {
        if (!input.trim() || loading) return;

        const userText = input;
        setInput("");
        setLoading(true);

        setMessages(prev => [...prev, { from: "user", text: userText }]);

        const isEmployeeChat = Boolean(employeeName);

        const endpoint = isEmployeeChat
            ? `${baseUrl}/api/assistant/chat/employee`
            : `${baseUrl}/api/assistant/chat`;

        const body = isEmployeeChat
            ? { message: userText, employeeName }
            : { message: userText };

        console.log("🤖 Chat mode:", isEmployeeChat ? "EMPLOYEE" : "GENERIC");
        console.log("📡 Endpoint:", endpoint);
        console.log("📦 Body:", body);

        try {
            const res = await fetch(endpoint, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(body),
            });

            if (!res.ok) throw new Error("Error asistente");

            const data = await res.json();

            setMessages(prev => [
                ...prev,
                { from: "assistant", text: data.response },
            ]);
        } catch (err) {
            console.error("❌ AIChat error", err);
            setMessages(prev => [
                ...prev,
                {
                    from: "assistant",
                    text: "⚠️ El asistente no está disponible ahora mismo",
                },
            ]);
        } finally {
            setLoading(false);
        }
    };

    if (!open) {
        return (
            <div
                className="ai-chat-open-btn"
                onClick={() => setOpen(true)}
                title="Abrir asistente"
            >
                🤖
            </div>
        );
    }

    /* ==========================
       Chat abierto
    ========================== */

    return (
        <div className="ai-chat ai-chat-fixed">
            <div className="ai-chat-container">

                {/* Header */}
                <div className="ai-chat-header">
                    <span>
                        Chat IA {employeeName && `– ${employeeName}`}
                    </span>
                    <div
                        className="ai-chat-close"
                        onClick={() => setOpen(false)}
                        title="Cerrar"
                    >
                        <span />
                        <span />
                    </div>
                </div>

                {/* Mensajes */}
                <div className="ai-chat-messages">
                    {messages.map((m, i) => (
                        <div
                            key={i}
                            className={`ai-chat-message ${m.from}`}
                        >
                            {m.text}
                        </div>
                    ))}

                    {loading && (
                        <div className="ai-chat-message assistant">
                            Escribiendo…
                        </div>
                    )}
                </div>

                {/* Input */}
                <div className="ai-chat-input-area">
                    <input
                        type="text"
                        placeholder="Escribe un mensaje…"
                        value={input}
                        onChange={e => setInput(e.target.value)}
                        onKeyDown={e => e.key === "Enter" && sendMessage()}
                        disabled={loading}
                    />
                    <button onClick={sendMessage} disabled={loading}>
                        ➤
                    </button>
                </div>
            </div>
        </div>
    );
}

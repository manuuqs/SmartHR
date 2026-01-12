import React from "react";
import "../styles/InfoCard.css";

export default function InfoCard({ title, children }) {
    return (
        <section className="info-card">
            <h2>{title}</h2>
            <div className="info-card-content">{children}</div>
        </section>
    );
}

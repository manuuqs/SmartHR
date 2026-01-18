import React, { useState, useEffect } from "react";
import styled from "styled-components";

export default function ThemeSwitch() {
    const [darkMode, setDarkMode] = useState(() => {
        const savedTheme = localStorage.getItem("theme");
        return savedTheme ? savedTheme === "dark" : true;
    });

    useEffect(() => {
        const theme = darkMode ? "dark" : "light";
        document.body.setAttribute("data-theme", theme);
        localStorage.setItem("theme", theme);

        const replaceFocusBg = (toLight) => {
            try {
                const root = document.documentElement;
                const newBg = getComputedStyle(root).getPropertyValue("--input-focus-bg") || "#eaf4ff";
                const newText = getComputedStyle(root).getPropertyValue("--text-color") || "#212121";
                const els = document.querySelectorAll("input, textarea, .display-input, [role='textbox']");
                els.forEach((el) => {
                    const cs = getComputedStyle(el);
                    const bg = cs.backgroundColor || "";
                    const has213547 =
                        bg === "rgb(33, 53, 71)" ||
                        bg === "rgba(33, 53, 71, 1)" ||
                        (el.getAttribute("style") || "").toLowerCase().includes("213547");

                    if (toLight && has213547) {
                        el.style.setProperty("background-color", newBg, "important");
                        el.style.setProperty("color", newText, "important");
                        el.setAttribute("data-replaced-bg", "true");
                    } else if (!toLight && el.getAttribute("data-replaced-bg") === "true") {
                        el.style.removeProperty("background-color");
                        el.style.removeProperty("color");
                        el.removeAttribute("data-replaced-bg");
                    }
                });
            } catch {}
        };

        setTimeout(() => replaceFocusBg(!darkMode), 100);
        return () => replaceFocusBg(false);
    }, [darkMode]);


    return (
        <Wrapper>
            <label className="switch">
                <input
                    type="checkbox"
                    checked={darkMode}
                    onChange={() => setDarkMode(!darkMode)}
                />
                <span className="slider">
          <div className="star star_1" />
          <div className="star star_2" />
          <div className="star star_3" />
          <svg viewBox="0 0 16 16" className="cloud cloud_1">
            <path
                transform="matrix(.77976 0 0 .78395-299.99-418.63)"
                fill="#fff"
                d="m391.84 540.91c-.421-.329-.949-.524-1.523-.524-1.351 0-2.451 1.084-2.485 2.435-1.395.526-2.388 1.88-2.388 3.466 0 1.874 1.385 3.423 3.182 3.667v.034h12.73v-.006c1.775-.104 3.182-1.584 3.182-3.395 0-1.747-1.309-3.186-2.994-3.379.007-.106.011-.214.011-.322 0-2.707-2.271-4.901-5.072-4.901-2.073 0-3.856 1.202-4.643 2.925"
            />
          </svg>
        </span>
            </label>
        </Wrapper>
    );
}

const Wrapper = styled.div`
    position: fixed;
    top: 16px;
    right: 16px;
    z-index: 999; /* que quede encima de todo */

    .switch {
        font-size: 17px;
        position: relative;
        display: inline-block;
        width: 4em;
        height: 2.2em;
        border-radius: 30px;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
    }

    .switch input {
        opacity: 0;
        width: 0;
        height: 0;
    }

    .slider {
        position: absolute;
        cursor: pointer;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: #2a2a2a;
        transition: 0.4s;
        border-radius: 30px;
        overflow: hidden;
    }

    .slider:before {
        position: absolute;
        content: "";
        height: 1.2em;
        width: 1.2em;
        border-radius: 20px;
        left: 0.5em;
        bottom: 0.5em;
        transition: 0.4s;
        transition-timing-function: cubic-bezier(0.81, -0.04, 0.38, 1.5);
        box-shadow: inset 8px -4px 0 0 #fff;
    }

    .star {
        background-color: #fff;
        border-radius: 50%;
        position: absolute;
        width: 5px;
        height: 5px;
        transition: all 0.4s;
    }

    .star_1 {
        left: 2.5em;
        top: 0.5em;
    }

    .star_2 {
        left: 2.2em;
        top: 1.2em;
    }

    .star_3 {
        left: 3em;
        top: 0.9em;
    }

    .cloud {
        width: 3.5em;
        position: absolute;
        bottom: -1.4em;
        left: -1.1em;
        opacity: 0;
        transition: all 0.4s;
    }

    .switch input:checked + .slider:before {
        transform: translateX(1.8em);
        box-shadow: inset 15px -4px 0 15px #ffcf48;
    }

    .switch input:checked + .slider {
        background-color: #00a6ff;
    }

    .switch input:checked + .slider .star {
        opacity: 0;
    }

    .switch input:checked + .slider .cloud {
        opacity: 1;
    }
`;

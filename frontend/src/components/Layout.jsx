import ThemeSwitch from "./ThemeSwitch.jsx";

export default function Layout({ children }) {
    return (
        <div className="page-container relative min-h-screen">

            {/* ThemeSwitch en la esquina superior derecha */}
            <div className="absolute top-4 right-4">
                <ThemeSwitch />
            </div>

            {/* Contenido principal */}
            <div>{children}</div>
        </div>
    );
}

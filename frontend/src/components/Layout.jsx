import ThemeSwitch from "./ThemeSwitch.jsx";
import "../styles/Layout.css";

export default function Layout({ children, employeeCard }) {
    return (
        <>
            {employeeCard && <div className="employee-card-fixed">{employeeCard}</div>}
            <div className="theme-switch-wrapper"><ThemeSwitch /></div>
            <div className="page-container">{children}</div>
        </>
    );
}

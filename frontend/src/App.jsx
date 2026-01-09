import { BrowserRouter, Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import EmployeeDashboard from "./pages/EmployeeDashboard";
import RRHHDashboard from "./pages/RRHHDashboard";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Login />} />
                <Route path="/employee" element={<EmployeeDashboard />} />
                <Route path="/rrhh" element={<RRHHDashboard />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;

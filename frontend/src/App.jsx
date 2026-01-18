import { BrowserRouter, Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import EmployeeDashboard from "./pages/EmployeeDashboard";
import RRHHDashboard from "./pages/RRHHDashboard";
import NewEmployee from "./pages/NewEmployee.jsx";


function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Login />} />
                <Route path="/employee" element={<EmployeeDashboard />} />
                <Route path="/rrhh" element={<RRHHDashboard />} />
                <Route path="/rrhh/new-employee" element={<NewEmployee />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;

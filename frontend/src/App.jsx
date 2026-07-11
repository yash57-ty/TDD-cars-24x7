import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';

// Placeholder components that we will create next
const DashboardPage = () => <div className="page-container"><h2>Dashboard Page Coming Soon</h2></div>;
const LoginPage = () => <div className="page-container"><h2>Login Page Coming Soon</h2></div>;
const RegisterPage = () => <div className="page-container"><h2>Register Page Coming Soon</h2></div>;
const AdminPage = () => <div className="page-container"><h2>Admin Page Coming Soon</h2></div>;

function App() {
  return (
    <Router>
      <div id="app-wrapper">
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/admin" element={<AdminPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;

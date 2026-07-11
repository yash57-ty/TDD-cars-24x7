import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * ProtectedRoute — A route guard that enforces authentication and optional role checks.
 *
 * Props:
 *  - children: the component to render if access is granted
 *  - requireAdmin: if true, user must have ADMIN role; otherwise just being authenticated is enough
 *  - redirectTo: where to redirect if access is denied (default: '/login')
 */
const ProtectedRoute = ({ children, requireAdmin = false, redirectTo = '/login' }) => {
  const { isAuthenticated, isAdmin, loading } = useAuth();

  // Wait for auth state to resolve before making routing decision
  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
      </div>
    );
  }

  // Not authenticated at all → redirect to login
  if (!isAuthenticated) {
    return <Navigate to={redirectTo} replace />;
  }

  // Authenticated but admin required and user is not admin → redirect to dashboard
  if (requireAdmin && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  // Access granted
  return children;
};

export default ProtectedRoute;

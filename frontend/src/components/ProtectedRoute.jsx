import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const ProtectedRoute = ({ children, requiredRole }) => {
  const { isAuthenticated, user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="flex flex-col items-center space-y-4">
          <div className="w-10 h-10 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
          <p className="text-sm font-medium text-slate-600">Loading JALTRACKER Platform...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Superadmin has master access to all platform views
  if (user?.role === 'ROLE_SUPERADMIN') {
    return children;
  }

  if (requiredRole && user?.role !== requiredRole) {
    if (user?.role === 'ROLE_ADMIN') {
      return <Navigate to="/admin/dashboard" replace />;
    } else if (user?.role === 'ROLE_RESIDENT') {
      return <Navigate to="/resident/dashboard" replace />;
    } else {
      return <Navigate to="/login" state={{ from: location }} replace />;
    }
  }

  return children;
};

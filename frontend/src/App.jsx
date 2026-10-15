import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { WebSocketProvider } from './context/WebSocketContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Layout } from './components/Layout';
import { ErrorBoundary } from './components/ErrorBoundary';
import { LoginView } from './pages/LoginView';
import { ResidentRegistration } from './pages/ResidentRegistration';

// Admin Views
import { AdminDashboard } from './pages/admin/AdminDashboard';
import { BulkPurchaseView } from './pages/admin/BulkPurchaseView';
import { TariffConfigView } from './pages/admin/TariffConfigView';
import { MeterReadingView } from './pages/admin/MeterReadingView';
import { BillingCyclesView } from './pages/admin/BillingCyclesView';
import { UsageComparisonView } from './pages/admin/UsageComparisonView';
import { HouseholdsView } from './pages/admin/HouseholdsView';
import { UserVerificationsView } from './pages/admin/UserVerificationsView';

// Resident Views
import { ResidentDashboard } from './pages/resident/ResidentDashboard';
import { ResidentComparisonView } from './pages/resident/ResidentComparisonView';
import { ResidentInvoicesView } from './pages/resident/ResidentInvoicesView';

const RootRedirect = () => {
  const { isAuthenticated, user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-900 text-white">
        <div className="flex flex-col items-center space-y-4">
          <div className="w-10 h-10 border-4 border-sky-400 border-t-transparent rounded-full animate-spin"></div>
          <p className="text-sm font-medium text-slate-400">Loading JALTRACKER Platform...</p>
        </div>
      </div>
    );
  }
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  if (user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_SUPERADMIN') {
    return <Navigate to="/admin/dashboard" replace />;
  }
  return <Navigate to="/resident/dashboard" replace />;
};

export const App = () => {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <WebSocketProvider>
          <BrowserRouter>
            <Routes>
              {/* Public Authentication */}
              <Route path="/login" element={<LoginView />} />
              <Route path="/register" element={<ResidentRegistration />} />

              {/* Root Redirect */}
              <Route path="/" element={<RootRedirect />} />

              {/* Admin Protected Routes */}
              <Route
                path="/admin"
                element={
                  <ProtectedRoute requiredRole="ROLE_ADMIN">
                    <Layout />
                  </ProtectedRoute>
                }
              >
                <Route index element={<Navigate to="/admin/dashboard" replace />} />
                <Route path="dashboard" element={<AdminDashboard />} />
                <Route path="verifications" element={<UserVerificationsView />} />
                <Route path="bulk-purchases" element={<BulkPurchaseView />} />
                <Route path="tariffs" element={<TariffConfigView />} />
                <Route path="meter-readings" element={<MeterReadingView />} />
                <Route path="billing-cycles" element={<BillingCyclesView />} />
                <Route path="usage-comparison" element={<UsageComparisonView />} />
                <Route path="households" element={<HouseholdsView />} />
              </Route>

              {/* Resident Protected Routes */}
              <Route
                path="/resident"
                element={
                  <ProtectedRoute requiredRole="ROLE_RESIDENT">
                    <Layout />
                  </ProtectedRoute>
                }
              >
                <Route index element={<Navigate to="/resident/dashboard" replace />} />
                <Route path="dashboard" element={<ResidentDashboard />} />
                <Route path="comparison" element={<ResidentComparisonView />} />
                <Route path="invoices" element={<ResidentInvoicesView />} />
              </Route>

              {/* Catch-all fallback */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </BrowserRouter>
        </WebSocketProvider>
      </AuthProvider>
    </ErrorBoundary>
  );
};

export default App;

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';

import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import OnboardingPage from './pages/OnboardingPage';
import DashboardPage from './pages/DashboardPage';
import CrisisPage from './pages/CrisisPage';
import CheckInPage from './pages/CheckInPage';
import ScriptGeneratorPage from './pages/ScriptGeneratorPage';
import CaregiverPage from './pages/CaregiverPage';
import ResourcesPage from './pages/ResourcesPage';
import SafetyPlanPage from './pages/SafetyPlanPage';

function AppLayout({ children }) {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main>{children}</main>
    </div>
  );
}

function AppRoutes() {
  const { user } = useAuth();
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />

      {/* Onboarding (protected) */}
      <Route path="/onboarding" element={
        <ProtectedRoute>
          <OnboardingPage />
        </ProtectedRoute>
      } />

      {/* Protected with nav */}
      <Route path="/dashboard" element={
        <ProtectedRoute>
          <AppLayout><DashboardPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/crisis" element={
        <ProtectedRoute>
          <AppLayout><CrisisPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/checkin" element={
        <ProtectedRoute>
          <AppLayout><CheckInPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/scripts" element={
        <ProtectedRoute>
          <AppLayout><ScriptGeneratorPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/caregiver" element={
        <ProtectedRoute>
          <AppLayout><CaregiverPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/resources" element={
        <ProtectedRoute>
          <AppLayout><ResourcesPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/safety-plan" element={
        <ProtectedRoute>
          <AppLayout><SafetyPlanPage /></AppLayout>
        </ProtectedRoute>
      } />

      {/* Default */}
      <Route path="/" element={<Navigate to={user ? "/dashboard" : "/login"} replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}

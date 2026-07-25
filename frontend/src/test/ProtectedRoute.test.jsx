import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import ProtectedRoute from '../../components/ProtectedRoute';

// Mock AuthContext
vi.mock('../../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

import { useAuth } from '../../context/AuthContext';

function renderProtectedRoute(userValue, loadingValue = false) {
  useAuth.mockReturnValue({ user: userValue, loading: loadingValue });

  return render(
    <MemoryRouter initialEntries={['/dashboard']}>
      <Routes>
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <div data-testid="protected-content">Protected Content</div>
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<div data-testid="login-page">Login</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe('ProtectedRoute', () => {
  test('renders children when user is authenticated', () => {
    renderProtectedRoute({ name: 'John', role: 'INDIVIDUAL' });
    expect(screen.getByTestId('protected-content')).toBeInTheDocument();
  });

  test('redirects to /login when user is null', () => {
    renderProtectedRoute(null);
    expect(screen.getByTestId('login-page')).toBeInTheDocument();
    expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
  });

  test('shows loading spinner while loading is true', () => {
    renderProtectedRoute(null, true);
    expect(screen.getByText(/loading recoverease ai/i)).toBeInTheDocument();
    expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
    expect(screen.queryByTestId('login-page')).not.toBeInTheDocument();
  });

  test('shows children when loading is false and user exists', () => {
    renderProtectedRoute({ name: 'Jane', role: 'CAREGIVER' }, false);
    expect(screen.getByTestId('protected-content')).toBeInTheDocument();
  });
});

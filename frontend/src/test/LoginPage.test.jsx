import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import LoginPage from '../../pages/LoginPage';

// Mock dependencies
vi.mock('../../api/authApi', () => ({
  login: vi.fn(),
}));

vi.mock('../../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

import { login } from '../../api/authApi';
import { useAuth } from '../../context/AuthContext';

function renderLoginPage() {
  const mockLoginUser = vi.fn();
  useAuth.mockReturnValue({ loginUser: mockLoginUser });

  render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>
  );

  return { mockLoginUser };
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('renders email and password fields', () => {
    renderLoginPage();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  });

  test('renders Sign In button', () => {
    renderLoginPage();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  test('renders link to signup page', () => {
    renderLoginPage();
    expect(screen.getByRole('link', { name: /create account/i })).toBeInTheDocument();
  });

  test('successful login with onboarding complete navigates to /dashboard', async () => {
    login.mockResolvedValueOnce({ data: { token: 'tok', onboardingComplete: true } });
    const { mockLoginUser } = renderLoginPage();

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'password123' } });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(mockLoginUser).toHaveBeenCalledWith({ token: 'tok', onboardingComplete: true });
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  test('successful login without onboarding navigates to /onboarding', async () => {
    login.mockResolvedValueOnce({ data: { token: 'tok', onboardingComplete: false } });
    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'password123' } });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/onboarding');
    });
  });

  test('shows error message on login failure', async () => {
    login.mockRejectedValueOnce({
      response: { data: { error: 'Invalid credentials' } },
    });
    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'bad@example.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'wrongpass' } });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Invalid credentials');
    });
  });

  test('shows fallback error message when no server message', async () => {
    login.mockRejectedValueOnce(new Error('Network Error'));
    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'pass123' } });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Login failed. Please try again.');
    });
  });

  test('button shows "Signing in..." while loading', async () => {
    login.mockImplementation(() => new Promise(() => {})); // never resolves
    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'pass123' } });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByRole('button', { name: /signing in/i })).toBeDisabled();
  });

  test('demo button fills individual credentials', () => {
    renderLoginPage();
    fireEvent.click(screen.getByRole('button', { name: /individual user/i }));
    expect(screen.getByLabelText(/email/i)).toHaveValue('testuser@recoverease.com');
  });

  test('demo button fills caregiver credentials', () => {
    renderLoginPage();
    fireEvent.click(screen.getByRole('button', { name: /caregiver/i }));
    expect(screen.getByLabelText(/email/i)).toHaveValue('caregiver@recoverease.com');
  });
});

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import SignupPage from '../../pages/SignupPage';

vi.mock('../../api/authApi', () => ({
  signup: vi.fn(),
}));

vi.mock('../../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

import { signup } from '../../api/authApi';
import { useAuth } from '../../context/AuthContext';

function renderSignupPage() {
  const mockLoginUser = vi.fn();
  useAuth.mockReturnValue({ loginUser: mockLoginUser });

  render(
    <MemoryRouter>
      <SignupPage />
    </MemoryRouter>
  );

  return { mockLoginUser };
}

describe('SignupPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('renders name, email, password fields', () => {
    renderSignupPage();
    expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  });

  test('renders Create Account button', () => {
    renderSignupPage();
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
  });

  test('renders link to login page', () => {
    renderSignupPage();
    expect(screen.getByRole('link', { name: /sign in/i })).toBeInTheDocument();
  });

  test('successful signup calls loginUser and navigates to /onboarding', async () => {
    signup.mockResolvedValueOnce({ data: { token: 'tok', name: 'John', email: 'j@x.com', role: 'INDIVIDUAL', onboardingComplete: false } });
    const { mockLoginUser } = renderSignupPage();

    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: 'John Doe' } });
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'john@example.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'password123' } });
    fireEvent.click(screen.getByRole('button', { name: /create account/i }));

    await waitFor(() => {
      expect(mockLoginUser).toHaveBeenCalled();
      expect(mockNavigate).toHaveBeenCalledWith('/onboarding');
    });
  });

  test('shows error on signup failure', async () => {
    signup.mockRejectedValueOnce({
      response: { data: { error: 'Email already registered' } },
    });
    renderSignupPage();

    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: 'John' } });
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'existing@example.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'pass123' } });
    fireEvent.click(screen.getByRole('button', { name: /create account/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Email already registered');
    });
  });

  test('shows fallback error message when no server error', async () => {
    signup.mockRejectedValueOnce(new Error('Network Error'));
    renderSignupPage();

    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: 'John' } });
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'john@x.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'pass123' } });
    fireEvent.click(screen.getByRole('button', { name: /create account/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Sign up failed. Please try again.');
    });
  });

  test('defaults to INDIVIDUAL role', () => {
    renderSignupPage();
    // INDIVIDUAL button should be "pressed" by default
    expect(screen.getByRole('button', { name: /👤 individual/i })).toHaveAttribute('aria-pressed', 'true');
    expect(screen.getByRole('button', { name: /🤝 caregiver/i })).toHaveAttribute('aria-pressed', 'false');
  });

  test('selecting CAREGIVER role toggles aria-pressed', () => {
    renderSignupPage();
    fireEvent.click(screen.getByRole('button', { name: /🤝 caregiver/i }));
    expect(screen.getByRole('button', { name: /🤝 caregiver/i })).toHaveAttribute('aria-pressed', 'true');
    expect(screen.getByRole('button', { name: /👤 individual/i })).toHaveAttribute('aria-pressed', 'false');
  });

  test('button shows "Creating account..." while loading', async () => {
    signup.mockImplementation(() => new Promise(() => {})); // never resolves
    renderSignupPage();

    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: 'John' } });
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'john@x.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'pass123' } });
    fireEvent.click(screen.getByRole('button', { name: /create account/i }));

    expect(await screen.findByRole('button', { name: /creating account/i })).toBeDisabled();
  });
});

import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import Navbar from '../../components/Navbar';

vi.mock('../../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

import { useAuth } from '../../context/AuthContext';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

function renderNavbar(user) {
  useAuth.mockReturnValue({ user, logoutUser: vi.fn() });

  return render(
    <MemoryRouter initialEntries={['/dashboard']}>
      <Routes>
        <Route path="*" element={<Navbar />} />
      </Routes>
    </MemoryRouter>
  );
}

describe('Navbar', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
  });

  test('renders brand link for authenticated user', () => {
    renderNavbar({ name: 'John', role: 'INDIVIDUAL' });
    expect(screen.getAllByText(/RecoverEase AI/i).length).toBeGreaterThan(0);
  });

  test('shows individual nav links for INDIVIDUAL role', () => {
    renderNavbar({ name: 'John', role: 'INDIVIDUAL' });
    expect(screen.getAllByLabelText('Crisis Help').length).toBeGreaterThan(0);
    expect(screen.getAllByLabelText('Check-in').length).toBeGreaterThan(0);
    expect(screen.getAllByLabelText('Scripts').length).toBeGreaterThan(0);
  });

  test('shows caregiver nav links for CAREGIVER role', () => {
    renderNavbar({ name: 'Carol', role: 'CAREGIVER' });
    expect(screen.getAllByLabelText('Caregiver Guide').length).toBeGreaterThan(0);
    // Crisis Help should NOT be visible for caregiver
    expect(screen.queryByLabelText('Crisis Help')).not.toBeInTheDocument();
  });

  test('displays first name of user', () => {
    renderNavbar({ name: 'John Doe', role: 'INDIVIDUAL' });
    expect(screen.getByText('John')).toBeInTheDocument();
  });

  test('renders Logout button', () => {
    renderNavbar({ name: 'John', role: 'INDIVIDUAL' });
    expect(screen.getByLabelText('Logout')).toBeInTheDocument();
  });

  test('logout button calls logoutUser and navigates to /login', () => {
    const mockLogout = vi.fn();
    useAuth.mockReturnValue({ user: { name: 'John', role: 'INDIVIDUAL' }, logoutUser: mockLogout });

    render(
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    );

    fireEvent.click(screen.getByLabelText('Logout'));
    expect(mockLogout).toHaveBeenCalledTimes(1);
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  test('shows caregiver badge for CAREGIVER role', () => {
    renderNavbar({ name: 'Carol', role: 'CAREGIVER' });
    expect(screen.getByText(/Caregiver/i)).toBeInTheDocument();
  });

  test('shows individual badge for INDIVIDUAL role', () => {
    renderNavbar({ name: 'John', role: 'INDIVIDUAL' });
    expect(screen.getByText(/Individual/i)).toBeInTheDocument();
  });
});

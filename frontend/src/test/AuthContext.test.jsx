import { render, act } from '@testing-library/react';
import { AuthProvider, useAuth } from '../../context/AuthContext';

// Helper component to expose context values
function AuthConsumer({ onRender }) {
  const ctx = useAuth();
  onRender(ctx);
  return null;
}

function renderWithAuth(onRender) {
  return render(
    <AuthProvider>
      <AuthConsumer onRender={onRender} />
    </AuthProvider>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  test('initial state: user and token are null, loading becomes false', async () => {
    let ctx;
    await act(async () => {
      renderWithAuth((c) => { ctx = c; });
    });

    expect(ctx.user).toBeNull();
    expect(ctx.token).toBeNull();
    expect(ctx.loading).toBe(false);
  });

  test('restores user and token from localStorage on mount', async () => {
    const authData = {
      token: 'stored-token',
      name: 'Jane',
      email: 'jane@example.com',
      role: 'INDIVIDUAL',
      onboardingComplete: true,
    };
    localStorage.setItem('recoverease_token', 'stored-token');
    localStorage.setItem('recoverease_user', JSON.stringify(authData));

    let ctx;
    await act(async () => {
      renderWithAuth((c) => { ctx = c; });
    });

    expect(ctx.token).toBe('stored-token');
    expect(ctx.user.email).toBe('jane@example.com');
  });

  test('loginUser sets user, token, and persists to localStorage', async () => {
    let ctx;
    await act(async () => {
      renderWithAuth((c) => { ctx = c; });
    });

    const authResponse = {
      token: 'new-token',
      name: 'John',
      email: 'john@example.com',
      role: 'INDIVIDUAL',
      onboardingComplete: false,
    };

    await act(async () => {
      ctx.loginUser(authResponse);
    });

    expect(ctx.token).toBe('new-token');
    expect(ctx.user.email).toBe('john@example.com');
    expect(localStorage.getItem('recoverease_token')).toBe('new-token');
    expect(JSON.parse(localStorage.getItem('recoverease_user')).email).toBe('john@example.com');
  });

  test('logoutUser clears user, token, and localStorage', async () => {
    localStorage.setItem('recoverease_token', 'token');
    localStorage.setItem('recoverease_user', JSON.stringify({ name: 'John' }));

    let ctx;
    await act(async () => {
      renderWithAuth((c) => { ctx = c; });
    });

    await act(async () => {
      ctx.logoutUser();
    });

    expect(ctx.user).toBeNull();
    expect(ctx.token).toBeNull();
    expect(localStorage.getItem('recoverease_token')).toBeNull();
    expect(localStorage.getItem('recoverease_user')).toBeNull();
  });

  test('updateOnboarding sets onboardingComplete to true', async () => {
    const authData = {
      token: 'tok',
      name: 'Sam',
      email: 'sam@example.com',
      role: 'INDIVIDUAL',
      onboardingComplete: false,
    };
    localStorage.setItem('recoverease_token', 'tok');
    localStorage.setItem('recoverease_user', JSON.stringify(authData));

    let ctx;
    await act(async () => {
      renderWithAuth((c) => { ctx = c; });
    });

    await act(async () => {
      ctx.updateOnboarding();
    });

    expect(ctx.user.onboardingComplete).toBe(true);
    const stored = JSON.parse(localStorage.getItem('recoverease_user'));
    expect(stored.onboardingComplete).toBe(true);
  });

  test('updateOnboarding does nothing when user is null', async () => {
    let ctx;
    await act(async () => {
      renderWithAuth((c) => { ctx = c; });
    });

    // Should not throw
    await act(async () => {
      ctx.updateOnboarding();
    });

    expect(ctx.user).toBeNull();
  });
});

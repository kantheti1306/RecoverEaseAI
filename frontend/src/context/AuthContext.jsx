import { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('recoverease_token');
    const storedUser = localStorage.getItem('recoverease_user');
    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const loginUser = (authResponse) => {
    localStorage.setItem('recoverease_token', authResponse.token);
    localStorage.setItem('recoverease_user', JSON.stringify(authResponse));
    setToken(authResponse.token);
    setUser(authResponse);
  };

  const logoutUser = () => {
    localStorage.removeItem('recoverease_token');
    localStorage.removeItem('recoverease_user');
    setToken(null);
    setUser(null);
  };

  const updateOnboarding = () => {
    if (user) {
      const updated = { ...user, onboardingComplete: true };
      localStorage.setItem('recoverease_user', JSON.stringify(updated));
      setUser(updated);
    }
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, loginUser, logoutUser, updateOnboarding }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

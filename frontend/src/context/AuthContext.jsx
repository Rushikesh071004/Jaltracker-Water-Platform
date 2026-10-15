import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axiosClient';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('token');
      const storedUser = localStorage.getItem('user');

      if (storedToken && storedUser) {
        try {
          setUser(JSON.parse(storedUser));
          setToken(storedToken);
          // Verify with /api/auth/me in background
          const res = await api.get('/auth/me');
          setUser((prev) => ({ ...prev, ...res.data }));
          localStorage.setItem('user', JSON.stringify(res.data));
        } catch (err) {
          console.warn('Session expired or invalid token:', err);
          logout();
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (usernameOrEmail, password) => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);

    const payload = {
      username: usernameOrEmail,
      email: usernameOrEmail,
      password,
    };
    const res = await api.post('/auth/login', payload);
    const { token: jwtToken, ...userData } = res.data;
    localStorage.setItem('token', jwtToken);
    localStorage.setItem('user', JSON.stringify(userData));
    setToken(jwtToken);
    setUser(userData);
    return userData;
  };

  const register = async (registerData) => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);

    const res = await api.post('/auth/register', registerData);
    const { token: jwtToken, ...userData } = res.data;
    localStorage.setItem('token', jwtToken);
    localStorage.setItem('user', JSON.stringify(userData));
    setToken(jwtToken);
    setUser(userData);
    return userData;
  };

  const loginWithOtp = async (email, otp) => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);

    const res = await api.post('/auth/verify-otp', { email, otp });
    const { token: jwtToken, ...userData } = res.data;
    localStorage.setItem('token', jwtToken);
    localStorage.setItem('user', JSON.stringify(userData));
    setToken(jwtToken);
    setUser(userData);
    return userData;
  };

  const sendOtp = async (email) => {
    const res = await api.post('/auth/send-otp', { email });
    return res.data;
  };

  const sendLoginOtp = async (identifier, password) => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);

    const res = await api.post('/sendOtp', { identifier, password });
    return res.data;
  };

  const validateLoginOtp = async (identifier, otp) => {
    const res = await api.post('/validateOtp', { identifier, otp });
    const { token: jwtToken, ...userData } = res.data;
    localStorage.setItem('token', jwtToken);
    localStorage.setItem('user', JSON.stringify(userData));
    setToken(jwtToken);
    setUser(userData);
    return userData;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  const isSuperAdmin = user?.role === 'ROLE_SUPERADMIN';
  const isAdmin = user?.role === 'ROLE_ADMIN' || isSuperAdmin;
  const isResident = user?.role === 'ROLE_RESIDENT';

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        role: user?.role || null,
        accountStatus: user?.accountStatus || 'VERIFIED',
        isAuthenticated: !!token && !!user,
        isSuperAdmin,
        isAdmin,
        isResident,
        loading,
        login,
        loginWithOtp,
        sendOtp,
        sendLoginOtp,
        validateLoginOtp,
        register,
        logout,
        setUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

import { createContext, useContext, useState, useEffect, ReactNode } from 'react';

interface AuthContextType {
  userId: number | null;
  username: string | null;
  role: string | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (token: string, userId: number, username: string, role: string) => void;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [userId, setUserId] = useState<number | null>(null);
  const [username, setUsername] = useState<string | null>(null);
  const [role, setRole] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Restore auth state from localStorage on mount
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUserId = localStorage.getItem('userId');
    const storedUsername = localStorage.getItem('username');
    const storedRole = localStorage.getItem('role');

    if (storedToken) {
      setToken(storedToken);
      setUserId(storedUserId ? Number(storedUserId) : null);
      setUsername(storedUsername);
      setRole(storedRole);
    }
    setIsLoading(false);
  }, []);

  const login = (newToken: string, newUserId: number, newUsername: string, newRole: string) => {
    setToken(newToken);
    setUserId(newUserId);
    setUsername(newUsername);
    setRole(newRole);
    localStorage.setItem('token', newToken);
    localStorage.setItem('userId', String(newUserId));
    localStorage.setItem('username', newUsername);
    localStorage.setItem('role', newRole);
  };

  const logout = () => {
    setToken(null);
    setUserId(null);
    setUsername(null);
    setRole(null);
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    window.location.hash = '#/login';
  };

  const value: AuthContextType = {
    userId,
    username,
    role,
    token,
    isAuthenticated: !!token,
    login,
    logout,
    isLoading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

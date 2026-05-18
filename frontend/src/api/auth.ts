import http from './http';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
  role: string;
  username: string;
  userId: number;
}

export interface CurrentUser {
  username: string;
  role: string;
  userId: number;
  authorities: string[];
}

export const authApi = {
  login: (data: LoginRequest) =>
    http.post<LoginResponse>('/auth/login', data),

  getCurrentUser: () =>
    http.get<CurrentUser>('/auth/me'),
};

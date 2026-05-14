import http from './http';
import type { MaintenanceRecord } from '../types';

interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const maintenanceApi = {
  list: (params: Record<string, unknown> = {}) => {
    return http.get<PageResult<MaintenanceRecord>>('/maintenance-records', { params });
  },

  delete: (id: number) => {
    return http.delete(`/maintenance-records/${id}`);
  },

  update: (id: number, payload: Record<string, unknown>) => {
    return http.put(`/maintenance-records/${id}`, payload);
  },
};

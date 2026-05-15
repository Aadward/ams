import http from './http';
import type { PageResult } from '../types';

export interface Supplier {
  id: number;
  name: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  remark?: string;
  createdAt: string;
  updatedAt: string;
}

interface SupplierFormData {
  name: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  remark?: string;
}

export const supplierApi = {
  list: (params: Record<string, unknown> = {}) => {
    return http.get<PageResult<Supplier>>('/suppliers', { params });
  },

  getById: (id: number) => {
    return http.get<Supplier>(`/suppliers/${id}`);
  },

  create: (data: SupplierFormData) => {
    return http.post('/suppliers', data);
  },

  update: (id: number, data: SupplierFormData) => {
    return http.put(`/suppliers/${id}`, data);
  },

  delete: (id: number) => {
    return http.delete(`/suppliers/${id}`);
  },
};

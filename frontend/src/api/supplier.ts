import http from './http';
import type { PageResult } from '../types';

export interface Supplier {
  id: number;
  supplierCode: string;
  name: string;
  type: string;
  status: string;
  contact?: string;
  phone?: string;
  email?: string;
  address?: string;
  remark?: string;
  rating?: number;
  createdAt: string;
  updatedAt: string;
}

interface SupplierFormData {
  supplierCode: string;
  name: string;
  type: string;
  status?: string;
  contact?: string;
  phone?: string;
  email?: string;
  address?: string;
  remark?: string;
  rating?: number;
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

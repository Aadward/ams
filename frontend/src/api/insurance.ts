import http from './http';
import type { PageResult } from '../types';

export interface InsuranceRecord {
  id: number;
  assetId: number;
  assetCode: string;
  assetName: string;
  policyNumber: string;
  insuranceCompany: string;
  insuranceType: string;
  coverageAmount: number;
  premium: number;
  startDate: string;
  endDate: string;
  status: 'ACTIVE' | 'EXPIRED' | 'CANCELLED';
  claimAmount?: number;
  claimDate?: string;
  claimStatus?: 'NONE' | 'PENDING' | 'APPROVED' | 'REJECTED';
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

interface InsuranceFormData {
  assetId: number;
  policyNumber: string;
  insuranceCompany: string;
  insuranceType: string;
  coverageAmount: number;
  premium: number;
  startDate: string;
  endDate: string;
  remarks?: string;
}

interface ClaimFormData {
  insuranceId: number;
  claimAmount: number;
  claimReason: string;
}

export const insuranceApi = {
  list: (params: Record<string, unknown> = {}) => {
    return http.get<PageResult<InsuranceRecord>>('/insurances', { params });
  },

  getById: (id: number) => {
    return http.get<InsuranceRecord>(`/insurances/${id}`);
  },

  create: (data: InsuranceFormData) => {
    return http.post('/insurances', data);
  },

  update: (id: number, data: InsuranceFormData) => {
    return http.put(`/insurances/${id}`, data);
  },

  delete: (id: number) => {
    return http.delete(`/insurances/${id}`);
  },

  submitClaim: (data: ClaimFormData) => {
    return http.post('/insurances/claims', data);
  },

  getExpiring: (days: number = 30) => {
    return http.get<InsuranceRecord[]>('/insurances/expiring', { params: { days } });
  },
};

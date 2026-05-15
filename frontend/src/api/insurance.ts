import http from './http';
import type { PageResult } from '../types';

// Policy types
export interface InsuranceRecord {
  id: number;
  assetId: number;
  assetCode: string;
  assetName: string;
  policyNumber: string;
  insuranceCompany: string;
  type: string;
  coverageAmount: number;
  premium?: number;
  startDate: string;
  endDate: string;
  status: 'ACTIVE' | 'EXPIRED' | 'CANCELLED';
  policyDocument?: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

// Claim types
export interface ClaimRecord {
  id: number;
  claimNumber: string;
  policyId: number;
  policyNumber: string;
  assetId: number;
  assetName: string;
  assetCode: string;
  incidentDate: string;
  claimAmount: number;
  settledAmount?: number;
  status: 'PENDING' | 'SETTLED' | 'REJECTED';
  incidentDescription?: string;
  settlementNotes?: string;
  filedBy?: number;
  createdAt: string;
  updatedAt: string;
}

export interface InsuranceFormData {
  assetId: number;
  policyNumber: string;
  insuranceCompany: string;
  insuranceType: string;
  coverageAmount: number;
  premium?: number;
  startDate: string;
  endDate: string;
  remarks?: string;
}

export interface ClaimFormData {
  policyId: number;
  claimNumber: string;
  incidentDate: string;
  claimAmount: number;
  incidentDescription: string;
}

export const insuranceApi = {
  // Policy APIs
  list: (params: Record<string, unknown> = {}) => {
    return http.get<PageResult<InsuranceRecord>>('/insurance-policies', { params });
  },

  getById: (id: number) => {
    return http.get<InsuranceRecord>(`/insurance-policies/${id}`);
  },

  getByAsset: (assetId: number) => {
    return http.get<InsuranceRecord[]>(`/assets/${assetId}/insurance-policies`);
  },

  create: (data: InsuranceFormData) => {
    return http.post('/insurance-policies', data);
  },

  update: (id: number, data: InsuranceFormData) => {
    return http.put(`/insurance-policies/${id}`, data);
  },

  delete: (id: number) => {
    return http.delete(`/insurance-policies/${id}`);
  },

  // Claim APIs
  listClaims: (params: Record<string, unknown> = {}) => {
    return http.get<PageResult<ClaimRecord>>('/insurance-claims', { params });
  },

  getClaim: (id: number) => {
    return http.get<ClaimRecord>(`/insurance-claims/${id}`);
  },

  getClaimsByPolicy: (policyId: number) => {
    return http.get<ClaimRecord[]>(`/insurance-policies/${policyId}/claims`);
  },

  createClaim: (data: ClaimFormData) => {
    return http.post('/insurance-claims', data);
  },

  updateClaim: (id: number, data: ClaimFormData) => {
    return http.put(`/insurance-claims/${id}`, data);
  },

  settleClaim: (id: number, settledAmount: number, settlementNotes?: string) => {
    return http.put(`/insurance-claims/${id}/settle`, { settledAmount, settlementNotes });
  },

  rejectClaim: (id: number, settlementNotes?: string) => {
    return http.put(`/insurance-claims/${id}/reject`, { settlementNotes });
  },

  deleteClaim: (id: number) => {
    return http.delete(`/insurance-claims/${id}`);
  },
};

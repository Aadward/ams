import http from './http';
import type { Asset } from '../types';

export interface ApprovalRequest {
  id: number;
  requesterId: number;
  requesterName?: string;
  assetId: number;
  assetName?: string;
  departmentId: number;
  departmentName?: string;
  type: string;
  status: string;
  reason: string;
  managerComment?: string;
  createdAt: string;
  resolvedAt?: string;
}

export const approvalApi = {
  listPending: () =>
    http.get<ApprovalRequest[]>('/approvals/pending'),

  listMy: (requesterId: number) =>
    http.get<ApprovalRequest[]>('/approvals/my', { params: { requesterId } }),

  getPendingCount: () =>
    http.get<{ count: number }>('/approvals/pending-count'),

  getMyAssets: () =>
    http.get<Asset[]>('/assets/my'),

  create: (data: { requesterId: number; assetId: number; departmentId: number; type: string; reason: string }) =>
    http.post<ApprovalRequest>('/approvals', data),

  approve: (id: number, managerComment?: string) =>
    http.post(`/approvals/${id}/approve`, { managerComment }),

  reject: (id: number, managerComment?: string) =>
    http.post(`/approvals/${id}/reject`, { managerComment }),
};

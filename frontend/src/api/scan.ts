import { useMutation } from '@tanstack/react-query';
import http from './http';

// Scan response type matching backend
export interface ScanResponse {
  assetId: number;
  assetCode: string;
  assetName: string;
  category: string;
  status: 'IN_STOCK' | 'IN_USE' | 'MAINTENANCE' | 'RETIRED';
  assigneeName?: string;
  location?: string;
  availableActions: string[];
  borrowRecordId?: number;
}

// Scan request type
export interface ScanRequest {
  reason: string;
}

// Get asset info by scan
export function useScanAsset() {
  return useMutation({
    mutationFn: async ({ assetCode, employeeId }: { assetCode: string; employeeId: number }) => {
      const { data } = await http.get<ScanResponse>(`/scan/${assetCode}`, {
        params: { employeeId },
      });
      return data;
    },
  });
}

// Assign asset via scan
export function useScanAssign() {
  return useMutation({
    mutationFn: async ({ assetCode, reason, employeeId }: { assetCode: string; reason: string; employeeId: number }) => {
      const { data } = await http.post<ScanResponse>(`/scan/${assetCode}/assign`, { reason, employeeId });
      return data;
    },
  });
}

// Return asset via scan
export function useScanReturn() {
  return useMutation({
    mutationFn: async ({ assetCode, reason, employeeId }: { assetCode: string; reason: string; employeeId: number }) => {
      const { data } = await http.post<ScanResponse>(`/scan/${assetCode}/return`, { reason, employeeId });
      return data;
    },
  });
}

// Borrow return via scan
export function useScanBorrowReturn() {
  return useMutation({
    mutationFn: async ({ assetCode, employeeId }: { assetCode: string; employeeId: number }) => {
      const { data } = await http.post<ScanResponse>(`/scan/${assetCode}/borrow-return`, { employeeId });
      return data;
    },
  });
}

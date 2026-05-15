import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import http from './http';
import type { TransferRecord, TransferRequest } from '../types/transfer';
import type { PageResult } from '../types';

export function useTransferList(params?: {
  status?: string;
  transferType?: string;
  keyword?: string;
  page?: number;
  size?: number;
}) {
  return useQuery({
    queryKey: ['transfers', params],
    queryFn: async () => {
      const { data } = await http.get<PageResult<TransferRecord>>('/transfers', { params });
      return data;
    },
  });
}

export function useMyTransfers(requesterId: number) {
  return useQuery({
    queryKey: ['transfers', 'my', requesterId],
    queryFn: async () => {
      const { data } = await http.get<TransferRecord[]>('/transfers/my', { params: { requesterId } });
      return data;
    },
    enabled: !!requesterId,
  });
}

export function usePendingTransfers() {
  return useQuery({
    queryKey: ['transfers', 'pending'],
    queryFn: async () => {
      const { data } = await http.get<TransferRequest[]>('/transfers/pending');
      return data;
    },
  });
}

export function useTransfer(id: number) {
  return useQuery({
    queryKey: ['transfer', id],
    queryFn: async () => {
      const { data } = await http.get<TransferRecord>(`/transfers/${id}`);
      return data;
    },
    enabled: !!id,
  });
}

export function useCreateTransfer() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: {
      assetId: number;
      fromDepartmentId: number;
      toDepartmentId: number;
      transferType: 'DEPARTMENT' | 'PERSON';
      fromPersonId?: number;
      toPersonId?: number;
      reason?: string;
    }) => {
      const { data } = await http.post('/transfers/apply', payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['transfers'] }),
  });
}

export function useApproveTransfer() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, managerComment }: { id: number; managerComment?: string }) => {
      const { data } = await http.post<TransferRecord>(`/transfers/${id}/approve`, { managerComment });
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['transfers'] }),
  });
}

export function useRejectTransfer() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, managerComment }: { id: number; managerComment?: string }) => {
      const { data } = await http.post<TransferRecord>(`/transfers/${id}/reject`, { managerComment });
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['transfers'] }),
  });
}

export function useCancelTransfer() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const { data } = await http.post<TransferRecord>(`/transfers/${id}/cancel`);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['transfers'] }),
  });
}
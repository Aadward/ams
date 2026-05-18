import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import http from './http';
import type { ProcurementRecord, ProcurementRequest } from '../types/procurement';
import type { PageResult } from '../types';

export function useProcurementList(params?: {
  status?: string;
  type?: string;
  keyword?: string;
  page?: number;
  size?: number;
}) {
  return useQuery({
    queryKey: ['procurements', params],
    queryFn: async () => {
      const { data } = await http.get<PageResult<ProcurementRecord>>('/procurements', { params });
      return data;
    },
  });
}

export function useMyProcurements(requesterId: number) {
  return useQuery({
    queryKey: ['procurements', 'my', requesterId],
    queryFn: async () => {
      const { data } = await http.get<ProcurementRecord[]>('/procurements/my', { params: { requesterId } });
      return data;
    },
    enabled: !!requesterId,
  });
}

export function usePendingProcurements() {
  return useQuery({
    queryKey: ['procurements', 'pending'],
    queryFn: async () => {
      const { data } = await http.get<ProcurementRequest[]>('/procurements/pending');
      return data;
    },
  });
}

export function useProcurement(id: number) {
  return useQuery({
    queryKey: ['procurement', id],
    queryFn: async () => {
      const { data } = await http.get<ProcurementRecord>(`/procurements/${id}`);
      return data;
    },
    enabled: !!id,
  });
}

export function useCreateProcurement() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: {
      assetId?: number;
      type: 'PURCHASE' | 'CONSUMABLE';
      name: string;
      description?: string;
      quantity: number;
      unitPrice: number;
      reason?: string;
    }) => {
      const { data } = await http.post('/procurements/apply', payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['procurements'] }),
  });
}

export function useApproveProcurement() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, managerComment }: { id: number; managerComment?: string }) => {
      const { data } = await http.post<ProcurementRecord>(`/procurements/${id}/approve`, { managerComment });
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['procurements'] }),
  });
}

export function useRejectProcurement() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, managerComment }: { id: number; managerComment?: string }) => {
      const { data } = await http.post<ProcurementRecord>(`/procurements/${id}/reject`, { managerComment });
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['procurements'] }),
  });
}

export function useCancelProcurement() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const { data } = await http.post<ProcurementRecord>(`/procurements/${id}/cancel`);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['procurements'] }),
  });
}

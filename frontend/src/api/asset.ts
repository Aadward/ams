import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import http from './http';
import type { Asset, DashboardStats, MaintenanceRecord, PageResult } from '../types';

export function useAssetList(params?: {
  category?: string;
  status?: string;
  keyword?: string;
  page?: number;
  size?: number;
}) {
  return useQuery({
    queryKey: ['assets', params],
    queryFn: async () => {
      const { data } = await http.get<PageResult<Asset>>('/assets', { params });
      return data;
    },
  });
}

export function useAsset(id: number) {
  return useQuery({
    queryKey: ['asset', id],
    queryFn: async () => {
      const { data } = await http.get<Asset>(`/assets/${id}`);
      return data;
    },
    enabled: !!id,
  });
}

export function useDashboardStats() {
  return useQuery({
    queryKey: ['dashboard', 'stats'],
    queryFn: async () => {
      const { data } = await http.get<DashboardStats>('/dashboard/stats');
      return data;
    },
  });
}

export function useMaintenanceRecords(assetId: number) {
  return useQuery({
    queryKey: ['asset', assetId, 'maintenance-records'],
    queryFn: async () => {
      const { data } = await http.get<MaintenanceRecord[]>(`/assets/${assetId}/maintenance-records`);
      return data;
    },
    enabled: !!assetId,
  });
}

export function useAssignAsset() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, assigneeId }: { id: number; assigneeId: number }) => {
      const { data } = await http.post<Asset>(`/assets/${id}/assign`, { assigneeId });
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['assets'] }),
  });
}

export function useUnassignAsset() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const { data } = await http.post<Asset>(`/assets/${id}/unassign`);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['assets'] }),
  });
}

export function useRetireAsset() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const { data } = await http.post<Asset>(`/assets/${id}/retire`);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['assets'] }),
  });
}

export function useCreateAsset() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: Record<string, unknown>) => {
      const { data } = await http.post<Asset>('/assets', payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['assets'] }),
  });
}

export function useUpdateAsset(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: Record<string, unknown>) => {
      const { data } = await http.put<Asset>(`/assets/${id}`, payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['assets'] }),
  });
}

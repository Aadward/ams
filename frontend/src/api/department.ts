import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import http from './http';
import type { Department } from '../types';

export function useDepartmentList() {
  return useQuery({
    queryKey: ['departments'],
    queryFn: async () => {
      const { data } = await http.get<Department[]>('/departments');
      return data;
    },
  });
}

export function useDepartment(id: number) {
  return useQuery({
    queryKey: ['department', id],
    queryFn: async () => {
      const { data } = await http.get<Department>(`/departments/${id}`);
      return data;
    },
    enabled: !!id,
  });
}

export function useCreateDepartment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: Record<string, unknown>) => {
      const { data } = await http.post<Department>('/departments', payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['departments'] }),
  });
}

export function useUpdateDepartment(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: Record<string, unknown>) => {
      const { data } = await http.put<Department>(`/departments/${id}`, payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['departments'] }),
  });
}

export function useDeleteDepartment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await http.delete(`/departments/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['departments'] }),
  });
}

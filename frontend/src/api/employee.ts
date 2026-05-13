import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import http from './http';
import type { Employee, PageResult } from '../types';

export function useEmployeeList(params?: { page?: number; size?: number }) {
  return useQuery({
    queryKey: ['employees', params],
    queryFn: async () => {
      const { data } = await http.get<PageResult<Employee>>('/employees', { params });
      return data;
    },
  });
}

export function useEmployee(id: number) {
  return useQuery({
    queryKey: ['employee', id],
    queryFn: async () => {
      const { data } = await http.get<Employee>(`/employees/${id}`);
      return data;
    },
    enabled: !!id,
  });
}

export function useCreateEmployee() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: Record<string, unknown>) => {
      const { data } = await http.post<Employee>('/employees', payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['employees'] }),
  });
}

export function useUpdateEmployee(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: Record<string, unknown>) => {
      const { data } = await http.put<Employee>(`/employees/${id}`, payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['employees'] }),
  });
}

export function useDeleteEmployee() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await http.delete(`/employees/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['employees'] }),
  });
}
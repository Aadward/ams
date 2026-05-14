import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import http from './http';
import type { BorrowRecord, PageResult } from '../types';

export function useBorrowList(params?: {
  status?: string;
  type?: string;
  keyword?: string;
  page?: number;
  size?: number;
}) {
  return useQuery({
    queryKey: ['borrows', params],
    queryFn: async () => {
      const { data } = await http.get<PageResult<BorrowRecord>>('/borrows', { params });
      return data;
    },
  });
}

export function useMyBorrows(borrowerId: number) {
  return useQuery({
    queryKey: ['borrows', 'my', borrowerId],
    queryFn: async () => {
      const { data } = await http.get<BorrowRecord[]>('/borrows/my', { params: { borrowerId } });
      return data;
    },
    enabled: !!borrowerId,
  });
}

export function usePendingBorrows() {
  return useQuery({
    queryKey: ['borrows', 'pending'],
    queryFn: async () => {
      const { data } = await http.get<BorrowRecord[]>('/borrows/pending');
      return data;
    },
  });
}

export function useBorrow(id: number) {
  return useQuery({
    queryKey: ['borrow', id],
    queryFn: async () => {
      const { data } = await http.get<BorrowRecord>(`/borrows/${id}`);
      return data;
    },
    enabled: !!id,
  });
}

export function useCreateBorrow() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: {
      assetId: number;
      borrowerId: number;
      departmentId: number;
      type: string;
      expectedReturnDate?: string;
      reason?: string;
    }) => {
      const { data } = await http.post<BorrowRecord>('/borrows', payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['borrows'] }),
  });
}

export function useApproveBorrow() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, managerComment }: { id: number; managerComment?: string }) => {
      const { data } = await http.post<BorrowRecord>(`/borrows/${id}/approve`, { managerComment });
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['borrows'] }),
  });
}

export function useRejectBorrow() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, managerComment }: { id: number; managerComment?: string }) => {
      const { data } = await http.post<BorrowRecord>(`/borrows/${id}/reject`, { managerComment });
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['borrows'] }),
  });
}

export function useReturnBorrow() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const { data } = await http.post<BorrowRecord>(`/borrows/${id}/return`);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['borrows'] }),
  });
}

export function useCancelBorrow() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const { data } = await http.post<BorrowRecord>(`/borrows/${id}/cancel`);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['borrows'] }),
  });
}

import { useQuery } from '@tanstack/react-query';
import http from './http';
import type { Asset, PageResult } from '../types';

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

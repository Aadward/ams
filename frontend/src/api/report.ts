import http from './http';

export interface AssetStatisticsResponse {
  name: string;
  count: number;
  totalValue: number;
}

export interface MaintenanceCostSummary {
  monthlyCost: number;
  quarterlyCost: number;
  yearlyCost: number;
  monthlyCount: number;
  quarterlyCount: number;
  yearlyCount: number;
}

export const reportApi = {
  byCategory: () =>
    http.get<AssetStatisticsResponse[]>('/reports/assets/by-category'),
  byStatus: () =>
    http.get<AssetStatisticsResponse[]>('/reports/assets/by-status'),
  byDepartment: () =>
    http.get<AssetStatisticsResponse[]>('/reports/assets/by-department'),
  maintenanceCost: () =>
    http.get<MaintenanceCostSummary>('/reports/maintenance/cost-summary'),
};

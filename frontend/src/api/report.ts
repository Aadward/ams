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

export interface DepreciationRecord {
  assetId: number;
  assetCode: string;
  assetName: string;
  category: string;
  purchaseDate: string;
  originalValue: number;
  depreciationYears: number;
  annualDepreciation: number;
  accumulatedDepreciation: number;
  currentNetValue: number;
  yearsUsed: number;
  fullyDepreciated: boolean;
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
  depreciationLedger: () =>
    http.get<DepreciationRecord[]>('/depreciation/ledger'),
  depreciationAsset: (id: number) =>
    http.get<DepreciationRecord>(`/depreciation/${id}`),
};

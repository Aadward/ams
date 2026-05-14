import http from './http';
import { DepreciationRecord } from './report';

export type { DepreciationRecord };

export interface DepreciationSummary {
  groupKey: string;
  groupLabel: string;
  assetCount: number;
  totalOriginalValue: number;
  totalAccumulatedDepreciation: number;
  totalNetValue: number;
  depreciationRate: number;
}

export const depreciationApi = {
  getLedger: () => http.get<DepreciationRecord[]>('/api/depreciation/ledger'),
  getAsset: (id: number) => http.get<DepreciationRecord>(`/api/depreciation/${id}`),
  getSummaryByCategory: () => http.get<DepreciationSummary[]>('/api/depreciation/summary/by-category'),
  getSummaryByDepartment: () => http.get<DepreciationSummary[]>('/api/depreciation/summary/by-department'),
};
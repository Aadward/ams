import http from './http';
import { DepreciationRecord } from './report';

export type { DepreciationRecord };

export const depreciationApi = {
  getLedger: () => http.get<DepreciationRecord[]>('/api/depreciation/ledger'),
  getAsset: (id: number) => http.get<DepreciationRecord>(`/api/depreciation/${id}`),
};
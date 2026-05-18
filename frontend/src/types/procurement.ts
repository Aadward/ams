// Procurement types
export interface ProcurementRecord {
  id: number;
  assetId?: number;
  assetName?: string;
  assetCode?: string;
  requesterId: number;
  requesterName: string;
  departmentId: number;
  departmentName: string;
  type: 'PURCHASE' | 'CONSUMABLE';
  name: string;
  description?: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'PURCHASED' | 'CANCELLED';
  reason?: string;
  managerComment?: string;
  approvalId?: number;
  createdAt: string;
  updatedAt?: string;
}

export interface ProcurementRequest {
  id: number;
  requesterId: number;
  requesterName: string;
  assetId?: number;
  assetName?: string;
  assetCode?: string;
  departmentId: number;
  departmentName: string;
  type: string;
  name: string;
  description?: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  status: string;
  reason?: string;
  managerComment?: string;
  createdAt: string;
}

export const procurementStatusLabels: Record<string, string> = {
  PENDING: '待审批',
  APPROVED: '已批准',
  REJECTED: '已拒绝',
  PURCHASED: '已采购',
  CANCELLED: '已取消',
};

export const procurementStatusColors: Record<string, string> = {
  PENDING: 'processing',
  APPROVED: 'success',
  REJECTED: 'error',
  PURCHASED: 'success',
  CANCELLED: 'default',
};

export const procurementTypeLabels: Record<string, string> = {
  PURCHASE: '资产采购',
  CONSUMABLE: '耗材采购',
};

// Transfer types
export interface TransferRecord {
  id: number;
  assetId: number;
  assetCode: string;
  assetName: string;
  requesterId?: number;
  requesterName?: string;
  fromDepartmentId: number;
  fromDepartmentName: string;
  toDepartmentId: number;
  toDepartmentName: string;
  transferType: 'DEPARTMENT' | 'PERSON';
  fromPersonId?: number;
  fromPersonName?: string;
  toPersonId?: number;
  toPersonName?: string;
  reason?: string;
  approvalId?: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED' | 'CANCELLED';
  managerComment?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface TransferRequest {
  id: number;
  requesterId: number;
  requesterName: string;
  assetId: number;
  assetName: string;
  assetCode: string;
  fromDepartmentId: number;
  fromDepartmentName: string;
  toDepartmentId: number;
  toDepartmentName: string;
  transferType: 'DEPARTMENT' | 'PERSON';
  fromPersonId?: number;
  fromPersonName?: string;
  toPersonId?: number;
  toPersonName?: string;
  status: string;
  reason?: string;
  managerComment?: string;
  createdAt: string;
}

export const transferStatusLabels: Record<string, string> = {
  PENDING: '待审批',
  APPROVED: '已批准',
  REJECTED: '已拒绝',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};

export const transferStatusColors: Record<string, string> = {
  PENDING: 'processing',
  APPROVED: 'success',
  REJECTED: 'error',
  COMPLETED: 'default',
  CANCELLED: 'default',
};

export const transferTypeLabels: Record<string, string> = {
  DEPARTMENT: '部门调拨',
  PERSON: '人员调拨',
};
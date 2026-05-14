// Asset types
export interface Asset {
  id: number;
  assetCode: string;
  name: string;
  category: 'HARDWARE' | 'NETWORK' | 'PERIPHERAL' | 'SOFTWARE_LICENSE';
  status: 'IN_STOCK' | 'IN_USE' | 'MAINTENANCE' | 'RETIRED';
  spec?: string;
  purchaseDate?: string;
  purchasePrice?: number;
  warrantyEnd?: string;
  supplier?: string;
  location?: string;
  assigneeId?: number;
  assigneeName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Employee {
  id: number;
  name: string;
  deptId?: number;
  deptName?: string;
  email?: string;
  phone?: string;
  role?: 'ADMIN' | 'MANAGER' | 'USER';
  createdAt: string;
  updatedAt: string;
}

export interface Department {
  id: number;
  name: string;
  parentId?: number;
  parentName?: string;
  description?: string;
  children?: Department[];
  createdAt: string;
  updatedAt: string;
}

export interface MaintenanceRecord {
  id: number;
  assetId: number;
  approvalId?: number;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  type: 'REPAIR' | 'MAINTENANCE' | 'INSPECTION';
  description?: string;
  cost?: number;
  startDate: string;
  endDate?: string;
  vendor?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DashboardStats {
  totalAssets: number;
  inStock: number;
  inUse: number;
  inMaintenance: number;
  retired: number;
  categoryStats: Record<string, number>;
  monthlyTrend?: { month: string; count: number }[];
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// Consumable types
export type ConsumableCategory = 'OFFICE_SUPPLIES' | 'ELECTRONIC_PARTS' | 'PRODUCTION_CONSUMABLES';
export type ConsumableRecordType = 'IN' | 'OUT';

export const consumableCategoryLabels: Record<string, string> = {
  OFFICE_SUPPLIES: '办公用品',
  ELECTRONIC_PARTS: '电子配件',
  PRODUCTION_CONSUMABLES: '生产耗材',
};

export const consumableRecordTypeLabels: Record<string, string> = {
  IN: '入库',
  OUT: '出库',
};

export const consumableCategoryColors: Record<string, string> = {
  OFFICE_SUPPLIES: 'blue',
  ELECTRONIC_PARTS: 'green',
  PRODUCTION_CONSUMABLES: 'orange',
};

// Warranty notification types
export interface ExpiringWarranty {
  id: number;
  assetCode: string;
  name: string;
  category: string;
  warrantyEnd: string;
}

// Borrow types
export interface BorrowRecord {
  id: number;
  assetId: number;
  assetCode: string;
  assetName: string;
  borrowerId: number;
  borrowerName: string;
  departmentId: number;
  departmentName: string;
  approvalId?: number;
  status: 'BORROWED' | 'RETURNED' | 'OVERDUE';
  expectedReturnDate?: string;
  actualReturnDate?: string;
  reason?: string;
  managerComment?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface BorrowRequest {
  id: number;
  requesterId: number;
  requesterName: string;
  assetId: number;
  assetName: string;
  assetCode: string;
  departmentId: number;
  departmentName: string;
  type: string;
  status: string;
  reason?: string;
  managerComment?: string;
  createdAt: string;
}

export const borrowStatusLabels: Record<string, string> = {
  BORROWED: '已借出',
  RETURNED: '已归还',
  OVERDUE: '已超期',
};

export const borrowStatusColors: Record<string, string> = {
  BORROWED: 'warning',
  RETURNED: 'green',
  OVERDUE: 'error',
};

export const borrowTypeLabels: Record<string, string> = {
  BORROW: '借出',
  RETURN: '归还',
};

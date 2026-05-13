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
  dept?: string;
  email?: string;
  phone?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MaintenanceRecord {
  id: number;
  assetId: number;
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
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

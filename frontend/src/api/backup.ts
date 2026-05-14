import http from './http';

export interface BackupFile {
  name: string;
  path: string;
  size: number;
  sizeFormatted: string;
  lastModified: string;
}

export interface BackupListResponse {
  success: boolean;
  backups: BackupFile[];
  count: number;
  directory: string;
}

export interface BackupCreateResponse {
  success: boolean;
  message: string;
  file: string;
  path: string;
  size: number;
  timestamp: string;
}

export const backupApi = {
  list: () => http.get<BackupListResponse>('/backup/list'),
  create: () => http.post<BackupCreateResponse>('/backup/create'),
};

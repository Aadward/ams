const API_BASE = '/api/inventory-plans';

const authHeaders = (): HeadersInit => {
  const token = localStorage.getItem('token');
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const employeeId = localStorage.getItem('currentEmployeeId');
  if (employeeId) {
    headers['X-Employee-Id'] = employeeId;
  }
  return headers;
};

export const inventoryPlanApi = {
  list: async () => {
    const res = await fetch(API_BASE, { headers: authHeaders() });
    return res.json();
  },
  getById: async (id: number) => {
    const res = await fetch(`${API_BASE}/${id}`, { headers: authHeaders() });
    return res.json();
  },
  create: async (data: any) => {
    const res = await fetch(API_BASE, { method: 'POST', headers: authHeaders(), body: JSON.stringify(data) });
    return res.json();
  },
  delete: async (id: number) => {
    const res = await fetch(`${API_BASE}/${id}`, { method: 'DELETE', headers: authHeaders() });
    return res.ok;
  },
  start: async (id: number) => {
    const res = await fetch(`${API_BASE}/${id}/start`, { method: 'POST', headers: authHeaders() });
    return res.json();
  },
  complete: async (id: number) => {
    const res = await fetch(`${API_BASE}/${id}/complete`, { method: 'POST', headers: authHeaders() });
    return res.json();
  },
};

export const inventoryTaskApi = {
  list: async (params?: { planId?: number; assigneeId?: number; status?: string }) => {
    const searchParams = new URLSearchParams();
    if (params?.planId) searchParams.set('planId', String(params.planId));
    if (params?.assigneeId) searchParams.set('assigneeId', String(params.assigneeId));
    if (params?.status) searchParams.set('status', params.status);
    const query = searchParams.toString();
    const res = await fetch(`${'/api/inventory-tasks'}${query ? '?' + query : ''}`, { headers: authHeaders() });
    return res.json();
  },
  myTasks: async () => {
    const res = await fetch('/api/inventory-tasks/my', { headers: authHeaders() });
    return res.json();
  },
  check: async (id: number, remark?: string) => {
    const res = await fetch(`/api/inventory-tasks/${id}/check`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify({ remark }),
    });
    return res.json();
  },
  uncheck: async (id: number) => {
    const res = await fetch(`/api/inventory-tasks/${id}/uncheck`, { method: 'PUT', headers: authHeaders() });
    return res.json();
  },
};

export const inventoryRecordApi = {
  list: async (planId: number, result?: string) => {
    const url = result ? `/api/inventory-records?planId=${planId}&result=${result}` : `/api/inventory-records?planId=${planId}`;
    const res = await fetch(url, { headers: authHeaders() });
    return res.json();
  },
  report: async (planId: number) => {
    const res = await fetch(`/api/inventory-records/report/${planId}`, { headers: authHeaders() });
    return res.json();
  },
  export: async (planId: number) => {
    const res = await fetch(`/api/inventory-records/report/${planId}/export`, {
      headers: authHeaders(),
    });
    if (!res.ok) throw new Error('Export failed');
    return res.blob();
  },
};

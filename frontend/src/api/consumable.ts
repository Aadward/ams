const API_BASE = '/api/consumables';

const authHeaders = (): HeadersInit => {
  const token = localStorage.getItem('token');
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
};

export const consumableApi = {
  list: async (category?: string) => {
    const url = category ? `${API_BASE}?category=${category}` : API_BASE;
    const res = await fetch(url, { headers: authHeaders() });
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
  update: async (id: number, data: any) => {
    const res = await fetch(`${API_BASE}/${id}`, { method: 'PUT', headers: authHeaders(), body: JSON.stringify(data) });
    return res.json();
  },
  delete: async (id: number) => {
    const res = await fetch(`${API_BASE}/${id}`, { method: 'DELETE', headers: authHeaders() });
    return res.ok;
  },
  stockIn: async (data: any) => {
    const res = await fetch(`${API_BASE}/stock-in`, { method: 'POST', headers: authHeaders(), body: JSON.stringify(data) });
    return res.json();
  },
  stockOut: async (data: any) => {
    const res = await fetch(`${API_BASE}/stock-out`, { method: 'POST', headers: authHeaders(), body: JSON.stringify(data) });
    return res.json();
  },
  listRecords: async (params?: { consumableId?: number; type?: string; start?: string; end?: string }) => {
    const searchParams = new URLSearchParams();
    if (params?.consumableId) searchParams.set('consumableId', String(params.consumableId));
    if (params?.type) searchParams.set('type', params.type);
    if (params?.start) searchParams.set('start', params.start);
    if (params?.end) searchParams.set('end', params.end);
    const query = searchParams.toString();
    const res = await fetch(`${API_BASE}/records${query ? '?' + query : ''}`, { headers: authHeaders() });
    return res.json();
  },
  getConsumptionReport: async (start: string, end: string) => {
    const res = await fetch(`${API_BASE}/report/consumption?start=${start}&end=${end}`, { headers: authHeaders() });
    return res.json();
  },
};
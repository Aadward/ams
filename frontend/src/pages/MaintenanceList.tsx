import { useState } from 'react';
import { Table, Button, Space, Tag, message, Modal, Select, DatePicker, Popconfirm } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { maintenanceApi } from '../api/maintenance';
import type { MaintenanceRecord } from '../types';
import type { Dayjs } from 'dayjs';

const { RangePicker } = DatePicker;

const statusMap: Record<string, { color: string; label: string }> = {
  PENDING: { color: 'orange', label: '待处理' },
  IN_PROGRESS: { color: 'blue', label: '进行中' },
  COMPLETED: { color: 'green', label: '已完成' },
  CANCELLED: { color: 'red', label: '已取消' },
};

const typeMap: Record<string, string> = {
  REPAIR: '维修',
  INSPECTION: '巡检',
  CALIBRATION: '校准',
  CLEANING: '清洁',
  OTHER: '其他',
};

export default function MaintenanceList() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [filters, setFilters] = useState<{
    assetId?: number;
    status?: string;
    type?: string;
    dateFrom?: string;
    dateTo?: string;
  }>({});

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['maintenance-records', page, pageSize, filters],
    queryFn: async () => {
      const params: Record<string, unknown> = {
        page: page - 1,
        size: pageSize,
        ...filters,
      };
      if (filters.dateFrom) params.dateFrom = filters.dateFrom;
      if (filters.dateTo) params.dateTo = filters.dateTo;
      const { data: result } = await maintenanceApi.list(params);
      return result;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => maintenanceApi.delete(id),
    onSuccess: () => {
      message.success('删除成功');
      queryClient.invalidateQueries({ queryKey: ['maintenance-records'] });
    },
    onError: () => {
      message.error('删除失败');
    },
  });

  const handleFilterChange = (key: string, value: unknown) => {
    setFilters(prev => ({ ...prev, [key]: value }));
    setPage(1);
  };

  const handleDateChange = (dates: [Dayjs | null, Dayjs | null] | null) => {
    if (dates && dates[0] && dates[1]) {
      setFilters(prev => ({
        ...prev,
        dateFrom: dates[0].format('YYYY-MM-DD'),
        dateTo: dates[1].format('YYYY-MM-DD'),
      }));
    } else {
      setFilters(prev => ({ ...prev, dateFrom: undefined, dateTo: undefined }));
    }
    setPage(1);
  };

  const handleComplete = async (record: MaintenanceRecord) => {
    try {
      await maintenanceApi.update(record.id, { endDate: new Date().toISOString().slice(0, 10) });
      message.success('维修已完成');
      refetch();
    } catch {
      message.error('操作失败');
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 60,
    },
    {
      title: '资产ID',
      dataIndex: 'assetId',
      width: 80,
    },
    {
      title: '审批ID',
      dataIndex: 'approvalId',
      width: 80,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status: string) => (
        <Tag color={statusMap[status]?.color}>{statusMap[status]?.label || status}</Tag>
      ),
    },
    {
      title: '类型',
      dataIndex: 'type',
      width: 100,
      render: (type: string) => typeMap[type] || type,
    },
    {
      title: '描述',
      dataIndex: 'description',
      ellipsis: true,
    },
    {
      title: '费用',
      dataIndex: 'cost',
      width: 100,
      render: (cost: number) => cost != null ? `¥${cost}` : '-',
    },
    {
      title: '开始日期',
      dataIndex: 'startDate',
      width: 120,
    },
    {
      title: '结束日期',
      dataIndex: 'endDate',
      width: 120,
      render: (endDate: string) => endDate || '-',
    },
    {
      title: '维修商',
      dataIndex: 'vendor',
      width: 120,
      render: (vendor: string) => vendor || '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_: unknown, record: MaintenanceRecord) => (
        <Space size="small">
          <Button size="small" onClick={() => navigate(`/assets/${record.assetId}`)}>
            资产
          </Button>
          {record.status !== 'COMPLETED' && record.status !== 'CANCELLED' && (
            <Button size="small" type="primary" onClick={() => handleComplete(record)}>
              完成
            </Button>
          )}
          <Popconfirm
            title="确认删除"
            onConfirm={() => deleteMutation.mutate(record.id)}
            okText="确认"
            cancelText="取消"
          >
            <Button size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h2>维修记录管理</h2>

      <Space wrap style={{ marginBottom: 16 }}>
        <Select
          placeholder="状态"
          allowClear
          style={{ width: 120 }}
          value={filters.status}
          onChange={(val) => handleFilterChange('status', val)}
          options={Object.entries(statusMap).map(([k, v]) => ({ label: v.label, value: k }))}
        />
        <Select
          placeholder="类型"
          allowClear
          style={{ width: 120 }}
          value={filters.type}
          onChange={(val) => handleFilterChange('type', val)}
          options={Object.entries(typeMap).map(([k, v]) => ({ label: v, value: k }))}
        />
        <RangePicker onChange={handleDateChange} />
        <Button onClick={() => { setFilters({}); setPage(1); }}>重置</Button>
      </Space>

      <Table
        dataSource={data?.content || []}
        columns={columns}
        rowKey="id"
        loading={isLoading}
        pagination={{
          current: page,
          pageSize,
          total: data?.totalElements || 0,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`,
          onChange: (p, ps) => {
            setPage(p);
            setPageSize(ps);
          },
        }}
        scroll={{ x: 1200 }}
        size="small"
      />
    </div>
  );
}

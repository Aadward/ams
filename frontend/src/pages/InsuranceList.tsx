import { useState } from 'react';
import { Table, Button, Space, Tag, message, Select, DatePicker, Popconfirm } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import { insuranceApi } from '../api/insurance';
import type { InsuranceRecord } from '../api/insurance';

const { RangePicker } = DatePicker;

const statusMap: Record<string, { color: string; label: string }> = {
  ACTIVE: { color: 'green', label: '有效' },
  EXPIRED: { color: 'red', label: '已过期' },
  CANCELLED: { color: 'orange', label: '已取消' },
};

const insuranceTypeMap: Record<string, { color: string; label: string }> = {
  PROPERTY: { color: 'blue', label: '财产险' },
  COMPREHENSIVE: { color: 'purple', label: '综合险' },
  THEFT: { color: 'orange', label: '盗抢险' },
};

export default function InsuranceList() {
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

  const { data, isLoading } = useQuery({
    queryKey: ['insurance-policies', page, pageSize, filters],
    queryFn: async () => {
      const params: Record<string, unknown> = {
        page: page - 1,
        size: pageSize,
        ...filters,
      };
      if (filters.dateFrom) params.dateFrom = filters.dateFrom;
      if (filters.dateTo) params.dateTo = filters.dateTo;
      const { data: result } = await insuranceApi.list(params);
      return result;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => insuranceApi.delete(id),
    onSuccess: () => {
      message.success('删除成功');
      queryClient.invalidateQueries({ queryKey: ['insurance-policies'] });
    },
    onError: () => {
      message.error('删除失败');
    },
  });

  const handleFilterChange = (key: string, value: unknown) => {
    setFilters(prev => ({ ...prev, [key]: value }));
    setPage(1);
  };

  const handleDateChange = (dates: [dayjs.Dayjs | null, dayjs.Dayjs | null] | null) => {
    if (dates && dates[0] !== null && dates[1] !== null) {
      const [from, to] = dates as [dayjs.Dayjs, dayjs.Dayjs];
      setFilters(prev => ({
        ...prev,
        dateFrom: from.format('YYYY-MM-DD'),
        dateTo: to.format('YYYY-MM-DD'),
      }));
    } else {
      setFilters(prev => ({ ...prev, dateFrom: undefined, dateTo: undefined }));
    }
    setPage(1);
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
      title: '资产编码',
      dataIndex: 'assetCode',
      width: 120,
    },
    {
      title: '资产名称',
      dataIndex: 'assetName',
      width: 150,
      ellipsis: true,
    },
    {
      title: '保单号',
      dataIndex: 'policyNumber',
      width: 160,
    },
    {
      title: '保险公司',
      dataIndex: 'insuranceCompany',
      width: 140,
      ellipsis: true,
    },
    {
      title: '保险类型',
      dataIndex: 'type',
      width: 100,
      render: (type: string) => (
        <Tag color={insuranceTypeMap[type]?.color}>{insuranceTypeMap[type]?.label || type}</Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      render: (status: string) => (
        <Tag color={statusMap[status]?.color}>{statusMap[status]?.label || status}</Tag>
      ),
    },
    {
      title: '保险金额',
      dataIndex: 'coverageAmount',
      width: 110,
      render: (val: number) => val != null ? `¥${val.toLocaleString()}` : '-',
    },
    {
      title: '保费',
      dataIndex: 'premium',
      width: 100,
      render: (val: number) => val != null ? `¥${val.toLocaleString()}` : '-',
    },
    {
      title: '到期日期',
      dataIndex: 'endDate',
      width: 110,
      render: (date: string) => {
        if (!date) return '-';
        const isExpiring = dayjs(date).isBefore(dayjs().add(30, 'day'));
        return (
          <span style={{ color: isExpiring ? '#ff4d4f' : 'inherit' }}>
            {date}
            {isExpiring && ' ⚠️'}
          </span>
        );
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: unknown, record: InsuranceRecord) => (
        <Space size="small">
          <Button size="small" onClick={() => navigate(`/insurance/${record.id}`)}>
            详情
          </Button>
          <Button size="small" onClick={() => navigate(`/insurance/${record.id}/edit`)}>
            编辑
          </Button>
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
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h2>保险管理</h2>
        <Button type="primary" onClick={() => navigate('/insurance/form')}>
          新增保险
        </Button>
      </div>

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
          placeholder="保险类型"
          allowClear
          style={{ width: 120 }}
          value={filters.type}
          onChange={(val) => handleFilterChange('type', val)}
          options={Object.entries(insuranceTypeMap).map(([k, v]) => ({ label: v.label, value: k }))}
        />
        <RangePicker onChange={handleDateChange} />
        <Button
          onClick={() => {
            setFilters({});
            setPage(1);
          }}
        >
          重置
        </Button>
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
        scroll={{ x: 1500 }}
        size="small"
      />
    </div>
  );
}

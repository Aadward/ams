import { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, Modal, Input, message, Card, Row, Col, Select, Tabs, Badge, Statistic } from 'antd';
import { CheckOutlined, CloseOutlined, UndoOutlined } from '@ant-design/icons';
import { useBorrowList, useReturnBorrow } from '../api/borrow';
import type { BorrowRecord, BorrowRequest } from '../types';
import { borrowStatusLabels } from '../types';
import http from '../api/http';

const { TextArea } = Input;
const { Option } = Select;

export default function BorrowList() {
  const [statusFilter, setStatusFilter] = useState<string>();
  const [pendingRequests, setPendingRequests] = useState<BorrowRequest[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [actionType, setActionType] = useState<'approve' | 'reject'>('approve');
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [comment, setComment] = useState('');
  const [returnModalVisible, setReturnModalVisible] = useState(false);
  const [returnId, setReturnId] = useState<number | null>(null);
  const [pendingLoading, setPendingLoading] = useState(false);

  const { data, isLoading, refetch } = useBorrowList(statusFilter ? { status: statusFilter } : {});
  const returnMutation = useReturnBorrow();

  // Fetch pending borrow requests (ASSET_BORROW approval requests)
  const fetchPending = async () => {
    setPendingLoading(true);
    try {
      const { data: res } = await http.get<BorrowRequest[]>('/borrows/pending');
      setPendingRequests(res || []);
    } catch {
      message.error('获取待审批列表失败');
    } finally {
      setPendingLoading(false);
    }
  };

  useEffect(() => {
    fetchPending();
  }, []);

  const statusBadge = (status: string) => {
    const colorMap: Record<string, string> = {
      BORROWED: 'orange',
      RETURNED: 'green',
      OVERDUE: 'red',
    };
    return <Tag color={colorMap[status] || 'default'}>{borrowStatusLabels[status] || status}</Tag>;
  };

  const approvalStatusBadge = (status: string) => {
    const colorMap: Record<string, string> = {
      PENDING: 'blue',
      APPROVED: 'green',
      REJECTED: 'red',
    };
    const labels: Record<string, string> = {
      PENDING: '待审批',
      APPROVED: '已批准',
      REJECTED: '已拒绝',
    };
    return <Tag color={colorMap[status] || 'default'}>{labels[status] || status}</Tag>;
  };

  const openActionModal = (id: number, action: 'approve' | 'reject') => {
    setSelectedId(id);
    setActionType(action);
    setComment('');
    setModalVisible(true);
  };

  const handleAction = async () => {
    if (!selectedId) return;
    try {
      const endpoint = actionType === 'approve' ? `/approvals/${selectedId}/approve` : `/approvals/${selectedId}/reject`;
      await http.post(endpoint, { managerComment: comment });
      message.success(actionType === 'approve' ? '审批通过' : '已拒绝');
      setModalVisible(false);
      refetch();
      fetchPending();
    } catch {
      message.error('操作失败');
    }
  };

  const handleReturn = async () => {
    if (!returnId) return;
    try {
      await returnMutation.mutateAsync(returnId);
      message.success('归还成功');
      setReturnModalVisible(false);
      refetch();
    } catch {
      message.error('归还失败');
    }
  };

  const openReturnModal = (id: number) => {
    setReturnId(id);
    setReturnModalVisible(true);
  };

  // Columns for BorrowRecord list (after approval - BORROWED/RETURNED/OVERDUE)
  const recordColumns = [
    { title: '资产编码', dataIndex: 'assetCode', width: 120 },
    { title: '资产名称', dataIndex: 'assetName', width: 150 },
    { title: '状态', dataIndex: 'status', width: 100, render: statusBadge },
    { title: '借用人', dataIndex: 'borrowerName', width: 100 },
    { title: '部门', dataIndex: 'departmentName', width: 120 },
    { title: '预计归还', dataIndex: 'expectedReturnDate', width: 120 },
    { title: '实际归还', dataIndex: 'actualReturnDate', width: 120 },
    { title: '申请原因', dataIndex: 'reason', ellipsis: true },
    { title: '时间', dataIndex: 'createdAt', width: 170 },
    {
      title: '操作',
      width: 120,
      render: (_: unknown, record: BorrowRecord) => (
        <Space>
          {record.status === 'BORROWED' && (
            <Button size="small" type="primary" icon={<UndoOutlined />} onClick={() => openReturnModal(record.id)}>
              确认归还
            </Button>
          )}
        </Space>
      ),
    },
  ];

  // Columns for pending approval requests
  const pendingColumns = [
    { title: '资产编码', dataIndex: 'assetCode', width: 120 },
    { title: '资产名称', dataIndex: 'assetName', width: 150 },
    { title: '申请人', dataIndex: 'requesterName', width: 100 },
    { title: '部门', dataIndex: 'departmentName', width: 120 },
    { title: '预计归还', dataIndex: 'expectedReturnDate', width: 120 },
    { title: '状态', dataIndex: 'status', width: 80, render: approvalStatusBadge },
    { title: '申请原因', dataIndex: 'reason', ellipsis: true },
    { title: '时间', dataIndex: 'createdAt', width: 170 },
    {
      title: '操作',
      width: 160,
      render: (_: unknown, record: BorrowRequest) => (
        <Space>
          <Button size="small" type="primary" icon={<CheckOutlined />} onClick={() => openActionModal(record.id, 'approve')}>
            通过
          </Button>
          <Button size="small" danger icon={<CloseOutlined />} onClick={() => openActionModal(record.id, 'reject')}>
            拒绝
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Row gutter={16}>
        <Col span={6}>
          <Card>
            <Statistic title="待审批" value={pendingRequests.length} valueStyle={{ color: '#cf1322' }} />
          </Card>
        </Col>
      </Row>

      <Tabs
        items={[
          {
            key: 'all',
            label: <span>全部记录 <Badge count={data?.totalElements || 0} style={{ backgroundColor: '#1677ff' }} /></span>,
            children: (
              <>
                <Space style={{ marginBottom: 16 }}>
                  <Select
                    placeholder="状态"
                    allowClear
                    style={{ width: 140 }}
                    value={statusFilter}
                    onChange={setStatusFilter}
                  >
                    <Option value="BORROWED">已借出</Option>
                    <Option value="RETURNED">已归还</Option>
                    <Option value="OVERDUE">已超期</Option>
                  </Select>
                  <Input.Search placeholder="搜索资产/借用人" style={{ width: 200 }} />
                </Space>
                <Table
                  loading={isLoading}
                  dataSource={data?.content}
                  rowKey="id"
                  columns={recordColumns}
                  pagination={{ total: data?.totalElements, defaultPageSize: 10 }}
                />
              </>
            ),
          },
          {
            key: 'pending',
            label: <span>待我审批 <Badge count={pendingRequests.length} style={{ backgroundColor: '#faad14' }} /></span>,
            children: (
              <Table
                loading={pendingLoading}
                dataSource={pendingRequests}
                rowKey="id"
                columns={pendingColumns}
                pagination={false}
              />
            ),
          },
        ]}
      />

      <Modal
        title={actionType === 'approve' ? '审批通过' : '拒绝申请'}
        open={modalVisible}
        onOk={handleAction}
        onCancel={() => setModalVisible(false)}
        okText={actionType === 'approve' ? '确认通过' : '确认拒绝'}
        okButtonProps={{ danger: actionType === 'reject' }}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <p>{actionType === 'approve' ? '确认通过此借用申请？' : '请输入拒绝原因：'}</p>
          <TextArea rows={3} value={comment} onChange={(e) => setComment(e.target.value)} placeholder="可选备注" />
        </Space>
      </Modal>

      <Modal
        title="确认归还"
        open={returnModalVisible}
        onOk={handleReturn}
        onCancel={() => setReturnModalVisible(false)}
        okText="确认归还"
        okButtonProps={{ loading: returnMutation.isPending }}
      >
        <p>确认该资产已归还？</p>
      </Modal>
    </Space>
  );
}

import { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, Modal, Input, message, Card, Row, Col, Select, Tabs, Badge, Statistic } from 'antd';
import { CheckOutlined, CloseOutlined, StopOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useTransferList, useApproveTransfer, useRejectTransfer, useCancelTransfer } from '../api/transfer';
import type { TransferRecord, TransferRequest } from '../types/transfer';
import { transferStatusLabels, transferStatusColors, transferTypeLabels } from '../types/transfer';
import http from '../api/http';

const { TextArea } = Input;
const { Option } = Select;

export default function TransferList() {
  const navigate = useNavigate();
  const [statusFilter, setStatusFilter] = useState<string>();
  const [typeFilter, setTypeFilter] = useState<string>();
  const [pendingRequests, setPendingRequests] = useState<TransferRequest[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [actionType, setActionType] = useState<'approve' | 'reject'>('approve');
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [comment, setComment] = useState('');
  const [cancelModalVisible, setCancelModalVisible] = useState(false);
  const [cancelId, setCancelId] = useState<number | null>(null);
  const [pendingLoading, setPendingLoading] = useState(false);

  const { data, isLoading, refetch } = useTransferList({
    ...(statusFilter && { status: statusFilter }),
    ...(typeFilter && { transferType: typeFilter }),
  });
  const approveMutation = useApproveTransfer();
  const rejectMutation = useRejectTransfer();
  const cancelMutation = useCancelTransfer();

  const fetchPending = async () => {
    setPendingLoading(true);
    try {
      const { data: res } = await http.get<TransferRequest[]>('/transfers/pending');
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
    const color = transferStatusColors[status] || 'default';
    return <Tag color={color}>{transferStatusLabels[status] || status}</Tag>;
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
      if (actionType === 'approve') {
        await approveMutation.mutateAsync({ id: selectedId, managerComment: comment });
        message.success('审批通过');
      } else {
        await rejectMutation.mutateAsync({ id: selectedId, managerComment: comment });
        message.success('已拒绝');
      }
      setModalVisible(false);
      refetch();
      fetchPending();
    } catch {
      message.error('操作失败');
    }
  };

  const openCancelModal = (id: number) => {
    setCancelId(id);
    setCancelModalVisible(true);
  };

  const handleCancel = async () => {
    if (!cancelId) return;
    try {
      await cancelMutation.mutateAsync(cancelId);
      message.success('已取消');
      setCancelModalVisible(false);
      refetch();
    } catch {
      message.error('取消失败');
    }
  };

  const recordColumns = [
    { title: '资产编码', dataIndex: 'assetCode', width: 120 },
    { title: '资产名称', dataIndex: 'assetName', width: 150 },
    { title: '调拨类型', dataIndex: 'transferType', width: 100, render: (v: string) => transferTypeLabels[v] || v },
    { title: '调出部门', dataIndex: 'fromDepartmentName', width: 120 },
    { title: '调入部门', dataIndex: 'toDepartmentName', width: 120 },
    { title: '状态', dataIndex: 'status', width: 90, render: statusBadge },
    { title: '申请原因', dataIndex: 'reason', ellipsis: true },
    { title: '时间', dataIndex: 'createdAt', width: 170 },
    {
      title: '操作',
      width: 150,
      render: (_: unknown, record: TransferRecord) => (
        <Space>
          <Button size="small" type="link" onClick={() => navigate(`/transfers/${record.id}`)}>详情</Button>
          {record.status === 'PENDING' && (
            <Button size="small" danger icon={<StopOutlined />} onClick={() => openCancelModal(record.id)}>
              取消
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const pendingColumns = [
    { title: '资产编码', dataIndex: 'assetCode', width: 120 },
    { title: '资产名称', dataIndex: 'assetName', width: 150 },
    { title: '调拨类型', dataIndex: 'transferType', width: 100, render: (v: string) => transferTypeLabels[v] || v },
    { title: '调出部门', dataIndex: 'fromDepartmentName', width: 120 },
    { title: '调入部门', dataIndex: 'toDepartmentName', width: 120 },
    { title: '申请人', dataIndex: 'requesterName', width: 80 },
    { title: '状态', dataIndex: 'status', width: 80, render: statusBadge },
    { title: '申请原因', dataIndex: 'reason', ellipsis: true },
    { title: '时间', dataIndex: 'createdAt', width: 170 },
    {
      title: '操作',
      width: 160,
      render: (_: unknown, record: TransferRequest) => (
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
                    <Option value="PENDING">待审批</Option>
                    <Option value="APPROVED">已批准</Option>
                    <Option value="REJECTED">已拒绝</Option>
                    <Option value="COMPLETED">已完成</Option>
                    <Option value="CANCELLED">已取消</Option>
                  </Select>
                  <Select
                    placeholder="调拨类型"
                    allowClear
                    style={{ width: 140 }}
                    value={typeFilter}
                    onChange={setTypeFilter}
                  >
                    <Option value="DEPARTMENT">部门调拨</Option>
                    <Option value="PERSON">人员调拨</Option>
                  </Select>
                  <Input.Search placeholder="搜索资产/部门" style={{ width: 200 }} />
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
        okButtonProps={{ danger: actionType === 'reject', loading: actionType === 'approve' ? approveMutation.isPending : rejectMutation.isPending }}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <p>{actionType === 'approve' ? '确认通过此调拨申请？' : '请输入拒绝原因：'}</p>
          <TextArea rows={3} value={comment} onChange={(e) => setComment(e.target.value)} placeholder="可选备注" />
        </Space>
      </Modal>

      <Modal
        title="取消调拨"
        open={cancelModalVisible}
        onOk={handleCancel}
        onCancel={() => setCancelModalVisible(false)}
        okText="确认取消"
        okButtonProps={{ danger: true, loading: cancelMutation.isPending }}
      >
        <p>确认取消此调拨申请？</p>
      </Modal>
    </Space>
  );
}
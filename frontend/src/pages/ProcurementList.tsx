import { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, Modal, Input, message, Card, Row, Col, Select, Tabs, Badge, Statistic } from 'antd';
import { CheckOutlined, CloseOutlined, StopOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useProcurementList, useApproveProcurement, useRejectProcurement, useCancelProcurement } from '../api/procurement';
import type { ProcurementRecord, ProcurementRequest } from '../types/procurement';
import { procurementStatusLabels, procurementStatusColors, procurementTypeLabels } from '../types/procurement';
import http from '../api/http';

const { TextArea } = Input;
const { Option } = Select;

export default function ProcurementList() {
  const navigate = useNavigate();
  const [statusFilter, setStatusFilter] = useState<string>();
  const [typeFilter, setTypeFilter] = useState<string>();
  const [pendingRequests, setPendingRequests] = useState<ProcurementRequest[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [actionType, setActionType] = useState<'approve' | 'reject'>('approve');
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [comment, setComment] = useState('');
  const [cancelModalVisible, setCancelModalVisible] = useState(false);
  const [cancelId, setCancelId] = useState<number | null>(null);
  const [pendingLoading, setPendingLoading] = useState(false);

  const { data, isLoading, refetch } = useProcurementList({
    ...(statusFilter && { status: statusFilter }),
    ...(typeFilter && { type: typeFilter }),
  });
  const approveMutation = useApproveProcurement();
  const rejectMutation = useRejectProcurement();
  const cancelMutation = useCancelProcurement();

  const fetchPending = async () => {
    setPendingLoading(true);
    try {
      const { data: res } = await http.get<ProcurementRequest[]>('/procurements/pending');
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
    const color = procurementStatusColors[status] || 'default';
    return <Tag color={color}>{procurementStatusLabels[status] || status}</Tag>;
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
    { title: '采购类型', dataIndex: 'type', width: 100, render: (v: string) => procurementTypeLabels[v] || v },
    { title: '名称', dataIndex: 'name', width: 150 },
    { title: '数量', dataIndex: 'quantity', width: 80 },
    { title: '单价', dataIndex: 'unitPrice', width: 100 },
    { title: '总价', dataIndex: 'totalPrice', width: 120 },
    { title: '状态', dataIndex: 'status', width: 90, render: statusBadge },
    { title: '申请原因', dataIndex: 'reason', ellipsis: true },
    { title: '时间', dataIndex: 'createdAt', width: 170 },
    {
      title: '操作',
      width: 150,
      render: (_: unknown, record: ProcurementRecord) => (
        <Space>
          <Button size="small" type="link" onClick={() => navigate(`/procurements/${record.id}`)}>详情</Button>
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
    { title: '采购类型', dataIndex: 'type', width: 100, render: (v: string) => procurementTypeLabels[v] || v },
    { title: '名称', dataIndex: 'name', width: 150 },
    { title: '申请人', dataIndex: 'requesterName', width: 80 },
    { title: '部门', dataIndex: 'departmentName', width: 120 },
    { title: '数量', dataIndex: 'quantity', width: 80 },
    { title: '总价', dataIndex: 'totalPrice', width: 100 },
    { title: '状态', dataIndex: 'status', width: 80, render: statusBadge },
    { title: '申请原因', dataIndex: 'reason', ellipsis: true },
    { title: '时间', dataIndex: 'createdAt', width: 170 },
    {
      title: '操作',
      width: 160,
      render: (_: unknown, record: ProcurementRequest) => (
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
                    <Option value="PURCHASED">已采购</Option>
                    <Option value="CANCELLED">已取消</Option>
                  </Select>
                  <Select
                    placeholder="采购类型"
                    allowClear
                    style={{ width: 140 }}
                    value={typeFilter}
                    onChange={setTypeFilter}
                  >
                    <Option value="PURCHASE">资产采购</Option>
                    <Option value="CONSUMABLE">耗材采购</Option>
                  </Select>
                  <Input.Search placeholder="搜索名称/申请人" style={{ width: 200 }} />
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
          <p>{actionType === 'approve' ? '确认通过此采购申请？' : '请输入拒绝原因：'}</p>
          <TextArea rows={3} value={comment} onChange={(e) => setComment(e.target.value)} placeholder="可选备注" />
        </Space>
      </Modal>

      <Modal
        title="取消采购"
        open={cancelModalVisible}
        onOk={handleCancel}
        onCancel={() => setCancelModalVisible(false)}
        okText="确认取消"
        okButtonProps={{ danger: true, loading: cancelMutation.isPending }}
      >
        <p>确认取消此采购申请？</p>
      </Modal>
    </Space>
  );
}

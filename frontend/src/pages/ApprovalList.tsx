import { useEffect, useState } from 'react';
import { Table, Button, Space, Tabs, Tag, Modal, Input, message } from 'antd';
import { CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { approvalApi, ApprovalRequest } from '../api/approval';

const { TextArea } = Input;

export default function ApprovalList() {
  const [pending, setPending] = useState<ApprovalRequest[]>([]);
  const [myRequests, setMyRequests] = useState<ApprovalRequest[]>([]);
  const [loading, setLoading] = useState(false);
  const [comment, setComment] = useState('');
  const [modalVisible, setModalVisible] = useState(false);
  const [actionType, setActionType] = useState<'approve' | 'reject'>('approve');
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const fetchPending = async () => {
    setLoading(true);
    try {
      const res = await approvalApi.listPending();
      setPending(res.data);
    } finally {
      setLoading(false);
    }
  };

  const fetchMyRequests = async () => {
    try {
      const res = await approvalApi.listMy(1);
      setMyRequests(res.data);
    } catch {}
  };

  useEffect(() => {
    fetchPending();
    fetchMyRequests();
  }, []);

  const statusBadge = (status: string) => {
    const map: Record<string, { color: string; text: string }> = {
      PENDING: { color: 'processing', text: '待审批' },
      APPROVED: { color: 'success', text: '已通过' },
      REJECTED: { color: 'error', text: '已拒绝' },
    };
    const s = map[status] || { color: 'default', text: status };
    return <Tag color={s.color === 'processing' ? 'blue' : s.color === 'success' ? 'green' : s.color === 'error' ? 'red' : 'default'}>{s.text}</Tag>;
  };

  const typeBadge = (type: string) => {
    const map: Record<string, string> = {
      PROCUREMENT: '采购',
      ASSIGNMENT: '领用',
      RETURN: '归还',
      MAINTENANCE: '维修',
      RETIREMENT: '报废',
    };
    return <Tag>{map[type] || type}</Tag>;
  };

  const openActionModal = (id: number, type: 'approve' | 'reject') => {
    setSelectedId(id);
    setActionType(type);
    setComment('');
    setModalVisible(true);
  };

  const handleAction = async () => {
    if (!selectedId) return;
    try {
      if (actionType === 'approve') {
        await approvalApi.approve(selectedId, comment);
        message.success('审批通过');
      } else {
        await approvalApi.reject(selectedId, comment);
        message.success('已拒绝');
      }
      setModalVisible(false);
      fetchPending();
      fetchMyRequests();
    } catch (e: unknown) {
      message.error((e as { response?: { data?: { error?: string } } })?.response?.data?.error || '操作失败');
    }
  };

  const pendingColumns = [
    { title: '资产名称', dataIndex: 'assetName', key: 'assetName' },
    { title: '资产编号', dataIndex: 'assetCode', key: 'assetCode' },
    { title: '类型', dataIndex: 'type', key: 'type', render: typeBadge },
    { title: '状态', dataIndex: 'status', key: 'status', render: statusBadge },
    { title: '申请原因', dataIndex: 'reason', key: 'reason', ellipsis: true },
    { title: '申请人', dataIndex: 'requesterName', key: 'requesterName' },
    { title: '时间', dataIndex: 'createdAt', key: 'createdAt' },
    {
      title: '操作',
      key: 'action',
      render: (_: unknown, record: ApprovalRequest) => (
        <Space>
          <Button size="small" type="primary" icon={<CheckOutlined />} onClick={() => openActionModal(record.id, 'approve')}>通过</Button>
          <Button size="small" danger icon={<CloseOutlined />} onClick={() => openActionModal(record.id, 'reject')}>拒绝</Button>
        </Space>
      ),
    },
  ];

  const myColumns = [
    { title: '资产名称', dataIndex: 'assetName', key: 'assetName' },
    { title: '资产编号', dataIndex: 'assetCode', key: 'assetCode' },
    { title: '类型', dataIndex: 'type', key: 'type', render: typeBadge },
    { title: '状态', dataIndex: 'status', key: 'status', render: statusBadge },
    { title: '申请原因', dataIndex: 'reason', key: 'reason', ellipsis: true },
    { title: '时间', dataIndex: 'createdAt', key: 'createdAt' },
  ];

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Tabs
        items={[
          {
            key: 'pending',
            label: '待我审批',
            children: (
              <Table loading={loading} dataSource={pending} rowKey="id" columns={pendingColumns} pagination={false} />
            ),
          },
          {
            key: 'my',
            label: '我的申请',
            children: (
              <Table loading={loading} dataSource={myRequests} rowKey="id" columns={myColumns} pagination={false} />
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
          <p>{actionType === 'approve' ? '确认通过此审批申请？' : '请输入拒绝原因：'}</p>
          <TextArea rows={3} value={comment} onChange={(e) => setComment(e.target.value)} placeholder="可选备注" />
        </Space>
      </Modal>
    </Space>
  );
}

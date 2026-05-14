import { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, Modal, Input, message, Card, Row, Col, Select, Tabs, Badge, Statistic } from 'antd';
import { CheckOutlined, CloseOutlined, UndoOutlined } from '@ant-design/icons';
import { useBorrowList, usePendingBorrows, useApproveBorrow, useRejectBorrow, useReturnBorrow, useCancelBorrow } from '../api/borrow';
import type { BorrowRecord } from '../types';
import { borrowStatusLabels, borrowTypeLabels } from '../types';

const { TextArea } = Input;
const { Option } = Select;

export default function BorrowList() {
  const [status, setStatus] = useState<string>();
  const [type, setType] = useState<string>();
  const [keyword, setKeyword] = useState<string>();
  const [pending, setPending] = useState<BorrowRecord[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [actionType, setActionType] = useState<'approve' | 'reject'>('approve');
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [comment, setComment] = useState('');
  const [returnModalVisible, setReturnModalVisible] = useState(false);
  const [returnId, setReturnId] = useState<number | null>(null);

  const { data, isLoading, refetch } = useBorrowList({ status, type, keyword });
  const { data: pendingData, refetch: refetchPending } = usePendingBorrows();
  const approveMutation = useApproveBorrow();
  const rejectMutation = useRejectBorrow();
  const returnMutation = useReturnBorrow();
  const cancelMutation = useCancelBorrow();

  useEffect(() => {
    if (pendingData) {
      setPending(pendingData);
    }
  }, [pendingData]);

  const statusBadge = (status: string) => {
    const colorMap: Record<string, string> = {
      PENDING: 'blue',
      APPROVED: 'green',
      REJECTED: 'red',
      BORROWED: 'orange',
      RETURNED: 'purple',
      CANCELLED: 'default',
    };
    return <Tag color={colorMap[status] || 'default'}>{borrowStatusLabels[status] || status}</Tag>;
  };

  const typeBadge = (type: string) => {
    const colorMap: Record<string, string> = {
      BORROW: 'blue',
      RETURN: 'green',
    };
    return <Tag color={colorMap[type] || 'default'}>{borrowTypeLabels[type] || type}</Tag>;
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
      refetchPending();
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
      refetchPending();
    } catch {
      message.error('归还失败');
    }
  };

  const handleCancel = async (id: number) => {
    try {
      await cancelMutation.mutateAsync(id);
      message.success('已取消');
      refetch();
      refetchPending();
    } catch {
      message.error('取消失败');
    }
  };

  const openReturnModal = (id: number) => {
    setReturnId(id);
    setReturnModalVisible(true);
  };

  const columns = [
    { title: '资产编码', dataIndex: 'assetCode', width: 120 },
    { title: '资产名称', dataIndex: 'assetName', width: 150 },
    { title: '类型', dataIndex: 'type', width: 80, render: typeBadge },
    { title: '状态', dataIndex: 'status', width: 100, render: statusBadge },
    { title: '借用人', dataIndex: 'borrowerName', width: 100 },
    { title: '部门', dataIndex: 'departmentName', width: 120 },
    { title: '预计归还', dataIndex: 'expectedReturnDate', width: 120 },
    { title: '实际归还', dataIndex: 'actualReturnDate', width: 120 },
    { title: '申请原因', dataIndex: 'reason', ellipsis: true },
    { title: '时间', dataIndex: 'createdAt', width: 170 },
    {
      title: '操作',
      width: 200,
      render: (_: unknown, record: { id: number; status: string }) => (
        <Space>
          {record.status === 'PENDING' && (
            <>
              <Button size="small" type="primary" icon={<CheckOutlined />} onClick={() => openActionModal(record.id, 'approve')}>通过</Button>
              <Button size="small" danger icon={<CloseOutlined />} onClick={() => openActionModal(record.id, 'reject')}>拒绝</Button>
            </>
          )}
          {record.status === 'APPROVED' && (
            <Button size="small" type="primary" icon={<UndoOutlined />} onClick={() => openReturnModal(record.id)}>确认归还</Button>
          )}
          {(record.status === 'PENDING' || record.status === 'APPROVED') && (
            <Button size="small" onClick={() => handleCancel(record.id)}>取消</Button>
          )}
        </Space>
      ),
    },
  ];

  const pendingColumns = [
    { title: '资产编码', dataIndex: 'assetCode', width: 120 },
    { title: '资产名称', dataIndex: 'assetName', width: 150 },
    { title: '类型', dataIndex: 'type', width: 80, render: typeBadge },
    { title: '借用人', dataIndex: 'borrowerName', width: 100 },
    { title: '部门', dataIndex: 'departmentName', width: 120 },
    { title: '预计归还', dataIndex: 'expectedReturnDate', width: 120 },
    { title: '申请原因', dataIndex: 'reason', ellipsis: true },
    { title: '时间', dataIndex: 'createdAt', width: 170 },
    {
      title: '操作',
      width: 160,
      render: (_: unknown, record: { id: number }) => (
        <Space>
          <Button size="small" type="primary" icon={<CheckOutlined />} onClick={() => openActionModal(record.id, 'approve')}>通过</Button>
          <Button size="small" danger icon={<CloseOutlined />} onClick={() => openActionModal(record.id, 'reject')}>拒绝</Button>
        </Space>
      ),
    },
  ];

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Row gutter={16}>
        <Col span={6}>
          <Card>
            <Statistic title="待审批" value={pending.length} valueStyle={{ color: '#cf1322' }} />
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
                  <Select placeholder="状态" allowClear style={{ width: 140 }} value={status} onChange={setStatus}>
                    <Option value="PENDING">待审批</Option>
                    <Option value="APPROVED">已批准</Option>
                    <Option value="REJECTED">已拒绝</Option>
                    <Option value="BORROWED">已借出</Option>
                    <Option value="RETURNED">已归还</Option>
                    <Option value="CANCELLED">已取消</Option>
                  </Select>
                  <Select placeholder="类型" allowClear style={{ width: 120 }} value={type} onChange={setType}>
                    <Option value="BORROW">借出</Option>
                    <Option value="RETURN">归还</Option>
                  </Select>
                  <Input.Search placeholder="搜索资产/借用人" style={{ width: 200 }} onSearch={setKeyword} />
                </Space>
                <Table loading={isLoading} dataSource={data?.content} rowKey="id" columns={columns} pagination={{ total: data?.totalElements, defaultPageSize: 10 }} />
              </>
            ),
          },
          {
            key: 'pending',
            label: <span>待我审批 <Badge count={pending.length} style={{ backgroundColor: '#faad14' }} /></span>,
            children: (
              <Table dataSource={pending} rowKey="id" columns={pendingColumns} pagination={false} />
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
        okButtonProps={{ danger: actionType === 'reject', loading: approveMutation.isPending || rejectMutation.isPending }}
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

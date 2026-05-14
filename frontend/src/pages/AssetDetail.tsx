import { Descriptions, Card, Button, Space, Tag, Table, Modal, InputNumber, message, Input } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import {
  useAsset,
  useMaintenanceRecords,
  useAssignAsset,
  useUnassignAsset,
  useRetireAsset,
} from '../api/asset';
import { approvalApi } from '../api/approval';

const { TextArea } = Input;

const statusMap: Record<string, { color: string; label: string }> = {
  IN_STOCK: { color: 'green', label: '库存' },
  IN_USE: { color: 'blue', label: '已领用' },
  MAINTENANCE: { color: 'orange', label: '维修中' },
  RETIRED: { color: 'red', label: '已报废' },
};

const categoryLabel: Record<string, string> = {
  HARDWARE: '硬件设备',
  NETWORK: '网络设备',
  PERIPHERAL: '配件耗材',
  SOFTWARE_LICENSE: '软件许可证',
};

const getCurrentEmployeeId = (): number | null => {
  const stored = localStorage.getItem('currentEmployeeId');
  return stored ? parseInt(stored, 10) : null;
};

export default function AssetDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const numericId = Number(id);

  const { data: asset, isLoading, refetch } = useAsset(numericId);
  const { data: maintenanceRecords } = useMaintenanceRecords(numericId);
  const assignMutation = useAssignAsset();
  const unassignMutation = useUnassignAsset();
  const retireMutation = useRetireAsset();

  const [assignModalVisible, setAssignModalVisible] = useState(false);
  const [applyModalVisible, setApplyModalVisible] = useState(false);
  const [applyType, setApplyType] = useState<'ASSIGNMENT' | 'MAINTENANCE'>('ASSIGNMENT');
  const [assigneeId, setAssigneeId] = useState<number>();
  const [applyReason, setApplyReason] = useState('');

  const currentEmployeeId = getCurrentEmployeeId();

  const handleAssign = async () => {
    if (!assigneeId) {
      message.error('请输入领用人ID');
      return;
    }
    await assignMutation.mutateAsync({ id: numericId, assigneeId });
    message.success('领用成功');
    setAssignModalVisible(false);
    refetch();
  };

  const handleUnassign = async () => {
    await unassignMutation.mutateAsync(numericId);
    message.success('归还成功');
    refetch();
  };

  const handleRetire = async () => {
    Modal.confirm({
      title: '确认报废',
      content: '确定要报废该资产吗？',
      onOk: async () => {
        await retireMutation.mutateAsync(numericId);
        message.success('报废成功');
        refetch();
      },
    });
  };

  const openApplyModal = (type: 'ASSIGNMENT' | 'MAINTENANCE') => {
    if (!currentEmployeeId) {
      message.warning('请先在员工列表选择当前登录员工（localStorage currentEmployeeId）');
      return;
    }
    setApplyType(type);
    setApplyReason('');
    setApplyModalVisible(true);
  };

  const handleApply = async () => {
    if (!currentEmployeeId) {
      message.warning('请先在员工列表选择当前登录员工');
      return;
    }
    if (!applyReason.trim()) {
      message.error('请填写申请原因');
      return;
    }
    try {
      await approvalApi.create({
        requesterId: currentEmployeeId,
        assetId: numericId,
        departmentId: 1, // default department
        type: applyType,
        reason: applyReason,
      });
      message.success(applyType === 'ASSIGNMENT' ? '领用申请已提交' : '维修申请已提交');
      setApplyModalVisible(false);
    } catch (e: unknown) {
      message.error('提交申请失败');
    }
  };

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Card
        loading={isLoading}
        title={`资产详情 - ${asset?.assetCode ?? ''}`}
        extra={
          <Space>
            <Button onClick={() => navigate('/assets')}>返回列表</Button>
          </Space>
        }
      >
        <Descriptions column={2} bordered>
          <Descriptions.Item label="资产编码">{asset?.assetCode}</Descriptions.Item>
          <Descriptions.Item label="名称">{asset?.name}</Descriptions.Item>
          <Descriptions.Item label="分类">{categoryLabel[asset?.category ?? ''] || asset?.category}</Descriptions.Item>
          <Descriptions.Item label="状态">
            {asset?.status && <Tag color={statusMap[asset.status]?.color}>{statusMap[asset.status]?.label}</Tag>}
          </Descriptions.Item>
          <Descriptions.Item label="规格">{asset?.spec}</Descriptions.Item>
          <Descriptions.Item label="存放地点">{asset?.location}</Descriptions.Item>
          <Descriptions.Item label="购入日期">{asset?.purchaseDate}</Descriptions.Item>
          <Descriptions.Item label="价格">{asset?.purchasePrice} 元</Descriptions.Item>
          <Descriptions.Item label="保修截止">{asset?.warrantyEnd}</Descriptions.Item>
          <Descriptions.Item label="供应商">{asset?.supplier}</Descriptions.Item>
          <Descriptions.Item label="领用人">{asset?.assigneeName ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{asset?.createdAt}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="操作">
        <Space wrap>
          {asset?.status === 'IN_STOCK' && (
            <Button type="primary" onClick={() => setAssignModalVisible(true)}>领用</Button>
          )}
          {asset?.status === 'IN_USE' && (
            <Button onClick={handleUnassign}>归还</Button>
          )}
          <Button onClick={handleRetire}>报废</Button>
          <Button onClick={() => navigate('/maintenance/new', { state: { assetId: numericId } })}>送修</Button>
          <Button onClick={() => openApplyModal('ASSIGNMENT')}>申请领用</Button>
          <Button onClick={() => openApplyModal('MAINTENANCE')}>申请维修</Button>
        </Space>
      </Card>

      <Card title="维修记录">
        <Table
          dataSource={maintenanceRecords}
          rowKey="id"
          pagination={false}
          size="small"
          columns={[
            { title: '审批ID', dataIndex: 'approvalId' },
            { title: '状态', dataIndex: 'status' },
            { title: '类型', dataIndex: 'type' },
            { title: '描述', dataIndex: 'description' },
            { title: '费用', dataIndex: 'cost' },
            { title: '开始日期', dataIndex: 'startDate' },
            { title: '结束日期', dataIndex: 'endDate' },
            { title: '维修商', dataIndex: 'vendor' },
          ]}
        />
      </Card>

      <Modal
        title="领用资产"
        open={assignModalVisible}
        onOk={handleAssign}
        onCancel={() => setAssignModalVisible(false)}
        confirmLoading={assignMutation.isPending}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <span>请输入领用人ID：</span>
          <InputNumber
            style={{ width: '100%' }}
            placeholder="领用人ID"
            value={assigneeId}
            onChange={(val) => setAssigneeId(val ?? undefined)}
          />
        </Space>
      </Modal>

      <Modal
        title={applyType === 'ASSIGNMENT' ? '申请领用' : '申请维修'}
        open={applyModalVisible}
        onOk={handleApply}
        onCancel={() => setApplyModalVisible(false)}
        confirmLoading={false}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <span>申请原因：</span>
          <TextArea
            rows={4}
            placeholder="请输入申请原因"
            value={applyReason}
            onChange={(e) => setApplyReason(e.target.value)}
          />
        </Space>
      </Modal>
    </Space>
  );
}

import { Card, Descriptions, Button, Space, Tag, Steps, message } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { useTransfer, useApproveTransfer, useRejectTransfer } from '../api/transfer';
import { transferStatusLabels, transferStatusColors, transferTypeLabels } from '../types/transfer';

export default function TransferDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const numericId = Number(id);

  const { data: transfer, isLoading, refetch } = useTransfer(numericId);
  const approveMutation = useApproveTransfer();
  const rejectMutation = useRejectTransfer();

  const handleApprove = async () => {
    try {
      await approveMutation.mutateAsync({ id: numericId });
      message.success('审批通过');
      refetch();
    } catch {
      message.error('操作失败');
    }
  };

  const handleReject = async () => {
    try {
      await rejectMutation.mutateAsync({ id: numericId });
      message.success('已拒绝');
      refetch();
    } catch {
      message.error('操作失败');
    }
  };

  const currentStep = transfer?.status === 'COMPLETED' ? 2 : transfer?.status === 'APPROVED' ? 1 : 0;
  const stepItems = [
    { title: '提交申请', status: 'finish' as const },
    { title: '审批中', status: currentStep >= 1 ? 'finish' as const : 'wait' as const },
    { title: '完成', status: currentStep >= 2 ? 'finish' as const : 'wait' as const },
  ];

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Card
        loading={isLoading}
        title={`调拨详情 - ${transfer?.assetCode ?? ''}`}
        extra={
          <Button onClick={() => navigate('/transfers')}>返回列表</Button>
        }
      >
        <Descriptions column={2} bordered>
          <Descriptions.Item label="资产编码">{transfer?.assetCode}</Descriptions.Item>
          <Descriptions.Item label="资产名称">{transfer?.assetName}</Descriptions.Item>
          <Descriptions.Item label="调拨类型">
            {transfer?.transferType && <Tag>{transferTypeLabels[transfer.transferType]}</Tag>}
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            {transfer?.status && (
              <Tag color={transferStatusColors[transfer.status]}>
                {transferStatusLabels[transfer.status]}
              </Tag>
            )}
          </Descriptions.Item>
          <Descriptions.Item label="调出部门">{transfer?.fromDepartmentName}</Descriptions.Item>
          <Descriptions.Item label="调入部门">{transfer?.toDepartmentName}</Descriptions.Item>
          {transfer?.fromPersonName && (
            <Descriptions.Item label="调出人员">{transfer.fromPersonName}</Descriptions.Item>
          )}
          {transfer?.toPersonName && (
            <Descriptions.Item label="调入人员">{transfer.toPersonName}</Descriptions.Item>
          )}
          <Descriptions.Item label="申请原因" span={2}>{transfer?.reason || '-'}</Descriptions.Item>
          {transfer?.managerComment && (
            <Descriptions.Item label="审批备注" span={2}>{transfer.managerComment}</Descriptions.Item>
          )}
          <Descriptions.Item label="申请人">{transfer?.requesterName}</Descriptions.Item>
          <Descriptions.Item label="申请时间">{transfer?.createdAt}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="审批流程">
        <Steps current={currentStep} items={stepItems} />
      </Card>

      <Card title="操作">
        <Space>
          {transfer?.status === 'PENDING' && (
            <>
              <Button type="primary" onClick={handleApprove} loading={approveMutation.isPending}>
                通过申请
              </Button>
              <Button danger onClick={handleReject} loading={rejectMutation.isPending}>
                拒绝申请
              </Button>
            </>
          )}
          <Button onClick={() => navigate('/transfers')}>返回</Button>
        </Space>
      </Card>
    </Space>
  );
}
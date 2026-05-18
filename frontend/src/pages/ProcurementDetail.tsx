import { Card, Descriptions, Button, Space, Tag, Steps, message } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { useProcurement, useApproveProcurement, useRejectProcurement } from '../api/procurement';
import { procurementStatusLabels, procurementStatusColors, procurementTypeLabels } from '../types/procurement';

export default function ProcurementDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const numericId = Number(id);

  const { data: procurement, isLoading, refetch } = useProcurement(numericId);
  const approveMutation = useApproveProcurement();
  const rejectMutation = useRejectProcurement();

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

  const currentStep = procurement?.status === 'PURCHASED' ? 2 :
    procurement?.status === 'APPROVED' ? 1 :
    procurement?.status === 'REJECTED' || procurement?.status === 'CANCELLED' ? -1 : 0;

  const stepItems = [
    { title: '提交申请', status: 'finish' as const },
    { title: '审批中', status: currentStep >= 1 ? 'finish' as const : 'wait' as const },
    { title: '完成', status: currentStep >= 2 ? 'finish' as const : 'wait' as const },
  ];

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Card
        loading={isLoading}
        title={`采购详情 - ${procurement?.name ?? ''}`}
        extra={
          <Button onClick={() => navigate('/procurements')}>返回列表</Button>
        }
      >
        <Descriptions column={2} bordered>
          <Descriptions.Item label="资产编码">{procurement?.assetCode || '-'}</Descriptions.Item>
          <Descriptions.Item label="资产名称">{procurement?.assetName || '-'}</Descriptions.Item>
          <Descriptions.Item label="采购类型">
            {procurement?.type && <Tag>{procurementTypeLabels[procurement.type]}</Tag>}
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            {procurement?.status && (
              <Tag color={procurementStatusColors[procurement.status]}>
                {procurementStatusLabels[procurement.status]}
              </Tag>
            )}
          </Descriptions.Item>
          <Descriptions.Item label="采购名称">{procurement?.name}</Descriptions.Item>
          <Descriptions.Item label="数量">{procurement?.quantity}</Descriptions.Item>
          <Descriptions.Item label="单价">{procurement?.unitPrice} 元</Descriptions.Item>
          <Descriptions.Item label="总价">{procurement?.totalPrice} 元</Descriptions.Item>
          <Descriptions.Item label="部门">{procurement?.departmentName}</Descriptions.Item>
          <Descriptions.Item label="申请人">{procurement?.requesterName}</Descriptions.Item>
          <Descriptions.Item label="采购说明" span={2}>{procurement?.description || '-'}</Descriptions.Item>
          <Descriptions.Item label="申请原因" span={2}>{procurement?.reason || '-'}</Descriptions.Item>
          {procurement?.managerComment && (
            <Descriptions.Item label="审批备注" span={2}>{procurement.managerComment}</Descriptions.Item>
          )}
          <Descriptions.Item label="申请时间">{procurement?.createdAt}</Descriptions.Item>
        </Descriptions>
      </Card>

      {currentStep >= 0 && (
        <Card title="审批流程">
          <Steps current={currentStep} items={stepItems} />
        </Card>
      )}

      <Card title="操作">
        <Space>
          {procurement?.status === 'PENDING' && (
            <>
              <Button type="primary" onClick={handleApprove} loading={approveMutation.isPending}>
                通过申请
              </Button>
              <Button danger onClick={handleReject} loading={rejectMutation.isPending}>
                拒绝申请
              </Button>
            </>
          )}
          <Button onClick={() => navigate('/procurements')}>返回</Button>
        </Space>
      </Card>
    </Space>
  );
}

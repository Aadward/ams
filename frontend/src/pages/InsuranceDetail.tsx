import { Descriptions, Card, Button, Space, Tag, Row, Col } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { insuranceApi } from '../api/insurance';

const statusMap: Record<string, { color: string; label: string }> = {
  ACTIVE: { color: 'green', label: '有效' },
  EXPIRED: { color: 'red', label: '已过期' },
  CANCELLED: { color: 'orange', label: '已取消' },
};

const claimStatusMap: Record<string, { color: string; label: string }> = {
  NONE: { color: 'default', label: '无理赔' },
  PENDING: { color: 'orange', label: '待处理' },
  APPROVED: { color: 'green', label: '已批准' },
  REJECTED: { color: 'red', label: '已拒绝' },
};

export default function InsuranceDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const numericId = Number(id);

  const { data: insurance, isLoading } = useQuery({
    queryKey: ['insurance', numericId],
    queryFn: async () => {
      const { data } = await insuranceApi.getById(numericId);
      return data;
    },
  });

  if (isLoading) {
    return <Card loading />;
  }

  if (!insurance) {
    return (
      <Card>
        <div>保险记录不存在</div>
        <Button onClick={() => navigate('/insurance')}>返回列表</Button>
      </Card>
    );
  }

  const isExpiringSoon = insurance.endDate && dayjs(insurance.endDate).isBefore(dayjs().add(30, 'day')) && insurance.status === 'ACTIVE';
  const daysUntilExpiry = insurance.endDate ? dayjs(insurance.endDate).diff(dayjs(), 'day') : null;

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Card
        title={`保险详情 - ${insurance.policyNumber}`}
        extra={
          <Space>
            <Button onClick={() => navigate('/insurance')}>返回列表</Button>
            <Button type="primary" onClick={() => navigate(`/insurance/${id}/edit`)}>
              编辑
            </Button>
          </Space>
        }
      >
        <Descriptions column={2} bordered>
          <Descriptions.Item label="ID">{insurance.id}</Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={statusMap[insurance.status]?.color}>
              {statusMap[insurance.status]?.label || insurance.status}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="资产ID">{insurance.assetId}</Descriptions.Item>
          <Descriptions.Item label="资产编码">{insurance.assetCode}</Descriptions.Item>
          <Descriptions.Item label="资产名称">{insurance.assetName}</Descriptions.Item>
          <Descriptions.Item label="保单号">{insurance.policyNumber}</Descriptions.Item>
          <Descriptions.Item label="保险公司">{insurance.insuranceCompany}</Descriptions.Item>
          <Descriptions.Item label="保险类型">{insurance.insuranceType}</Descriptions.Item>
          <Descriptions.Item label="保险金额">
            {insurance.coverageAmount != null ? `¥${insurance.coverageAmount.toLocaleString()}` : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="保费">
            {insurance.premium != null ? `¥${insurance.premium.toLocaleString()}` : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="生效日期">{insurance.startDate}</Descriptions.Item>
          <Descriptions.Item label="到期日期">
            <span style={{ color: isExpiringSoon ? '#ff4d4f' : 'inherit' }}>
              {insurance.endDate}
              {isExpiringSoon && ` (还有 ${daysUntilExpiry} 天到期)`}
            </span>
          </Descriptions.Item>
          <Descriptions.Item label="理赔状态">
            <Tag color={claimStatusMap[insurance.claimStatus || 'NONE']?.color}>
              {claimStatusMap[insurance.claimStatus || 'NONE']?.label}
            </Tag>
          </Descriptions.Item>
          {insurance.claimAmount != null && (
            <Descriptions.Item label="理赔金额">
              ¥{insurance.claimAmount.toLocaleString()}
            </Descriptions.Item>
          )}
          {insurance.claimDate && (
            <Descriptions.Item label="理赔日期">{insurance.claimDate}</Descriptions.Item>
          )}
          <Descriptions.Item label="创建时间">{insurance.createdAt}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{insurance.updatedAt}</Descriptions.Item>
          <Descriptions.Item label="备注" span={2}>
            {insurance.remarks || '-'}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="快捷操作">
        <Space wrap>
          <Button type="primary" onClick={() => navigate(`/insurance/${id}/claim`)}>
            {insurance.claimStatus === 'NONE' ? '申请理赔' : '查看理赔'}
          </Button>
          <Button onClick={() => navigate(`/assets/${insurance.assetId}`)}>
            查看资产
          </Button>
          <Button onClick={() => navigate(`/insurance/${id}/edit`)}>
            编辑保险
          </Button>
        </Space>
      </Card>

      <Card title="保险信息摘要">
        <Row gutter={16}>
          <Col span={8}>
            <Card size="small">
              <Descriptions column={1} size="small">
                <Descriptions.Item label="保险金额">
                  <strong style={{ fontSize: 18, color: '#1890ff' }}>
                    ¥{insurance.coverageAmount?.toLocaleString() || '0'}
                  </strong>
                </Descriptions.Item>
                <Descriptions.Item label="状态">
                  <Tag color={statusMap[insurance.status]?.color}>
                    {statusMap[insurance.status]?.label}
                  </Tag>
                </Descriptions.Item>
              </Descriptions>
            </Card>
          </Col>
          <Col span={8}>
            <Card size="small">
              <Descriptions column={1} size="small">
                <Descriptions.Item label="保费">
                  <strong style={{ fontSize: 18 }}>¥{insurance.premium?.toLocaleString() || '0'}</strong>
                </Descriptions.Item>
                <Descriptions.Item label="保险类型">{insurance.insuranceType}</Descriptions.Item>
              </Descriptions>
            </Card>
          </Col>
          <Col span={8}>
            <Card size="small">
              <Descriptions column={1} size="small">
                <Descriptions.Item label="到期日期">
                  <span style={{ color: isExpiringSoon ? '#ff4d4f' : 'inherit', fontSize: 16 }}>
                    {insurance.endDate || '-'}
                  </span>
                </Descriptions.Item>
                <Descriptions.Item label="理赔状态">
                  <Tag color={claimStatusMap[insurance.claimStatus || 'NONE']?.color}>
                    {claimStatusMap[insurance.claimStatus || 'NONE']?.label}
                  </Tag>
                </Descriptions.Item>
              </Descriptions>
            </Card>
          </Col>
        </Row>
      </Card>
    </Space>
  );
}

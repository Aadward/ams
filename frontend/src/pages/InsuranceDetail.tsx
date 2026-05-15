import { Descriptions, Card, Button, Space, Tag, Row, Col, Table, Popconfirm, message } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { insuranceApi } from '../api/insurance';
import type { ClaimRecord } from '../api/insurance';

const statusMap: Record<string, { color: string; label: string }> = {
  ACTIVE: { color: 'green', label: '有效' },
  EXPIRED: { color: 'red', label: '已过期' },
  CANCELLED: { color: 'orange', label: '已取消' },
};

const claimStatusMap: Record<string, { color: string; label: string }> = {
  PENDING: { color: 'orange', label: '待处理' },
  SETTLED: { color: 'green', label: '已赔付' },
  REJECTED: { color: 'red', label: '已拒绝' },
};

export default function InsuranceDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const numericId = Number(id);
  const queryClient = useQueryClient();

  const { data: insurance, isLoading } = useQuery({
    queryKey: ['insurance-policy', numericId],
    queryFn: async () => {
      const { data } = await insuranceApi.getById(numericId);
      return data;
    },
  });

  const { data: claimsData } = useQuery({
    queryKey: ['insurance-policy-claims', numericId],
    queryFn: async () => {
      const { data } = await insuranceApi.getClaimsByPolicy(numericId);
      return data;
    },
  });

  const deleteClaimMutation = useMutation({
    mutationFn: (claimId: number) => insuranceApi.deleteClaim(claimId),
    onSuccess: () => {
      message.success('理赔记录删除成功');
      queryClient.invalidateQueries({ queryKey: ['insurance-policy-claims', numericId] });
    },
    onError: () => {
      message.error('删除失败');
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

  const claimColumns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 60,
    },
    {
      title: '索赔单号',
      dataIndex: 'claimNumber',
      width: 150,
    },
    {
      title: '出险日期',
      dataIndex: 'incidentDate',
      width: 120,
    },
    {
      title: '索赔金额',
      dataIndex: 'claimAmount',
      width: 120,
      render: (val: number) => val != null ? `¥${val.toLocaleString()}` : '-',
    },
    {
      title: '赔付金额',
      dataIndex: 'settledAmount',
      width: 120,
      render: (val: number) => val != null ? `¥${val.toLocaleString()}` : '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status: string) => (
        <Tag color={claimStatusMap[status]?.color}>{claimStatusMap[status]?.label || status}</Tag>
      ),
    },
    {
      title: '出险经过',
      dataIndex: 'incidentDescription',
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_: unknown, record: ClaimRecord) => (
        <Popconfirm
          title="确认删除此理赔记录"
          onConfirm={() => deleteClaimMutation.mutate(record.id)}
          okText="确认"
          cancelText="取消"
        >
          <Button size="small" danger>
            删除
          </Button>
        </Popconfirm>
      ),
    },
  ];

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
          <Descriptions.Item label="保险类型">{insurance.type}</Descriptions.Item>
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
            新办理赔
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
                <Descriptions.Item label="保险类型">{insurance.type}</Descriptions.Item>
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
                <Descriptions.Item label="剩余天数">
                  {daysUntilExpiry != null ? `${daysUntilExpiry} 天` : '-'}
                </Descriptions.Item>
              </Descriptions>
            </Card>
          </Col>
        </Row>
      </Card>

      <Card title="理赔记录">
        <Table
          dataSource={claimsData || []}
          columns={claimColumns}
          rowKey="id"
          pagination={false}
          size="small"
        />
        {!claimsData || claimsData.length === 0 ? (
          <div style={{ textAlign: 'center', color: '#666', padding: 24 }}>
            暂无理赔记录
          </div>
        ) : null}
      </Card>
    </Space>
  );
}
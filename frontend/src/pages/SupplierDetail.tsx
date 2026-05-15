import { Descriptions, Card, Button, Space } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { supplierApi } from '../api/supplier';

export default function SupplierDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const numericId = Number(id);

  const { data: supplier, isLoading } = useQuery({
    queryKey: ['supplier', numericId],
    queryFn: async () => {
      const { data } = await supplierApi.getById(numericId);
      return data;
    },
    enabled: !!numericId,
  });

  return (
    <Card
      loading={isLoading}
      title={`供应商详情 - ${supplier?.name ?? ''}`}
      extra={
        <Space>
          <Button onClick={() => navigate('/suppliers')}>返回列表</Button>
          <Button type="primary" onClick={() => navigate(`/suppliers/${id}/edit`)}>
            编辑
          </Button>
        </Space>
      }
    >
      <Descriptions column={2} bordered>
        <Descriptions.Item label="ID">{supplier?.id}</Descriptions.Item>
        <Descriptions.Item label="供应商名称">{supplier?.name}</Descriptions.Item>
        <Descriptions.Item label="联系人">{supplier?.contactPerson ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="电话">{supplier?.phone ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="邮箱">{supplier?.email ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="地址" span={2}>{supplier?.address ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="备注" span={2}>{supplier?.remark ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="创建时间">{supplier?.createdAt}</Descriptions.Item>
        <Descriptions.Item label="更新时间">{supplier?.updatedAt}</Descriptions.Item>
      </Descriptions>
    </Card>
  );
}

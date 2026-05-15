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
        <Descriptions.Item label="供应商编码">{supplier?.supplierCode ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="供应商名称">{supplier?.name}</Descriptions.Item>
        <Descriptions.Item label="类型">{supplier?.type === 'EQUIPMENT' ? '设备供应商' : supplier?.type === 'CONSUMABLE' ? '易耗品供应商' : supplier?.type === 'MAINTENANCE' ? '维修服务商' : supplier?.type === 'MULTI' ? '多元化供应商' : (supplier?.type ?? '-')}</Descriptions.Item>
        <Descriptions.Item label="状态">{supplier?.status === 'ACTIVE' ? '启用' : '停用'}</Descriptions.Item>
        <Descriptions.Item label="联系人">{supplier?.contact ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="电话">{supplier?.phone ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="邮箱">{supplier?.email ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="地址" span={2}>{supplier?.address ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="评级">{supplier?.rating != null && supplier.rating > 0 ? '★'.repeat(Math.floor(supplier.rating)) : supplier?.rating === 0 ? '0星' : '-'}</Descriptions.Item>
        <Descriptions.Item label="备注" span={2}>{supplier?.remark ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="创建时间">{supplier?.createdAt}</Descriptions.Item>
        <Descriptions.Item label="更新时间">{supplier?.updatedAt}</Descriptions.Item>
      </Descriptions>
    </Card>
  );
}

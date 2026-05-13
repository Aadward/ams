import { Descriptions, Card, Button, Space, Tag } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { useAsset } from '../api/asset';

export default function AssetDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: asset, isLoading } = useAsset(Number(id));

  const statusMap: Record<string, { color: string; label: string }> = {
    IN_STOCK: { color: 'green', label: '库存' },
    IN_USE: { color: 'blue', label: '已领用' },
    MAINTENANCE: { color: 'orange', label: '维修中' },
    RETIRED: { color: 'red', label: '已报废' },
  };

  return (
    <Card loading={isLoading} title={`资产详情 - ${asset?.assetCode ?? ''}`}
      extra={<Space>
        <Button onClick={() => navigate(`/assets/${id}/edit`)}>编辑</Button>
        <Button onClick={() => navigate('/assets')}>返回</Button>
      </Space>}>
      <Descriptions column={2} bordered>
        <Descriptions.Item label="资产编码">{asset?.assetCode}</Descriptions.Item>
        <Descriptions.Item label="名称">{asset?.name}</Descriptions.Item>
        <Descriptions.Item label="分类">{asset?.category}</Descriptions.Item>
        <Descriptions.Item label="状态">
          {asset?.status && <Tag color={statusMap[asset.status]?.color}>{statusMap[asset.status]?.label}</Tag>}
        </Descriptions.Item>
        <Descriptions.Item label="规格">{asset?.spec}</Descriptions.Item>
        <Descriptions.Item label="存放地点">{asset?.location}</Descriptions.Item>
        <Descriptions.Item label="采购日期">{asset?.purchaseDate}</Descriptions.Item>
        <Descriptions.Item label="采购价格">{asset?.purchasePrice} 元</Descriptions.Item>
        <Descriptions.Item label="保修到期">{asset?.warrantyEnd}</Descriptions.Item>
        <Descriptions.Item label="供应商">{asset?.supplier}</Descriptions.Item>
        <Descriptions.Item label="领用人">{asset?.assigneeName ?? '-'}</Descriptions.Item>
        <Descriptions.Item label="创建时间">{asset?.createdAt}</Descriptions.Item>
      </Descriptions>
    </Card>
  );
}

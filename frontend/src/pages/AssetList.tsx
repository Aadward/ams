import { Table, Button, Space, Select, Tag } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAssetList } from '../api/asset';

const { Option } = Select;

const categoryBadgeColor: Record<string, string> = {
  HARDWARE: 'blue',
  NETWORK: 'green',
  PERIPHERAL: 'orange',
  SOFTWARE_LICENSE: 'purple',
};

const categoryLabel: Record<string, string> = {
  HARDWARE: '硬件设备',
  NETWORK: '网络设备',
  PERIPHERAL: '配件耗材',
  SOFTWARE_LICENSE: '软件许可证',
};

const statusBadgeColor: Record<string, string> = {
  IN_STOCK: 'green',
  IN_USE: 'blue',
  MAINTENANCE: 'orange',
  RETIRED: 'red',
};

const statusLabel: Record<string, string> = {
  IN_STOCK: '库存',
  IN_USE: '已领用',
  MAINTENANCE: '维修中',
  RETIRED: '已报废',
};

export default function AssetList() {
  const navigate = useNavigate();
  const [category, setCategory] = useState<string>();
  const [status, setStatus] = useState<string>();
  const { data, isLoading } = useAssetList({ category, status });

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Space>
        <Select
          placeholder="分类"
          allowClear
          style={{ width: 160 }}
          value={category}
          onChange={setCategory}
        >
          <Option value="HARDWARE">硬件设备</Option>
          <Option value="NETWORK">网络设备</Option>
          <Option value="PERIPHERAL">配件耗材</Option>
          <Option value="SOFTWARE_LICENSE">软件许可证</Option>
        </Select>
        <Select
          placeholder="状态"
          allowClear
          style={{ width: 140 }}
          value={status}
          onChange={setStatus}
        >
          <Option value="IN_STOCK">库存</Option>
          <Option value="IN_USE">已领用</Option>
          <Option value="MAINTENANCE">维修中</Option>
          <Option value="RETIRED">已报废</Option>
        </Select>
        <Button type="primary" onClick={() => navigate('/assets/new')}>新建资产</Button>
      </Space>
      <Table
        loading={isLoading}
        dataSource={data?.content}
        rowKey="id"
        pagination={{ total: data?.totalElements, defaultPageSize: 10 }}
        columns={[
          { title: '资产编码', dataIndex: 'assetCode' },
          { title: '名称', dataIndex: 'name' },
          {
            title: '分类',
            dataIndex: 'category',
            render: (val: string) => (
              <Tag color={categoryBadgeColor[val]}>{categoryLabel[val] || val}</Tag>
            ),
          },
          {
            title: '状态',
            dataIndex: 'status',
            render: (val: string) => (
              <Tag color={statusBadgeColor[val]}>{statusLabel[val] || val}</Tag>
            ),
          },
          { title: '购入日期', dataIndex: 'purchaseDate' },
          { title: '价格', dataIndex: 'purchasePrice' },
          { title: '位置', dataIndex: 'location' },
          {
            title: '操作',
            render: (_: unknown, record: { id: number }) => (
              <Space>
                <Button size="small" onClick={() => navigate(`/assets/${record.id}`)}>查看</Button>
                <Button size="small" onClick={() => navigate(`/assets/${record.id}/edit`)}>编辑</Button>
              </Space>
            ),
          },
        ]}
      />
    </Space>
  );
}
import { Table, Button, Space, Input, Select } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAssetList } from '../api/asset';

const { Option } = Select;

export default function AssetList() {
  const navigate = useNavigate();
  const { data, isLoading } = useAssetList();

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Space>
        <Input.Search placeholder="搜索资产名称/编码" style={{ width: 240 }} />
        <Select placeholder="分类" allowClear style={{ width: 160 }}>
          <Option value="HARDWARE">硬件设备</Option>
          <Option value="NETWORK">网络设备</Option>
          <Option value="PERIPHERAL">配件耗材</Option>
          <Option value="SOFTWARE_LICENSE">软件许可证</Option>
        </Select>
        <Select placeholder="状态" allowClear style={{ width: 140 }}>
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
        pagination={{ total: data?.totalElements, pageSize: data?.size }}
        onRow={(record) => ({
          onClick: () => navigate(`/assets/${record.id}`),
          style: { cursor: 'pointer' },
        })}
        columns={[
          { title: '资产编码', dataIndex: 'assetCode' },
          { title: '名称', dataIndex: 'name' },
          { title: '分类', dataIndex: 'category' },
          { title: '状态', dataIndex: 'status' },
          { title: '领用人', dataIndex: 'assigneeName' },
          { title: '存放地点', dataIndex: 'location' },
        ]}
      />
    </Space>
  );
}

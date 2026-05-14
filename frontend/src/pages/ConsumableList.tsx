import { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, message, Popconfirm, Select } from 'antd';
import { useNavigate } from 'react-router-dom';
import { consumableApi } from '../api/consumable';
import { consumableCategoryLabels, consumableCategoryColors } from '../types';

export default function ConsumableList() {
  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [category, setCategory] = useState<string | undefined>();
  const navigate = useNavigate();

  const load = async () => {
    setLoading(true);
    try {
      const res = await consumableApi.list(category);
      setData(Array.isArray(res) ? res : []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [category]);

  const handleDelete = async (id: number) => {
    await consumableApi.delete(id);
    message.success('删除成功');
    load();
  };

  const columns = [
    { title: '名称', dataIndex: 'name' },
    {
      title: '分类',
      dataIndex: 'category',
      render: (cat: string) => (
        <Tag color={consumableCategoryColors[cat] || 'default'}>
          {consumableCategoryLabels[cat] || cat}
        </Tag>
      ),
    },
    { title: '规格', dataIndex: 'spec' },
    { title: '单位', dataIndex: 'unit' },
    {
      title: '当前库存',
      dataIndex: 'currentStock',
      render: (stock: number, row: any) => (
        <span style={{ color: row.lowStock ? 'red' : undefined, fontWeight: row.lowStock ? 600 : undefined }}>
          {stock}
        </span>
      ),
    },
    { title: '预警阈值', dataIndex: 'threshold' },
    {
      title: '操作',
      render: (_: any, row: any) => (
        <Space>
          <Button size="small" onClick={() => navigate(`/consumables/${row.id}/edit`)}>编辑</Button>
          <Popconfirm title="确认删除？" onConfirm={() => handleDelete(row.id)}>
            <Button size="small" danger>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Space>
          <Select allowClear placeholder="筛选分类" style={{ width: 160 }}
            onChange={v => setCategory(v)}>
            <Select.Option value="OFFICE_SUPPLIES">办公用品</Select.Option>
            <Select.Option value="ELECTRONIC_PARTS">电子配件</Select.Option>
            <Select.Option value="PRODUCTION_CONSUMABLES">生产耗材</Select.Option>
          </Select>
        </Space>
        <Button type="primary" onClick={() => navigate('/consumables/new')}>新增易耗品</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading} />
    </div>
  );
}
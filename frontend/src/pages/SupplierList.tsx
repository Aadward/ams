import { useEffect, useState } from 'react';
import { Table, Button, Space, message, Popconfirm } from 'antd';
import { useNavigate } from 'react-router-dom';
import { supplierApi } from '../api/supplier';

export default function SupplierList() {
  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const load = async () => {
    setLoading(true);
    try {
      const res = await supplierApi.list();
      setData(Array.isArray(res.data?.content) ? res.data.content : []);
    } catch {
      message.error('加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleDelete = async (id: number) => {
    try {
      await supplierApi.delete(id);
      message.success('删除成功');
      load();
    } catch {
      message.error('删除失败');
    }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id' },
    { title: '供应商名称', dataIndex: 'name' },
    { title: '联系人', dataIndex: 'contactPerson' },
    { title: '电话', dataIndex: 'phone' },
    { title: '邮箱', dataIndex: 'email' },
    {
      title: '操作',
      render: (_: any, row: any) => (
        <Space>
          <Button size="small" onClick={() => navigate(`/suppliers/${row.id}`)}>详情</Button>
          <Button size="small" onClick={() => navigate(`/suppliers/${row.id}/edit`)}>编辑</Button>
          <Popconfirm title="确认删除？" onConfirm={() => handleDelete(row.id)}>
            <Button size="small" danger>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
        <Button type="primary" onClick={() => navigate('/suppliers/new')}>新增供应商</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading} />
    </div>
  );
}

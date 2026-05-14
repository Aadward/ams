import { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, message, Popconfirm, Modal, Form, Input, Select, DatePicker } from 'antd';
import { useNavigate } from 'react-router-dom';
import { inventoryPlanApi } from '../api/inventory';

const statusColors: Record<string, string> = {
  PENDING: 'default',
  IN_PROGRESS: 'processing',
  COMPLETED: 'success',
};
const statusLabels: Record<string, string> = {
  PENDING: '待执行',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
};

export default function InventoryPlanList() {
  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const load = async () => {
    setLoading(true);
    try {
      const res = await inventoryPlanApi.list();
      setData(Array.isArray(res) ? res : []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async (values: any) => {
    const payload = {
      name: values.name,
      scopeType: values.scopeType,
      departmentIds: values.departmentIds,
      categoryIds: values.categoryIds,
      planDate: values.planDate.format('YYYY-MM-DD'),
      assigneeIds: values.assigneeIds,
    };
    const res = await inventoryPlanApi.create(payload);
    if (res.id) {
      message.success('创建成功');
      setModalOpen(false);
      form.resetFields();
      load();
    } else {
      message.error(res.error || '创建失败');
    }
  };

  const handleDelete = async (id: number) => {
    await inventoryPlanApi.delete(id);
    message.success('删除成功');
    load();
  };

  const handleStart = async (id: number) => {
    await inventoryPlanApi.start(id);
    message.success('盘点已开始');
    load();
  };

  const handleComplete = async (id: number) => {
    await inventoryPlanApi.complete(id);
    message.success('盘点已完成');
    load();
  };

  const columns = [
    { title: '计划名称', dataIndex: 'name' },
    { title: '范围类型', dataIndex: 'scopeType', render: (t: string) => t === 'DEPARTMENT' ? '按部门' : '按分类' },
    { title: '计划日期', dataIndex: 'planDate' },
    {
      title: '状态',
      dataIndex: 'status',
      render: (s: string) => <Tag color={statusColors[s]}>{statusLabels[s] || s}</Tag>,
    },
    { title: '创建人', dataIndex: 'creatorName' },
    {
      title: '任务进度',
      render: (_: any, row: any) => (
        <span>{row.checkedTasks}/{row.totalTasks}</span>
      ),
    },
    {
      title: '操作',
      render: (_: any, row: any) => (
        <Space>
          {row.status === 'PENDING' && (
            <Button size="small" onClick={() => handleStart(row.id)}>开始盘点</Button>
          )}
          {row.status === 'IN_PROGRESS' && (
            <Button size="small" onClick={() => handleComplete(row.id)}>完成盘点</Button>
          )}
          <Button size="small" onClick={() => navigate(`/inventory-report/${row.id}`)}>查看报告</Button>
          <Popconfirm title="确认删除？" onConfirm={() => handleDelete(row.id)}>
            <Button size="small" danger>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" onClick={() => setModalOpen(true)}>新建盘点计划</Button>
      </div>

      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} />

      <Modal title="新建盘点计划" open={modalOpen} onCancel={() => setModalOpen(false)} footer={null} width={600}>
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="name" label="计划名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="scopeType" label="范围类型" rules={[{ required: true }]}>
            <Select placeholder="选择范围类型">
              <Select.Option value="DEPARTMENT">按部门</Select.Option>
              <Select.Option value="CATEGORY">按资产分类</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item noStyle shouldUpdate={(prev, curr) => prev.scopeType !== curr.scopeType}>
            {({ getFieldValue }) => (
              getFieldValue('scopeType') === 'DEPARTMENT' ? (
                <Form.Item name="departmentIds" label="选择部门" rules={[{ required: true }]}>
                  <Select mode="multiple" placeholder="选择部门">
                    <Select.Option value={1}>研发部</Select.Option>
                    <Select.Option value={2}>市场部</Select.Option>
                  </Select>
                </Form.Item>
              ) : (
                <Form.Item name="categoryIds" label="选择资产分类" rules={[{ required: true }]}>
                  <Select mode="multiple" placeholder="选择分类">
                    <Select.Option value={0}>硬件设备</Select.Option>
                    <Select.Option value={1}>网络设备</Select.Option>
                    <Select.Option value={2}>配件耗材</Select.Option>
                    <Select.Option value={3}>软件许可证</Select.Option>
                  </Select>
                </Form.Item>
              )
            )}
          </Form.Item>
          <Form.Item name="planDate" label="计划日期" rules={[{ required: true }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="assigneeIds" label="盘点负责人">
            <Select mode="multiple" placeholder="选择负责人（可选）">
              <Select.Option value={1}>管理员</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">创建</Button>
              <Button onClick={() => setModalOpen(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

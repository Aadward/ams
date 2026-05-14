import { useEffect, useState } from 'react';
import { Table, Button, message, Modal, Form, Input, InputNumber, Select } from 'antd';
import { consumableApi } from '../api/consumable';

export default function ConsumableStockOut() {
  const [records, setRecords] = useState<any[]>([]);
  const [consumables, setConsumables] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const loadRecords = async () => {
    setLoading(true);
    try {
      const res = await consumableApi.listRecords();
      setRecords(Array.isArray(res) ? res.filter((r: any) => r.type === 'OUT').reverse() : []);
    } finally {
      setLoading(false);
    }
  };

  const loadConsumables = async () => {
    const res = await consumableApi.list();
    setConsumables(Array.isArray(res) ? res : []);
  };

  useEffect(() => {
    loadRecords();
    loadConsumables();
  }, []);

  const handleStockOut = async (values: any) => {
    await consumableApi.stockOut(values);
    message.success('出库成功');
    setModalOpen(false);
    form.resetFields();
    loadRecords();
  };

  const columns = [
    { title: '易耗品', dataIndex: 'consumableName' },
    { title: '数量', dataIndex: 'quantity' },
    { title: '领用人', dataIndex: 'relatedUserName' },
    { title: '备注', dataIndex: 'remark' },
    { title: '时间', dataIndex: 'createdAt', render: (v: string) => v?.substring(0, 19) },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
        <Button type="primary" onClick={() => setModalOpen(true)}>新增出库</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={records} loading={loading} />
      <Modal title="出库" open={modalOpen} onCancel={() => setModalOpen(false)} footer={null}>
        <Form form={form} layout="vertical" onFinish={handleStockOut}>
          <Form.Item name="consumableId" label="易耗品" rules={[{ required: true }]}>
            <Select placeholder="选择易耗品">
              {consumables.map(c => <Select.Option key={c.id} value={c.id}>{c.name}</Select.Option>)}
            </Select>
          </Form.Item>
          <Form.Item name="quantity" label="数量" rules={[{ required: true, min: 1 }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea />
          </Form.Item>
          <Button type="primary" htmlType="submit">确认出库</Button>
        </Form>
      </Modal>
    </div>
  );
}
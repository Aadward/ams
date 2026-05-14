import { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, message, Modal, Input } from 'antd';
import { inventoryTaskApi } from '../api/inventory';

const statusColors: Record<string, string> = {
  PENDING: 'warning',
  CHECKED: 'success',
};
const statusLabels: Record<string, string> = {
  PENDING: '待盘点',
  CHECKED: '已盘点',
};

export default function InventoryTaskList() {
  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [checkModalOpen, setCheckModalOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState<any>(null);
  const [remark, setRemark] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const res = await inventoryTaskApi.myTasks();
      setData(Array.isArray(res) ? res : []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleCheck = async () => {
    if (!selectedTask) return;
    const res = await inventoryTaskApi.check(selectedTask.id, remark);
    if (res.id) {
      message.success('盘点成功');
      setCheckModalOpen(false);
      setRemark('');
      setSelectedTask(null);
      load();
    } else {
      message.error(res.error || '盘点失败');
    }
  };

  const handleUncheck = async (id: number) => {
    await inventoryTaskApi.uncheck(id);
    message.success('已取消盘点');
    load();
  };

  const handleScan = async () => {
    // Simple QR code scan simulation - prompt for asset code
    const code = prompt('请扫描资产二维码或输入资产编码：');
    if (!code) return;

    // Find task by asset code prefix ASSET-
    const assetCode = code.replace('ASSET-', '');
    const task = data.find(t => t.assetCode === assetCode);
    if (task) {
      setSelectedTask(task);
      setCheckModalOpen(true);
    } else {
      message.warning('该资产不在您的盘点清单中');
    }
  };

  const columns = [
    { title: '资产编码', dataIndex: 'assetCode' },
    { title: '资产名称', dataIndex: 'assetName' },
    { title: '分类', dataIndex: 'category' },
    { title: '盘点计划', dataIndex: 'planName' },
    {
      title: '状态',
      dataIndex: 'status',
      render: (s: string) => <Tag color={statusColors[s]}>{statusLabels[s] || s}</Tag>,
    },
    {
      title: '操作',
      render: (_: any, row: any) => (
        <Space>
          {row.status === 'PENDING' && (
            <Button size="small" type="primary" onClick={() => { setSelectedTask(row); setCheckModalOpen(true); }}>
              确认盘点
            </Button>
          )}
          {row.status === 'CHECKED' && (
            <Button size="small" onClick={() => handleUncheck(row.id)}>取消</Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Space>
          <Button type="primary" onClick={handleScan}>扫码盘点</Button>
          <Button onClick={() => load()}>刷新</Button>
        </Space>
      </div>

      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} />

      <Modal title="确认盘点" open={checkModalOpen} onCancel={() => { setCheckModalOpen(false); setRemark(''); }} onOk={handleCheck}>
        {selectedTask && (
          <div>
            <p><strong>资产编码：</strong>{selectedTask.assetCode}</p>
            <p><strong>资产名称：</strong>{selectedTask.assetName}</p>
            <Input.TextArea
              rows={3}
              placeholder="备注（可选）"
              value={remark}
              onChange={e => setRemark(e.target.value)}
            />
          </div>
        )}
      </Modal>
    </div>
  );
}

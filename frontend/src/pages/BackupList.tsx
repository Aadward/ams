import { Card, Table, Button, Space, message } from 'antd';
import { useEffect, useState } from 'react';
import { backupApi, BackupFile } from '../api/backup';

export default function BackupList() {
  const [backups, setBackups] = useState<BackupFile[]>([]);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);

  const fetchBackups = async () => {
    setLoading(true);
    try {
      const res = await backupApi.list();
      if (res.data.success) {
        setBackups(res.data.backups);
      } else {
        message.error('获取备份列表失败');
      }
    } catch {
      message.error('获取备份列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBackups();
  }, []);

  const handleCreate = async () => {
    setCreating(true);
    try {
      const res = await backupApi.create();
      if (res.data.success) {
        message.success('备份创建成功: ' + res.data.file);
        fetchBackups();
      } else {
        message.error('备份创建失败: ' + res.data.message);
      }
    } catch {
      message.error('备份创建失败');
    } finally {
      setCreating(false);
    }
  };

  const columns = [
    { title: '备份文件名', dataIndex: 'name', key: 'name' as const },
    { title: '文件大小', dataIndex: 'sizeFormatted', key: 'sizeFormatted' as const },
    { title: '最后修改时间', dataIndex: 'lastModified', key: 'lastModified' as const },
  ];

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Space>
        <Button type="primary" onClick={handleCreate} loading={creating}>
          创建备份
        </Button>
        <Button onClick={fetchBackups}>刷新</Button>
      </Space>
      <Card loading={loading}>
        <Table
          dataSource={backups}
          rowKey="name"
          pagination={false}
          columns={columns}
          locale={{ emptyText: '暂无备份数据' }}
        />
      </Card>
    </Space>
  );
}

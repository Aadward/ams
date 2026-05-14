import { useEffect, useState } from 'react';
import { Table, Button, Space, Badge, Tag, Card, Statistic, Row, Col } from 'antd';
import { BellOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { notificationApi, Notification } from '../api/notification';

export default function NotificationList() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [total, setTotal] = useState(0);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);

  const fetchUnreadCount = async () => {
    try {
      const res = await notificationApi.getUnreadCount(1);
      setUnreadCount(res.data.count);
    } catch {}
  };

  const fetchNotifications = async (pageNum = 0) => {
    setLoading(true);
    try {
      const res = await notificationApi.list(1, pageNum, 20);
      setNotifications(res.data.content);
      setTotal(res.data.totalElements);
      setPage(pageNum);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUnreadCount();
    fetchNotifications();
  }, []);

  const handleMarkAsRead = async (id: number) => {
    await notificationApi.markAsRead(id);
    fetchNotifications(page);
    fetchUnreadCount();
  };

  const handleMarkAllAsRead = async () => {
    await notificationApi.markAllAsRead(1);
    fetchNotifications(page);
    fetchUnreadCount();
  };

  const typeBadge = (type: string) => {
    const colors: Record<string, string> = {
      ASSET_APPROVAL: 'blue',
      ASSET_ASSIGNED: 'green',
      ASSET_RETURNED: 'orange',
      MAINTENANCE_DUE: 'red',
      SYSTEM: 'purple',
    };
    const labels: Record<string, string> = {
      ASSET_APPROVAL: '审批',
      ASSET_ASSIGNED: '已领用',
      ASSET_RETURNED: '已归还',
      MAINTENANCE_DUE: '维保',
      SYSTEM: '系统',
    };
    return <Tag color={colors[type] || 'default'}>{labels[type] || type}</Tag>;
  };

  const readBadge = (isRead: boolean) =>
    isRead ? <Badge status="default" text="已读" /> : <Badge status="processing" text="未读" />;

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      <Row gutter={16}>
        <Col span={8}>
          <Card>
            <Statistic
              title="未读通知"
              value={unreadCount}
              prefix={<BellOutlined />}
              valueStyle={{ color: unreadCount > 0 ? '#cf1322' : '#3f8600' }}
            />
          </Card>
        </Col>
      </Row>

      <Space>
        <Button icon={<CheckCircleOutlined />} onClick={handleMarkAllAsRead} disabled={unreadCount === 0}>
          全部标为已读
        </Button>
      </Space>

      <Table
        loading={loading}
        dataSource={notifications}
        rowKey="id"
        pagination={{
          total,
          defaultPageSize: 20,
          onChange: (p) => fetchNotifications(p - 1),
        }}
        columns={[
          { title: '标题', dataIndex: 'title', width: 200 },
          { title: '内容', dataIndex: 'message', ellipsis: true },
          { title: '类型', dataIndex: 'type', width: 100, render: typeBadge },
          { title: '状态', dataIndex: 'isRead', width: 100, render: (v) => readBadge(v) },
          { title: '时间', dataIndex: 'createdAt', width: 180 },
          {
            title: '操作',
            width: 120,
            render: (_: unknown, record: Notification) =>
              !record.isRead && (
                <Button size="small" onClick={() => handleMarkAsRead(record.id)}>
                  标为已读
                </Button>
              ),
          },
        ]}
      />
    </Space>
  );
}

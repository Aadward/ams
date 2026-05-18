import { useEffect, useRef, useState } from 'react';
import { Badge, Popover, List, Button, Tag, Space } from 'antd';
import { BellOutlined } from '@ant-design/icons';
import { Client, IFrame } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { notificationApi, Notification } from '../api/notification';

interface NotificationBellProps {
  userId?: number;
}

export default function NotificationBell({ userId = 1 }: NotificationBellProps) {
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(false);
  const stompClientRef = useRef<Client | null>(null);
  const reconnectTimerRef = useRef<ReturnType<typeof setTimeout>>();

  const connectWebSocket = () => {
    const existing = stompClientRef.current;
    if (existing && existing.connected) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${window.location.protocol}//${window.location.host}/ws`),
      connectHeaders: {},
      reconnectDelay: 0,
    });

    client.onConnect = () => {
      console.log('[STOMP] Connected');
      client.subscribe(`/topic/notifications/${userId}`, (message: { body: string }) => {
        try {
          const notification: Notification = JSON.parse(message.body);
          setNotifications(prev => [notification, ...prev.slice(0, 19)]);
          setUnreadCount(prev => prev + 1);
        } catch {
          console.warn('[STOMP] Failed to parse notification message');
        }
      });
    };

    client.onStompError = (frame: IFrame) => {
      console.error('[STOMP] Error:', frame.headers?.message);
      client.deactivate();
    };

    client.onWebSocketClose = () => {
      console.log('[STOMP] Disconnected, reconnecting in 5s...');
      reconnectTimerRef.current = setTimeout(connectWebSocket, 5000);
    };

    client.activate();
    stompClientRef.current = client;
  };

  useEffect(() => {
    fetchUnreadCount();
    fetchNotifications();
    connectWebSocket();

    return () => {
      if (reconnectTimerRef.current) clearTimeout(reconnectTimerRef.current);
      stompClientRef.current?.deactivate();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  const fetchUnreadCount = async () => {
    try {
      const res = await notificationApi.getUnreadCount(userId);
      setUnreadCount(res.data.count);
    } catch { /* ignore */ }
  };

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const res = await notificationApi.list(userId, 0, 20);
      setNotifications(res.data.content);
    } catch { /* ignore */ } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (id: number) => {
    await notificationApi.markAsRead(id);
    setNotifications(prev =>
      prev.map(n => n.id === id ? { ...n, isRead: true } : n)
    );
    setUnreadCount(prev => Math.max(0, prev - 1));
  };

  const handleMarkAllAsRead = async () => {
    await notificationApi.markAllAsRead(userId);
    setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
    setUnreadCount(0);
  };

  const typeLabels: Record<string, string> = {
    ASSET_EXPIRING_WARRANTY: '维保到期',
    ASSET_EXPIRING_INSURANCE: '保险到期提醒',
    APPROVAL_REQUIRED: '待审批',
    APPROVAL_APPROVED: '已批准',
    APPROVAL_REJECTED: '已拒绝',
    ASSET_ASSIGNED: '已领用',
    ASSET_RETURNED: '已归还',
    MAINTENANCE_DUE: '维保',
    REPAIR_SUBMITTED: '维修提交',
    REPAIR_COMPLETED: '维修完成',
    SYSTEM: '系统',
    BORROW_APPROVED: '借用已批准',
    BORROW_REJECTED: '借用已拒绝',
    BORROW_OVERDUE: '借用已超期',
    BORROW_RETURN_REMINDER: '借用待归还',
    TRANSFER_REQUIRED: '调拨待审批',
    TRANSFER_APPROVED: '调拨已批准',
    TRANSFER_REJECTED: '调拨已拒绝',
    PROCUREMENT_REQUIRED: '采购待审批',
    PROCUREMENT_APPROVED: '采购已批准',
    PROCUREMENT_REJECTED: '采购已拒绝',
  };

  const typeColors: Record<string, string> = {
    ASSET_EXPIRING_WARRANTY: 'orange',
    ASSET_EXPIRING_INSURANCE: 'orange',
    APPROVAL_REQUIRED: 'blue',
    APPROVAL_APPROVED: 'green',
    APPROVAL_REJECTED: 'red',
    ASSET_ASSIGNED: 'green',
    ASSET_RETURNED: 'orange',
    MAINTENANCE_DUE: 'red',
    REPAIR_SUBMITTED: 'orange',
    REPAIR_COMPLETED: 'green',
    SYSTEM: 'purple',
    BORROW_APPROVED: 'success',
    BORROW_REJECTED: 'error',
    BORROW_OVERDUE: 'error',
    BORROW_RETURN_REMINDER: 'warning',
    TRANSFER_REQUIRED: 'blue',
    TRANSFER_APPROVED: 'green',
    TRANSFER_REJECTED: 'red',
    PROCUREMENT_REQUIRED: 'blue',
    PROCUREMENT_APPROVED: 'green',
    PROCUREMENT_REJECTED: 'red',
  };

  const content = (
    <div style={{ width: 360 }}>
      <Space style={{ marginBottom: 8, width: '100%', justifyContent: 'space-between' }}>
        <span style={{ fontWeight: 600 }}>通知中心</span>
        <Button size="small" onClick={handleMarkAllAsRead} disabled={unreadCount === 0}>
          全部已读
        </Button>
      </Space>
      <List
        loading={loading}
        dataSource={notifications}
        locale={{ emptyText: '暂无通知' }}
        size="small"
        style={{ maxHeight: 400, overflowY: 'auto' }}
        renderItem={(item) => (
          <List.Item
            key={item.id}
            style={{
              opacity: item.isRead ? 0.6 : 1,
              padding: '8px 0',
              cursor: 'pointer',
            }}
            onClick={() => !item.isRead && handleMarkAsRead(item.id)}
          >
            <List.Item.Meta
              title={
                <Space>
                  <Tag color={typeColors[item.type] || 'default'} style={{ marginRight: 4 }}>
                    {typeLabels[item.type] || item.type}
                  </Tag>
                  {!item.isRead && <Badge status="processing" />}
                </Space>
              }
              description={
                <>
                  <div style={{ fontSize: 13 }}>{item.title}</div>
                  <div style={{ fontSize: 12, color: '#888' }}>{item.message}</div>
                  <div style={{ fontSize: 11, color: '#aaa' }}>{item.createdAt}</div>
                </>
              }
            />
          </List.Item>
        )}
      />
    </div>
  );

  return (
    <Popover content={content} trigger="click" placement="bottomRight">
      <Badge count={unreadCount} size="small" overflowCount={99}>
        <BellOutlined style={{ fontSize: 18, cursor: 'pointer', color: '#fff' }} />
      </Badge>
    </Popover>
  );
}

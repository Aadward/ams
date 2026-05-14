import { useEffect, useRef, useState } from 'react';
import { Badge, Popover, List, Button, Tag, Space } from 'antd';
import { BellOutlined } from '@ant-design/icons';
import { notificationApi, Notification } from '../api/notification';

interface NotificationBellProps {
  userId?: number;
}

export default function NotificationBell({ userId = 1 }: NotificationBellProps) {
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(false);
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimerRef = useRef<ReturnType<typeof setTimeout>>();

  const connectWebSocket = () => {
    if (wsRef.current?.readyState === WebSocket.OPEN) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws`;

    try {
      const sock = new WebSocket(wsUrl);
      sock.binaryType = 'arraybuffer';

      sock.onopen = () => {
        console.log('[WS] Connected to', wsUrl);
        // Subscribe to user notifications via STOMP over SockJS
        if (sock.readyState === WebSocket.OPEN) {
          sock.send(JSON.stringify({ type: 'CONNECT' }));
        }
      };

      sock.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          if (data.command === 'MESSAGE' && data.headers?.destination === `/topic/notifications/${userId}`) {
            const notification: Notification = typeof data.body === 'string' ? JSON.parse(data.body) : data.body;
            setNotifications(prev => [notification, ...prev.slice(0, 19)]);
            setUnreadCount(prev => prev + 1);
          }
        } catch {
          // Ignore non-JSON or non-STOMP messages (e.g., SockJS heartbeat)
        }
      };

      sock.onclose = () => {
        console.log('[WS] Disconnected, reconnecting in 5s...');
        reconnectTimerRef.current = setTimeout(connectWebSocket, 5000);
      };

      sock.onerror = () => {
        sock.close();
      };

      wsRef.current = sock;
    } catch (e) {
      console.warn('[WS] WebSocket connection failed, will retry:', e);
      reconnectTimerRef.current = setTimeout(connectWebSocket, 5000);
    }
  };

  useEffect(() => {
    fetchUnreadCount();
    fetchNotifications();
    connectWebSocket();

    return () => {
      if (reconnectTimerRef.current) clearTimeout(reconnectTimerRef.current);
      wsRef.current?.close();
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
    ASSET_APPROVAL: '审批',
    ASSET_ASSIGNED: '已领用',
    ASSET_RETURNED: '已归还',
    MAINTENANCE_DUE: '维保',
    SYSTEM: '系统',
  };

  const typeColors: Record<string, string> = {
    ASSET_APPROVAL: 'blue',
    ASSET_ASSIGNED: 'green',
    ASSET_RETURNED: 'orange',
    MAINTENANCE_DUE: 'red',
    SYSTEM: 'purple',
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

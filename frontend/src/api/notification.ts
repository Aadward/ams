import http from './http';

export interface Notification {
  id: number;
  userId: number;
  title: string;
  message: string;
  type: string;
  isRead: boolean;
  createdAt: string;
  readAt?: string;
}

export const notificationApi = {
  list: (userId: number, page = 0, size = 20) =>
    http.get<{ content: Notification[]; totalElements: number }>(`/notifications`, {
      params: { userId, page, size },
    }),

  getUnreadCount: (userId: number) =>
    http.get<{ count: number }>(`/notifications/unread-count`, { params: { userId } }),

  markAsRead: (id: number) =>
    http.patch(`/notifications/${id}/read`),

  markAllAsRead: (userId: number) =>
    http.patch(`/notifications/read-all`, null, { params: { userId } }),
};

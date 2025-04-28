export interface NotificationDTO {
  id: number;
  title: string;
  content: string;
  senderId: number;
  senderName: string;
  createTime: string;
  recipientIds?: number[];
} 
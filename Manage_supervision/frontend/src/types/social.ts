/**
 * 帖子创建请求
 */
export interface PostCreateRequest {
  /** 帖子内容 */
  content: string;
  /** 图片URL列表，最多6张 */
  imageUrls?: string[];
  /** 位置信息 */
  location?: string;
  /** 可见范围：1-仅好友可见（固定值） */
  visibility?: number;
  /** 关联的跑步记录ID */
  runningRecordId?: number | null;
}

/**
 * 评论创建请求
 */
export interface CommentCreateRequest {
  /** 帖子ID */
  postId: number;
  /** 评论内容 */
  content: string;
  /** 回复的评论ID，如果是直接评论帖子则为null */
  replyToId?: number;
}

/**
 * 朋友圈帖子中的跑步记录
 */
export interface RunningRecord {
  /** 记录ID */
  id: number;
  /** 跑步距离 */
  distance: number | string;
  /** 跑步时长 */
  duration: number | string;
  /** 配速 */
  pace: string;
  /** 记录日期，可能是字符串或LocalDate对象 */
  recordDate: string | any;
  /** 创建时间 */
  createTime: string;
}

/**
 * 帖子响应
 */
export interface PostResponse {
  /** 帖子ID */
  id: number;
  /** 用户ID */
  userId: number;
  /** 用户名 */
  username: string;
  /** 用户头像 */
  avatar: string;
  /** 帖子内容 */
  content: string;
  /** 图片URL列表 */
  imageUrls: string[];
  /** 位置信息 */
  location?: string;
  /** 点赞数 */
  likeCount: number;
  /** 评论数 */
  commentCount: number;
  /** 当前用户是否点赞 */
  liked: boolean;
  /** 可见范围：0-全部可见，1-仅好友可见 */
  visibility: number;
  /** 关联的跑步记录 */
  runningRecord?: RunningRecord;
  /** 创建时间 */
  createTime: string;
  /** 更新时间 */
  updateTime: string;
}

/**
 * 评论响应
 */
export interface CommentResponse {
  /** 评论ID */
  id: number;
  /** 帖子ID */
  postId: number;
  /** 用户ID */
  userId: number;
  /** 用户名 */
  username: string;
  /** 用户头像 */
  avatar: string;
  /** 评论内容 */
  content: string;
  /** 回复的评论ID */
  replyToId: number | null;
  /** 回复的用户名 */
  replyToUsername: string | null;
  /** 创建时间 */
  createTime: string;
  /** 更新时间 */
  updateTime: string;
} 
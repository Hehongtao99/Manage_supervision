export interface UserProfile {
  id: number;
  username: string;
  realName: string;
  nickname: string;
  avatar: string;
  bio: string;
  email: string;
  phone: string;
  roles: string[];
  createTime: string;
  status: 'active' | 'inactive';
  userNumber: string;
  collegeId?: number;
  majorId?: number;
  classId?: number;
  collegeName?: string;
  majorName?: string;
  className?: string;
}

export interface Role {
  id: number;
  name: string;
  description: string;
  permissions: string[];
  createTime: string;
}

export interface UserInfo {
  id: number;
  username: string;
  realName: string;
  nickname: string;
  avatar: string;
  bio: string;
  email: string;
  phone: string;
  roles: string[];
  createTime: string;
  status: 'active' | 'inactive';
  userNumber: string;
  collegeId?: number;
  majorId?: number;
  classId?: number;
  collegeName?: string;
  majorName?: string;
  className?: string;
}

export interface UpdateProfileRequest {
  realName?: string;
  nickname?: string;
  bio?: string;
  email?: string;
  phone?: string;
}

export interface UpdatePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface CreateUserRequest {
  username: string;
  password: string;
  realName: string;
  nickname: string;
  email: string;
  phone: string;
  roles: string[];
  collegeId?: number;
  majorId?: number;
  classId?: number;
}

export interface UpdateUserRequest {
  realName?: string;
  nickname?: string;
  email?: string;
  phone?: string;
  roles?: string[];
  status?: 'active' | 'inactive';
  collegeId?: number;
  majorId?: number;
  classId?: number;
}

export interface CreateRoleRequest {
  name: string;
  description: string;
  permissions: string[];
}

export interface UpdateRoleRequest {
  name?: string;
  description?: string;
  permissions?: string[];
} 
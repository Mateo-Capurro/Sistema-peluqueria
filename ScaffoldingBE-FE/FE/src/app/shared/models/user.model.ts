export interface UserResponse {
  id: number;
  username: string;
  name: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
}

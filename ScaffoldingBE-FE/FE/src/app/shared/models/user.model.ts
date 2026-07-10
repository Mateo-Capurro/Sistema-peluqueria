import { Role } from './auth.model';

export interface UserResponse {
  id: number;
  username: string;
  name: string;
  role: Role;
  createdAt: string;
}

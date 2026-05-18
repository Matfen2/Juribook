export type Role = 'CLIENT' | 'LAWYER' | 'ADMIN';

export interface CurrentUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: Role[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterClientRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface RegisterLawyerRequest extends RegisterClientRequest {
  barNumber: string;
  specialtyCode: string;
  city: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  fields?: Record<string, string>;
}
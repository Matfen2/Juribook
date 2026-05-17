import { apiClient } from './client';
import type {
  CurrentUser,
  LoginRequest,
  RegisterClientRequest,
  RegisterLawyerRequest,
} from '../types/auth';

export async function login(data: LoginRequest): Promise<CurrentUser> {
  const response = await apiClient.post<CurrentUser>('/auth/login', data);
  return response.data;
}

export async function registerClient(data: RegisterClientRequest): Promise<CurrentUser> {
  const response = await apiClient.post<CurrentUser>('/auth/register', data);
  return response.data;
}

export async function registerLawyer(data: RegisterLawyerRequest): Promise<CurrentUser> {
  const response = await apiClient.post<CurrentUser>('/auth/register/lawyer', data);
  return response.data;
}

export async function fetchCurrentUser(): Promise<CurrentUser> {
  const response = await apiClient.get<CurrentUser>('/auth/me');
  return response.data;
}
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true, // CRUCIAL : envoie les cookies HttpOnly à chaque requête
  headers: {
    'Content-Type': 'application/json',
  },
});
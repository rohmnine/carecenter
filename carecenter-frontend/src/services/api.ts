import axios from 'axios';
import type { AxiosInstance, AxiosResponse } from 'axios';

const apiClient: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

export default {
  getUsers(): Promise<AxiosResponse> {
    return apiClient.get('/users');
  },
  login(credentials: any): Promise<AxiosResponse> {
    return apiClient.post('/login', credentials);
  },
  register(credentials: any): Promise<AxiosResponse> {
    return apiClient.post('/register', credentials);
  },
  getMessages(): Promise<AxiosResponse> {
    return apiClient.get('/messages');
  },
  createMessage(message: any): Promise<AxiosResponse> {
    return apiClient.post('/messages', message);
  }
};
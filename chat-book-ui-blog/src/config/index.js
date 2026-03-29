/**
 * API 配置
 */
export const API_CONFIG = {
    // Use env to control backend address per environment (dev/prod).
    // Empty string means same-origin (recommended for production behind Nginx reverse proxy).
    baseURL: (import.meta.env.VITE_APP_BASE_URL ?? import.meta.env.VITE_API_BASE_URL ?? ''),
    timeout: 8000,
};

export const AUTH_CONFIG = {
    baseURL: (import.meta.env.VITE_AUTH_BASE_URL ?? 'http://localhost:8081'),
};

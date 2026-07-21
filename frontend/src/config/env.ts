/**
 * Runtime configuration sourced from Vite env vars. In dev, requests go to
 * the relative `/api/v1` path which the Vite dev-server proxies to the Spring
 * Boot backend on :8080 (see vite.config.ts) — so no CORS is involved locally.
 * In production, set VITE_API_BASE_URL to the deployed API origin.
 */
export const env = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  appName: 'OGM HRMS',
} as const

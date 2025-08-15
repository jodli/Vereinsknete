// Shared API URL configuration for tests
// Use same API URL logic as main application for consistency
export const API_URL = process.env.NODE_ENV === 'production' 
  ? '/api'  // Relative URL for ingress proxy
  : (process.env.REACT_APP_API_URL || 'http://localhost:8080/api');
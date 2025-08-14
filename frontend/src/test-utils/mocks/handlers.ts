import { rest } from 'msw';
import { mockClients, mockSessions, mockInvoices, mockUserProfile, mockDashboardMetrics } from './mockData';
import type { UserProfile, Client, SessionWithDuration } from '../../types';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const handlers = [
  // User Profile endpoints
  rest.get(`${API_URL}/profile`, (req, res, ctx) => {
    return res(ctx.json(mockUserProfile));
  }),

  rest.put(`${API_URL}/profile`, (req, res, ctx) => {
    const body = (req.body ?? {}) as Partial<UserProfile>;
    return res(ctx.json({ ...mockUserProfile, ...body }));
  }),

  // Client endpoints
  rest.get(`${API_URL}/clients`, (req, res, ctx) => {
    return res(ctx.json(mockClients));
  }),

  rest.get(`${API_URL}/clients/:id`, (req, res, ctx) => {
    const { id } = req.params;
    const client = mockClients.find(c => c.id === parseInt(id as string));
    if (!client) {
      return res(ctx.status(404), ctx.json({ error: 'Client not found' }));
    }
    return res(ctx.json(client));
  }),

  rest.post(`${API_URL}/clients`, (req, res, ctx) => {
    const body = (req.body ?? {}) as Partial<Client>;
    const newClient: Client & { created_at: string; updated_at: string } = {
      id: mockClients.length + 1,
      name: 'Unnamed Client',
      address: '',
      contact_person: null,
      default_hourly_rate: 0,
      ...body,
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString(),
    };
    return res(ctx.status(201), ctx.json(newClient));
  }),

  rest.put(`${API_URL}/clients/:id`, (req, res, ctx) => {
    const { id } = req.params;
    const client = mockClients.find(c => c.id === parseInt(id as string));
    if (!client) {
      return res(ctx.status(404), ctx.json({ error: 'Client not found' }));
    }
    const body = (req.body ?? {}) as Partial<Client>;
    const updatedClient = { ...client, ...body, updated_at: new Date().toISOString() };
    return res(ctx.json(updatedClient));
  }),

  rest.delete(`${API_URL}/clients/:id`, (req, res, ctx) => {
    return res(ctx.status(204));
  }),

  // Session endpoints
  rest.get(`${API_URL}/sessions`, (req, res, ctx) => {
    const clientId = req.url.searchParams.get('client_id');
    const startDate = req.url.searchParams.get('start_date');
    const endDate = req.url.searchParams.get('end_date');

    let filteredSessions = [...mockSessions];

    if (clientId) {
      filteredSessions = filteredSessions.filter(s => s.client_id === parseInt(clientId));
    }

    if (startDate) {
      filteredSessions = filteredSessions.filter(s => s.date >= startDate);
    }

    if (endDate) {
      filteredSessions = filteredSessions.filter(s => s.date <= endDate);
    }

    return res(ctx.json(filteredSessions));
  }),

  rest.get(`${API_URL}/sessions/:id`, (req, res, ctx) => {
    const { id } = req.params;
    const session = mockSessions.find(s => s.id === parseInt(id as string));
    if (!session) {
      return res(ctx.status(404), ctx.json({ error: 'Session not found' }));
    }
    return res(ctx.json(session));
  }),

  rest.post(`${API_URL}/sessions`, (req, res, ctx) => {
    const body = (req.body ?? {}) as Partial<SessionWithDuration>;
    const newSession = {
      id: mockSessions.length + 1,
      client_id: 0,
      name: 'Unnamed Session',
      date: new Date().toISOString().slice(0, 10),
      start_time: '00:00',
      end_time: '00:00',
      created_at: new Date().toISOString(),
      client_name: 'Unknown',
      duration_minutes: 0,
      ...body,
    };
    return res(ctx.status(201), ctx.json(newSession));
  }),

  rest.put(`${API_URL}/sessions/:id`, (req, res, ctx) => {
    const { id } = req.params;
    const session = mockSessions.find(s => s.id === parseInt(id as string));
    if (!session) {
      return res(ctx.status(404), ctx.json({ error: 'Session not found' }));
    }
    const body = (req.body ?? {}) as Partial<SessionWithDuration>;
    const updatedSession = { ...session, ...body };
    return res(ctx.json(updatedSession));
  }),

  rest.delete(`${API_URL}/sessions/:id`, (req, res, ctx) => {
    return res(ctx.status(204));
  }),

  // Invoice endpoints
  rest.get(`${API_URL}/invoices`, (req, res, ctx) => {
    return res(ctx.json(mockInvoices));
  }),

  rest.post(`${API_URL}/invoices/generate`, (req, res, ctx) => {
    return res(ctx.json({
      invoice_id: 123,
      invoice_number: 'INV-2024-001',
      pdf_bytes: 'base64-encoded-pdf-data'
    }));
  }),

  rest.get(`${API_URL}/invoices/:id/pdf`, (req, res, ctx) => {
    // Return a mock PDF blob
    const pdfContent = new Uint8Array([37, 80, 68, 70]); // PDF header
    return res(
      ctx.set('Content-Type', 'application/pdf'),
      ctx.body(pdfContent)
    );
  }),

  rest.patch(`${API_URL}/invoices/:id/status`, (req, res, ctx) => {
    return res(ctx.status(200));
  }),

  rest.delete(`${API_URL}/invoices/:id`, (req, res, ctx) => {
    return res(ctx.status(204));
  }),

  // Dashboard endpoints
  rest.get(`${API_URL}/dashboard/metrics`, (req, res, ctx) => {
    return res(ctx.json(mockDashboardMetrics));
  }),

  // Error simulation handlers
  rest.get(`${API_URL}/error/500`, (req, res, ctx) => {
    return res(ctx.status(500), ctx.json({ error: 'Internal server error' }));
  }),

  rest.get(`${API_URL}/error/404`, (req, res, ctx) => {
    return res(ctx.status(404), ctx.json({ error: 'Not found' }));
  }),
];
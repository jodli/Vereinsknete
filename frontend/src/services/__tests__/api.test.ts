import { rest } from 'msw';
import { server } from '../../test-utils/mocks/server';
import {
  getUserProfile,
  updateUserProfile,
  getClients,
  getClient,
  createClient,
  updateClient,
  deleteClient,
  getSessions,
  getSession,
  createSession,
  updateSession,
  deleteSession,
  generateInvoice,
  downloadInvoicePdf,
  getAllInvoices,
  updateInvoiceStatus,
  deleteInvoice,
  getDashboardMetrics,
} from '../api';
import { mockClients, mockSessions, mockInvoices, mockUserProfile, mockDashboardMetrics } from '../../test-utils/mocks/mockData';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Global MSW server is started in setupTests.ts (avoid duplicate setup here)
// We still import rest to override handlers per test using server.use (server is global)

describe('User Profile API', () => {
  describe('getUserProfile', () => {
    it('fetches user profile successfully', async () => {
      const profile = await getUserProfile();
      expect(profile).toEqual(mockUserProfile);
    });

    it('returns null for 404 response', async () => {
      server.use(
        rest.get(`${API_URL}/profile`, (req, res, ctx) => {
          return res(ctx.status(404));
        })
      );

      const profile = await getUserProfile();
      expect(profile).toBeNull();
    });

    it('throws error for other HTTP errors', async () => {
      server.use(
        rest.get(`${API_URL}/profile`, (req, res, ctx) => {
          return res(ctx.status(500), ctx.json({ error: 'Server error' }));
        })
      );

      await expect(getUserProfile()).rejects.toThrow('Failed to get user profile');
    });
  });

  describe('updateUserProfile', () => {
    it('updates user profile successfully', async () => {
      const updateData = { name: 'Updated Name', address: 'New Address' };
      const updatedProfile = await updateUserProfile(updateData);

      expect(updatedProfile).toEqual({ ...mockUserProfile, ...updateData });
    });

    it('throws error on update failure', async () => {
      server.use(
        rest.put(`${API_URL}/profile`, (req, res, ctx) => {
          return res(ctx.status(400), ctx.json({ error: 'Bad request' }));
        })
      );

  await expect(updateUserProfile({ name: 'Test', address: 'Some Address' })).rejects.toThrow('Failed to update user profile');
    });
  });
});

describe('Client API', () => {
  describe('getClients', () => {
    it('fetches all clients successfully', async () => {
      const clients = await getClients();
      expect(clients).toEqual(mockClients);
    });

    it('throws error on fetch failure', async () => {
      server.use(
        rest.get(`${API_URL}/clients`, (req, res, ctx) => {
          return res(ctx.status(500));
        })
      );

      await expect(getClients()).rejects.toThrow('Failed to get clients');
    });
  });

  describe('getClient', () => {
    it('fetches single client successfully', async () => {
      const client = await getClient(1);
      expect(client).toEqual(mockClients[0]);
    });

    it('throws error for non-existent client', async () => {
      await expect(getClient(999)).rejects.toThrow('Failed to get client');
    });
  });

  describe('createClient', () => {
    it('creates client successfully', async () => {
      const newClientData = {
        name: 'New Client',
        address: 'New Address',
        contact_person: 'New Contact',
        default_hourly_rate: 80,
      };

      const createdClient = await createClient(newClientData);
      expect(createdClient).toMatchObject(newClientData);
      expect(createdClient.id).toBeDefined();
    });

    it('throws error on creation failure', async () => {
      server.use(
        rest.post(`${API_URL}/clients`, (req, res, ctx) => {
          return res(ctx.status(400));
        })
      );

      await expect(createClient({
        name: 'Test',
        address: 'Test',
        default_hourly_rate: 50,
      })).rejects.toThrow('Failed to create client');
    });
  });

  describe('updateClient', () => {
    it('updates client successfully', async () => {
      const updateData = { name: 'Updated Client Name' };
      const updatedClient = await updateClient(1, updateData);

      expect(updatedClient).toMatchObject({ ...mockClients[0], ...updateData });
    });

    it('throws error for non-existent client', async () => {
      await expect(updateClient(999, { name: 'Test' })).rejects.toThrow('Failed to update client');
    });
  });

  describe('deleteClient', () => {
    it('deletes client successfully', async () => {
      await expect(deleteClient(1)).resolves.toBeUndefined();
    });

    it('throws error on deletion failure', async () => {
      server.use(
        rest.delete(`${API_URL}/clients/:id`, (req, res, ctx) => {
          return res(ctx.status(500));
        })
      );

      await expect(deleteClient(1)).rejects.toThrow('Failed to delete client');
    });
  });
});

describe('Session API', () => {
  describe('getSessions', () => {
    it('fetches all sessions successfully', async () => {
      const sessions = await getSessions();
      expect(sessions).toEqual(mockSessions);
    });

    it('fetches sessions with client filter', async () => {
      const sessions = await getSessions({ client_id: 1 });
      expect(sessions).toEqual(mockSessions.filter(s => s.client_id === 1));
    });

    it('fetches sessions with date filters', async () => {
      const sessions = await getSessions({
        start_date: '2024-01-15',
        end_date: '2024-01-16',
      });
      expect(sessions).toEqual(mockSessions.filter(s => 
        s.date >= '2024-01-15' && s.date <= '2024-01-16'
      ));
    });

    it('throws error on fetch failure', async () => {
      server.use(
        rest.get(`${API_URL}/sessions`, (req, res, ctx) => {
          return res(ctx.status(500));
        })
      );

      await expect(getSessions()).rejects.toThrow('Failed to get sessions');
    });
  });

  describe('getSession', () => {
    it('fetches single session successfully', async () => {
      const session = await getSession(1);
      expect(session).toEqual(mockSessions[0]);
    });

    it('throws error for non-existent session', async () => {
      await expect(getSession(999)).rejects.toThrow('Failed to get session');
    });
  });

  describe('createSession', () => {
    it('creates session successfully', async () => {
      const newSessionData = {
        client_id: 1,
        name: 'New Session',
        date: '2024-01-20',
        start_time: '09:00',
        end_time: '10:00',
      };

      const createdSession = await createSession(newSessionData);
      expect(createdSession).toMatchObject(newSessionData);
      expect(createdSession.id).toBeDefined();
    });

    it('throws error on creation failure', async () => {
      server.use(
        rest.post(`${API_URL}/sessions`, (req, res, ctx) => {
          return res(ctx.status(400));
        })
      );

      await expect(createSession({
        client_id: 1,
        name: 'Test',
        date: '2024-01-20',
        start_time: '09:00',
        end_time: '10:00',
      })).rejects.toThrow('Failed to create session');
    });
  });

  describe('updateSession', () => {
    it('updates session successfully', async () => {
      const updateData = { name: 'Updated Session' };
      const updatedSession = await updateSession(1, {
        client_id: 1,
        name: 'Updated Session',
        date: '2024-01-15',
        start_time: '09:00',
        end_time: '12:00',
      });

      expect(updatedSession).toMatchObject({ ...mockSessions[0], ...updateData });
    });

    it('throws error for non-existent session', async () => {
      await expect(updateSession(999, {
        client_id: 1,
        name: 'Test',
        date: '2024-01-20',
        start_time: '09:00',
        end_time: '10:00',
      })).rejects.toThrow('Failed to update session');
    });
  });

  describe('deleteSession', () => {
    it('deletes session successfully', async () => {
      await expect(deleteSession(1)).resolves.toBeUndefined();
    });

    it('throws error on deletion failure', async () => {
      server.use(
        rest.delete(`${API_URL}/sessions/:id`, (req, res, ctx) => {
          return res(ctx.status(500));
        })
      );

      await expect(deleteSession(1)).rejects.toThrow('Failed to delete session');
    });
  });
});

describe('Invoice API', () => {
  describe('generateInvoice', () => {
    it('generates invoice successfully', async () => {
      const invoiceRequest = {
        client_id: 1,
        start_date: '2024-01-01',
        end_date: '2024-01-31',
        language: 'en',
      };

      const result = await generateInvoice(invoiceRequest);
      expect(result).toEqual({
        invoice_id: 123,
        invoice_number: 'INV-2024-001',
        pdf_bytes: 'base64-encoded-pdf-data',
      });
    });

    it('throws error on generation failure', async () => {
      server.use(
        rest.post(`${API_URL}/invoices/generate`, (req, res, ctx) => {
          return res(ctx.status(400));
        })
      );

      await expect(generateInvoice({
        client_id: 1,
        start_date: '2024-01-01',
        end_date: '2024-01-31',
      })).rejects.toThrow('Failed to generate invoice');
    });
  });

  describe('downloadInvoicePdf', () => {
    it('downloads PDF successfully', async () => {
      const pdfBlob = await downloadInvoicePdf(1);
      expect(pdfBlob).toBeInstanceOf(Blob);
    });

    it('throws error on download failure', async () => {
      server.use(
        rest.get(`${API_URL}/invoices/:id/pdf`, (req, res, ctx) => {
          return res(ctx.status(404));
        })
      );

      await expect(downloadInvoicePdf(999)).rejects.toThrow('Failed to download invoice PDF');
    });
  });

  describe('getAllInvoices', () => {
    it('fetches all invoices successfully', async () => {
      const invoices = await getAllInvoices();
      expect(invoices).toEqual(mockInvoices);
    });

    it('throws error on fetch failure', async () => {
      server.use(
        rest.get(`${API_URL}/invoices`, (req, res, ctx) => {
          return res(ctx.status(500));
        })
      );

      await expect(getAllInvoices()).rejects.toThrow('Failed to get invoices');
    });
  });

  describe('updateInvoiceStatus', () => {
    it('updates invoice status successfully', async () => {
      await expect(updateInvoiceStatus(1, { status: 'paid' })).resolves.toBeUndefined();
    });

    it('throws error on update failure', async () => {
      server.use(
        rest.patch(`${API_URL}/invoices/:id/status`, (req, res, ctx) => {
          return res(ctx.status(400));
        })
      );

      await expect(updateInvoiceStatus(1, { status: 'paid' })).rejects.toThrow('Failed to update invoice status');
    });
  });

  describe('deleteInvoice', () => {
    it('deletes invoice successfully', async () => {
      await expect(deleteInvoice(1)).resolves.toBeUndefined();
    });

    it('throws error on deletion failure', async () => {
      server.use(
        rest.delete(`${API_URL}/invoices/:id`, (req, res, ctx) => {
          return res(ctx.status(500));
        })
      );

      await expect(deleteInvoice(1)).rejects.toThrow('Failed to delete invoice');
    });
  });
});

describe('Dashboard API', () => {
  describe('getDashboardMetrics', () => {
    it('fetches dashboard metrics successfully', async () => {
      const query = { period: 'month' as const, year: 2024, month: 1 };
      const metrics = await getDashboardMetrics(query);
      expect(metrics).toEqual(mockDashboardMetrics);
    });

    it('fetches metrics without month parameter', async () => {
      const query = { period: 'year' as const, year: 2024 };
      const metrics = await getDashboardMetrics(query);
      expect(metrics).toEqual(mockDashboardMetrics);
    });

    it('throws error on fetch failure', async () => {
      server.use(
        rest.get(`${API_URL}/dashboard/metrics`, (req, res, ctx) => {
          return res(ctx.status(500));
        })
      );

      await expect(getDashboardMetrics({ 
        period: 'month', 
        year: 2024, 
        month: 1 
      })).rejects.toThrow('Failed to get dashboard metrics');
    });
  });
});

describe('API Error Handling', () => {
  it('handles network errors', async () => {
    server.use(
      rest.get(`${API_URL}/clients`, (req, res, ctx) => {
        return res.networkError('Network error');
      })
    );

    await expect(getClients()).rejects.toThrow();
  });

  it('handles JSON parsing errors', async () => {
    server.use(
      rest.get(`${API_URL}/clients`, (req, res, ctx) => {
        return res(ctx.text('Invalid JSON'));
      })
    );

    await expect(getClients()).rejects.toThrow();
  });

  it('logs errors to console', async () => {
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

    server.use(
      rest.get(`${API_URL}/clients`, (req, res, ctx) => {
        return res(ctx.status(500));
      })
    );

    await expect(getClients()).rejects.toThrow();
    expect(consoleSpy).toHaveBeenCalled();

    consoleSpy.mockRestore();
  });
});
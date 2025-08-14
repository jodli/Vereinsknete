import React from 'react';
import { render, screen, fireEvent, waitFor } from '../../test-utils/test-utils';
import { rest } from 'msw';
import { setupServer } from 'msw/node';
import ClientFormPage from '../ClientFormPage';
import { mockClients } from '../../test-utils/mocks/mockData';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Mock react-router-dom
const mockNavigate = jest.fn();
// Provide mutable params object with broader typing so we can switch between create/edit modes
const mockParams: { id: string | undefined } = { id: undefined };

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  useParams: () => mockParams,
}));

// Setup MSW server
const server = setupServer(
  rest.get(`${API_URL}/clients/:id`, (req, res, ctx) => {
    const { id } = req.params;
    const client = mockClients.find(c => c.id === parseInt(id as string));
    if (!client) {
      return res(ctx.status(404), ctx.json({ error: 'Client not found' }));
    }
    return res(ctx.json(client));
  }),
  rest.post(`${API_URL}/clients`, (req, res, ctx) => {
    const newClient = {
      id: mockClients.length + 1,
      ...(req.body as any),
    };
    return res(ctx.status(201), ctx.json(newClient));
  }),
  rest.put(`${API_URL}/clients/:id`, (req, res, ctx) => {
    const { id } = req.params;
    const client = mockClients.find(c => c.id === parseInt(id as string));
    if (!client) {
      return res(ctx.status(404), ctx.json({ error: 'Client not found' }));
    }
    const updatedClient = { ...client, ...(req.body as any) };
    return res(ctx.json(updatedClient));
  })
);

beforeAll(() => server.listen());
afterEach(() => {
  server.resetHandlers();
  mockNavigate.mockClear();
  mockParams.id = undefined;
});
afterAll(() => server.close());

// Using shared test-utils render that provides Language, Toast and Confirm providers

describe('ClientFormPage', () => {
  beforeEach(() => {
    // Force English language for these tests to match expected literals
    localStorage.setItem('preferredLanguage', 'en');
  });
  describe('Create Mode', () => {
    it('renders create client form', () => {
      render(<ClientFormPage />);

      // Title comes from translations.clients.form.title.new => 'New Client'. Use role to avoid duplicate match.
      expect(screen.getByRole('heading', { level: 1, name: /new client/i })).toBeInTheDocument();
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/address/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/hourly rate/i)).toBeInTheDocument();
    });

    it('creates new client successfully', async () => {
      render(<ClientFormPage />);

      fireEvent.change(screen.getByLabelText(/name/i), {
        target: { value: 'New Client' }
      });
      fireEvent.change(screen.getByLabelText(/address/i), {
        target: { value: 'New Address' }
      });
      fireEvent.change(screen.getByLabelText(/hourly rate/i), {
        target: { value: '75' }
      });

      const saveButton = screen.getByRole('button', { name: /save/i });
      fireEvent.click(saveButton);

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/clients');
      });
    });

    it('validates required fields', async () => {
      render(<ClientFormPage />);

      const saveButton = screen.getByRole('button', { name: /save/i });
      fireEvent.click(saveButton);

      await waitFor(() => {
        expect(screen.getByText(/name.*required/i)).toBeInTheDocument();
      });
    });
  });

  describe('Edit Mode', () => {
    beforeEach(() => {
      mockParams.id = '1';
    });

    it('loads existing client data', async () => {
      render(<ClientFormPage />);

      await waitFor(() => {
        expect(screen.getByDisplayValue('Acme Corporation')).toBeInTheDocument();
      });
      expect(screen.getByDisplayValue('456 Business Ave, Business City')).toBeInTheDocument();
      expect(screen.getByDisplayValue('75')).toBeInTheDocument();
    });

    it('updates existing client', async () => {
      render(<ClientFormPage />);

      await waitFor(() => {
        expect(screen.getByDisplayValue('Acme Corporation')).toBeInTheDocument();
      });

      const nameInput = screen.getByLabelText(/name/i);
      fireEvent.change(nameInput, { target: { value: 'Updated Client Name' } });

      const saveButton = screen.getByRole('button', { name: /save/i });
      fireEvent.click(saveButton);

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/clients');
      });
    });

    it('handles client not found', async () => {
      mockParams.id = '999';

      render(<ClientFormPage />);

      await waitFor(() => {
        expect(screen.getByText(/not found/i)).toBeInTheDocument();
      });
    });
  });

  it('handles form cancellation', () => {
    render(<ClientFormPage />);

    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    fireEvent.click(cancelButton);

    expect(mockNavigate).toHaveBeenCalledWith('/clients');
  });

  it('validates hourly rate as positive number', async () => {
    render(<ClientFormPage />);

    fireEvent.change(screen.getByLabelText(/name/i), {
      target: { value: 'Test Client' }
    });
    fireEvent.change(screen.getByLabelText(/hourly rate/i), {
      target: { value: '-10' }
    });

    const saveButton = screen.getByRole('button', { name: /save/i });
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(screen.getByText(/positive/i)).toBeInTheDocument();
    });
  });

  it('handles API errors during save', async () => {
    server.use(
      rest.post(`${API_URL}/clients`, (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ error: 'Server error' }));
      })
    );

    render(<ClientFormPage />);

    fireEvent.change(screen.getByLabelText(/name/i), {
      target: { value: 'Test Client' }
    });
    fireEvent.change(screen.getByLabelText(/address/i), {
      target: { value: 'Test Address' }
    });
    fireEvent.change(screen.getByLabelText(/hourly rate/i), {
      target: { value: '75' }
    });

    const saveButton = screen.getByRole('button', { name: /save/i });
    fireEvent.click(saveButton);

  });
});
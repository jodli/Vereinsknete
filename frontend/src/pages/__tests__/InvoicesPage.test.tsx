import React from 'react';
import { render, screen, fireEvent, waitFor } from '../../test-utils/test-utils';
import { rest } from 'msw';
import { server } from '../../test-utils/mocks/server';
import InvoicesPage from '../InvoicesPage';
// import { mockInvoices } from '../../test-utils/mocks/mockData';

import { API_URL } from '../../test-utils/apiConfig';

describe('InvoicesPage', () => {
  const mockConfirm = jest.fn();
  
  beforeAll(() => {
    Object.defineProperty(window, 'confirm', {
      value: mockConfirm,
      writable: true,
    });    
  });

  beforeEach(() => {
    mockConfirm.mockClear();
    
    // Set default language
    localStorage.setItem('preferredLanguage', 'en');
  });

  it('renders page title', async () => {
    render(<InvoicesPage />);

    // Wait for loading to finish
    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    }, { timeout: 3000 });

    expect(screen.getByText('Rechnungen')).toBeInTheDocument();
  });

  it('displays loading state initially', () => {
    render(<InvoicesPage />);
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it('displays invoices table with data', async () => {
    render(<InvoicesPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check table headers
    expect(screen.getByText('Rechnungsnummer')).toBeInTheDocument();
    expect(screen.getByText('Klient')).toBeInTheDocument();
    expect(screen.getByText('Datum')).toBeInTheDocument();
    expect(screen.getByText('Betrag')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();
    expect(screen.getByText('Aktionen')).toBeInTheDocument();

    // Check invoice data
    expect(screen.getByText('INV-2024-001')).toBeInTheDocument();
    expect(screen.getByText('Acme Corporation')).toBeInTheDocument();
  });

  it('toggles invoice generation form', async () => {
    render(<InvoicesPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Initially form should not be visible
    expect(screen.queryByText('Neue Rechnung erstellen')).not.toBeInTheDocument();

    // Click to show form
    fireEvent.click(screen.getByRole('button', { name: /neue rechnung/i }));
    expect(screen.getByText('Neue Rechnung erstellen')).toBeInTheDocument();

    // Click to hide form
    fireEvent.click(screen.getByRole('button', { name: /abbrechen/i }));
    expect(screen.queryByText('Neue Rechnung erstellen')).not.toBeInTheDocument();
  });

  it('displays empty state when no invoices exist', async () => {
    // Mock empty invoices response
    server.use(
      rest.get(`${API_URL}/invoices`, (req, res, ctx) => {
        return res(ctx.json([]));
      })
    );

    render(<InvoicesPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    expect(screen.getByText('Noch keine Rechnungen vorhanden.')).toBeInTheDocument();
  });

  it('displays error state when API fails', async () => {
    // Mock API failure
    server.use(
      rest.get(`${API_URL}/invoices`, (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ error: 'Server error' }));
      }),
      rest.get(`${API_URL}/clients`, (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ error: 'Server error' }));
      })
    );

    render(<InvoicesPage />);

    await waitFor(() => {
      expect(screen.getByText('Failed to load data')).toBeInTheDocument();
    });
  });

  it('deletes invoice with confirmation', async () => {
    mockConfirm.mockReturnValue(true);

    // Mock delete endpoint
    server.use(
      rest.delete(`${API_URL}/invoices/:id`, (req, res, ctx) => {
        return res(ctx.status(204));
      })
    );

    render(<InvoicesPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Find and click delete button
    const deleteButtons = screen.getAllByTitle('Rechnung löschen');
    fireEvent.click(deleteButtons[0]);

    // Verify confirmation was shown
    expect(mockConfirm).toHaveBeenCalledWith(
      expect.stringContaining('INV-2024-001')
    );

    // Wait for deletion to complete
    await waitFor(() => {
      expect(screen.queryByText('INV-2024-001')).not.toBeInTheDocument();
    });
  });

  it('cancels invoice deletion when not confirmed', async () => {
    mockConfirm.mockReturnValue(false);

    render(<InvoicesPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Find and click delete button
    const deleteButtons = screen.getAllByTitle('Rechnung löschen');
    fireEvent.click(deleteButtons[0]);

    // Verify confirmation was shown but invoice still exists
    expect(mockConfirm).toHaveBeenCalled();
    expect(screen.getByText('INV-2024-001')).toBeInTheDocument();
  });

  it('downloads PDF successfully', async () => {
    // Mock URL.createObjectURL and revokeObjectURL only for this test
    const mockCreateObjectURL = jest.fn(() => 'mock-url');
    const mockRevokeObjectURL = jest.fn();
    const originalCreateObjectURL = window.URL.createObjectURL;
    const originalRevokeObjectURL = window.URL.revokeObjectURL;
    
    window.URL.createObjectURL = mockCreateObjectURL;
    window.URL.revokeObjectURL = mockRevokeObjectURL;

    // Mock PDF download
    server.use(
      rest.get(`${API_URL}/invoices/:id/pdf`, (req, res, ctx) => {
        const pdfContent = new Uint8Array([37, 80, 68, 70]); // PDF header
        return res(
          ctx.set('Content-Type', 'application/pdf'),
          ctx.body(pdfContent)
        );
      })
    );

    render(<InvoicesPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Find and click download button for first invoice
    const downloadButtons = screen.getAllByTitle('PDF herunterladen');
    fireEvent.click(downloadButtons[0]);

    // Verify download was triggered
    await waitFor(() => {
      expect(mockCreateObjectURL).toHaveBeenCalled();
    });

    // Restore original functions
    window.URL.createObjectURL = originalCreateObjectURL;
    window.URL.revokeObjectURL = originalRevokeObjectURL;
  });

  it('formats currency correctly', async () => {
    render(<InvoicesPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check currency formatting (German format)
    expect(screen.getByText('337,50 €')).toBeInTheDocument();
    expect(screen.getByText('297,50 €')).toBeInTheDocument();
    expect(screen.getByText('180,00 €')).toBeInTheDocument();
  });

  it('displays correct status badges', async () => {
    render(<InvoicesPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check status badges
    expect(screen.getByText('Bezahlt')).toBeInTheDocument();
    expect(screen.getByText('Versendet')).toBeInTheDocument();
    expect(screen.getByText('Erstellt')).toBeInTheDocument();

    // Check status badge styling
    const paidBadge = screen.getByText('Bezahlt');
    expect(paidBadge).toHaveClass('bg-green-100', 'text-green-800');
  });
});
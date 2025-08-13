import React from 'react';
import { render, screen, fireEvent, waitFor } from '../../test-utils/test-utils';
import { rest } from 'msw';
import { server } from '../../test-utils/mocks/server';
import DashboardPage from '../DashboardPage';
import { mockDashboardMetrics, mockInvoices } from '../../test-utils/mocks/mockData';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Mock react-router-dom
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  Link: ({ children, to, ...props }: any) => (
    <a href={to} {...props}>{children}</a>
  ),
}));

// Deterministic date (Jan 15 2025) to stabilize month/year assertions
const FIXED_DATE = new Date('2025-01-15T12:00:00Z');
const RealDate = Date;
beforeAll(() => {
  // Minimal mock preserving Date API used in code under test
  // @ts-ignore
  global.Date = class extends RealDate {
    constructor(value?: any) {
      if (value !== undefined) {
        super(value);
      } else {
        super(FIXED_DATE.getTime());
      }
    }
    static now() { return FIXED_DATE.getTime(); }
    static parse(str: string) { return RealDate.parse(str); }
    static UTC(year: number, month?: number, date?: number, hours?: number, minutes?: number, seconds?: number, ms?: number) {
      return RealDate.UTC(year, month ?? 0, date ?? 1, hours ?? 0, minutes ?? 0, seconds ?? 0, ms ?? 0);
    }
  } as DateConstructor;
});
afterAll(() => {
  // @ts-ignore
  global.Date = RealDate;
});

describe('DashboardPage', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
  // Force English language for these tests to match expected literals
  localStorage.setItem('preferredLanguage', 'en');
  });

  it('renders dashboard title and subtitle', async () => {
    render(<DashboardPage />);

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });
  });

  it('displays loading state initially', () => {
    render(<DashboardPage />);

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it('displays dashboard metrics after loading', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check that metrics are displayed
    expect(screen.getByText('€815.00')).toBeInTheDocument(); // total_revenue_period
    expect(screen.getByText('€477.50')).toBeInTheDocument(); // pending_invoices_amount
    expect(screen.getByText('3')).toBeInTheDocument(); // total_invoices_count
    expect(screen.getByText('1')).toBeInTheDocument(); // paid_invoices_count
    expect(screen.getByText('2')).toBeInTheDocument(); // pending_invoices_count
  });

  it('displays recent invoices table', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check table headers
    expect(screen.getByText('Invoice Number')).toBeInTheDocument();
    expect(screen.getByText('Client')).toBeInTheDocument();
    expect(screen.getByText('Date')).toBeInTheDocument();
    expect(screen.getByText('Amount')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();

    // Check that recent invoices are displayed (first 5)
    expect(screen.getByText('INV-2024-001')).toBeInTheDocument();
    expect(screen.getByText('Acme Corporation')).toBeInTheDocument();
    expect(screen.getByText('Tech Solutions Ltd')).toBeInTheDocument();
  });

  it('displays quick actions section', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    expect(screen.getByText('Quick Actions')).toBeInTheDocument();
    
    // Check quick action links
    expect(screen.getByRole('link', { name: /new session/i })).toHaveAttribute('href', '/sessions/new');
    expect(screen.getByRole('link', { name: /new invoice/i })).toHaveAttribute('href', '/invoices/generate');
  // Accessible name contains heading + description: "Add New Client Add a new client"
  expect(screen.getByRole('link', { name: /add new client/i })).toHaveAttribute('href', '/clients/new');
  });

  it('handles period filter changes', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Find and change the period selector
    const periodSelect = screen.getByDisplayValue('Month');
    fireEvent.change(periodSelect, { target: { value: 'quarter' } });

    // Should trigger a new API call
    await waitFor(() => {
      expect(screen.getByDisplayValue('Quarter')).toBeInTheDocument();
    });
  });

  it('handles year filter changes', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Year select is second combobox (period, year, optional month)
    const selects = screen.getAllByRole('combobox');
    const yearSelect = selects[1];
    fireEvent.change(yearSelect, { target: { value: '2024' } });
    await waitFor(() => {
      expect(screen.getByDisplayValue('2024')).toBeInTheDocument();
    });
  });

  it('shows month selector only when period is month', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Initially should show month selector (default period is month)
    expect(screen.getByDisplayValue('January')).toBeInTheDocument();

    // Change to year period
    const periodSelect = screen.getByDisplayValue('Month');
    fireEvent.change(periodSelect, { target: { value: 'year' } });

    // Month selector should disappear
    await waitFor(() => {
      expect(screen.queryByDisplayValue('January')).not.toBeInTheDocument();
    });
  });

  it('displays error state when API fails', async () => {
    // Mock API failure
    server.use(
      rest.get(`${API_URL}/dashboard/metrics`, (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ error: 'Server error' }));
      }),
      rest.get(`${API_URL}/invoices`, (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ error: 'Server error' }));
      })
    );

    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.getByText(/error loading/i)).toBeInTheDocument();
    });

    expect(screen.getByRole('button', { name: /try again/i })).toBeInTheDocument();
  });

  it('retries data fetching when retry button is clicked', async () => {
    // Mock initial failure
    server.use(
      rest.get(`${API_URL}/dashboard/metrics`, (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ error: 'Server error' }));
      }),
      rest.get(`${API_URL}/invoices`, (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ error: 'Server error' }));
      })
    );

    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.getByText(/error loading/i)).toBeInTheDocument();
    });

    // Reset handlers to success
    server.resetHandlers();

    // Click retry button
    fireEvent.click(screen.getByRole('button', { name: /try again/i }));

    // Should show loading again
    expect(screen.getByText(/loading/i)).toBeInTheDocument();

    // Should eventually show data
    await waitFor(() => {
      expect(screen.queryByText(/error loading/i)).not.toBeInTheDocument();
      expect(screen.getByText('€815.00')).toBeInTheDocument();
    });
  });

  it('formats currency correctly for different locales', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Should format currency in EUR
    expect(screen.getByText('€815.00')).toBeInTheDocument();
    expect(screen.getByText('€477.50')).toBeInTheDocument();
  });

  it('displays status badges with correct styling', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check that status badges are rendered
    const statusBadges = screen.getAllByText(/paid|sent|created/i);
    expect(statusBadges.length).toBeGreaterThan(0);

    // Check specific status styling
    const paidBadge = screen.getByText('Paid');
    expect(paidBadge).toHaveClass('bg-emerald-50', 'text-emerald-700');
  });

  it('displays correct period display name', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

  // With fixed date mocking we expect January 2025
  expect(screen.getByText('(January 2025)')).toBeInTheDocument();
  });

  it('handles empty recent invoices', async () => {
    // Mock empty invoices response
    server.use(
      rest.get(`${API_URL}/invoices`, (req, res, ctx) => {
        return res(ctx.json([]));
      })
    );

    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

  // Translation uses 'No invoices created yet.'
  expect(screen.getByText('No invoices created yet.')).toBeInTheDocument();
  });

  it('limits recent invoices to 5 items', async () => {
    // Mock response with more than 5 invoices
    const manyInvoices = Array.from({ length: 10 }, (_, i) => ({
      ...mockInvoices[0],
      id: i + 1,
      invoice_number: `INV-2024-${String(i + 1).padStart(3, '0')}`,
    }));

    server.use(
      rest.get(`${API_URL}/invoices`, (req, res, ctx) => {
        return res(ctx.json(manyInvoices));
      })
    );

    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Should only show first 5 invoices
    expect(screen.getByText('INV-2024-001')).toBeInTheDocument();
    expect(screen.getByText('INV-2024-005')).toBeInTheDocument();
    expect(screen.queryByText('INV-2024-006')).not.toBeInTheDocument();
  });

  it('navigates to view all invoices', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    const viewAllLink = screen.getByRole('link', { name: /view all/i });
    expect(viewAllLink).toHaveAttribute('href', '/invoices');
  });
});
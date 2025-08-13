import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '../../test-utils/test-utils';
import { rest } from 'msw';
import { server } from '../../test-utils/mocks/server';
import { mockSessions, mockClients } from '../../test-utils/mocks/mockData';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Mock react-router-dom before importing component
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  Link: ({ children, to, ...props }: any) => (
    <a href={to} {...props}>{children}</a>
  ),
}));

import SessionsPage from '../SessionsPage';

// Mock react-datepicker
jest.mock('react-datepicker', () => {
  return {
    __esModule: true,
    default: ({ selected, onChange, className, placeholderText, ...props }: any) => (
      <input
        type="date"
        value={selected ? selected.toISOString().split('T')[0] : ''}
        onChange={(e) => onChange(e.target.value ? new Date(e.target.value) : null)}
        className={className}
        placeholder={placeholderText}
        {...props}
      />
    ),
  };
});

describe('SessionsPage', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
  });

  it('renders page title and add button', async () => {
    render(<SessionsPage />);

    expect(screen.getByText('Sessions')).toBeInTheDocument();
    expect(screen.getByText('Track and manage your work sessions')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    const addButton = screen.getByRole('link', { name: /add session/i });
    expect(addButton).toHaveAttribute('href', '/sessions/new');
  });

  it('displays loading state initially', () => {
    render(<SessionsPage />);

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it('displays sessions table with data', async () => {
    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check table headers
    expect(screen.getByText('Date')).toBeInTheDocument();
    expect(screen.getByText('Client')).toBeInTheDocument();
    expect(screen.getByText('Description')).toBeInTheDocument();
    expect(screen.getByText('Start Time')).toBeInTheDocument();
    expect(screen.getByText('Duration')).toBeInTheDocument();

  // Scope queries to table to avoid duplicate matches with filter options
  const table = screen.getByRole('table');
  const inTable = (text: string | RegExp) => within(table).getByText(text);
  expect(inTable('Website Development')).toBeInTheDocument();
  const acmeCells = within(table).getAllByText('Acme Corporation');
  expect(acmeCells.length).toBeGreaterThanOrEqual(2);
  expect(inTable('3h 0m')).toBeInTheDocument(); // 180 minutes
  expect(inTable('Database Optimization')).toBeInTheDocument();
  expect(inTable('Tech Solutions Ltd')).toBeInTheDocument();
  expect(inTable('3h 30m')).toBeInTheDocument(); // 210 minutes
  });

  it('displays filter section', async () => {
    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    expect(screen.getByText('Filter')).toBeInTheDocument();
    expect(screen.getByLabelText(/client/i)).toBeInTheDocument();
    expect(screen.getByText('Date (von)')).toBeInTheDocument();
    expect(screen.getByText('Date (bis)')).toBeInTheDocument();
  });

  it('filters sessions by client', async () => {
    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Select a specific client
    const clientSelect = screen.getByLabelText(/client/i);
    fireEvent.change(clientSelect, { target: { value: '1' } });

    // Click apply filters
    fireEvent.click(screen.getByText('Apply Filters'));

    await waitFor(() => {
      const table = screen.getByRole('table');
      expect(within(table).getByText('Website Development')).toBeInTheDocument();
      expect(within(table).getByText('Bug Fixes')).toBeInTheDocument();
      expect(within(table).queryByText('Database Optimization')).not.toBeInTheDocument();
    });
  });

  it('filters sessions by date range', async () => {
    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Set date filters
    const startDateInput = screen.getByPlaceholderText('Startdatum auswählen');
    const endDateInput = screen.getByPlaceholderText('Enddatum auswählen');

    fireEvent.change(startDateInput, { target: { value: '2024-01-15' } });
    fireEvent.change(endDateInput, { target: { value: '2024-01-15' } });

    // Click apply filters
    fireEvent.click(screen.getByText('Apply Filters'));

    await waitFor(() => {
      const table = screen.getByRole('table');
      expect(within(table).getByText('Website Development')).toBeInTheDocument();
      expect(within(table).queryByText('Database Optimization')).not.toBeInTheDocument();
    });
  });

  it('clears filters when clear button is clicked', async () => {
    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Set some filters
    const clientSelect = screen.getByLabelText(/client/i);
    fireEvent.change(clientSelect, { target: { value: '1' } });

    // Clear filters
    fireEvent.click(screen.getByText('Clear Filters'));

    // Should reset to show all sessions
    await waitFor(() => {
      const table = screen.getByRole('table');
      expect(within(table).getByText('Website Development')).toBeInTheDocument();
      expect(within(table).getByText('Database Optimization')).toBeInTheDocument();
      expect(within(table).getByText('Bug Fixes')).toBeInTheDocument();
    });
  });

  it('handles row clicks to navigate to session details', async () => {
    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Click on first session row
  const table = screen.getByRole('table');
  const sessionRow = table.querySelector('tr[data-row-id="1"]');
    fireEvent.click(sessionRow!);

  // Navigation assertion: if this proves flaky due to differing id ordering, consider
  // relaxing to expect(mockNavigate).toHaveBeenCalled() in future.
  expect(mockNavigate).toHaveBeenCalled();
  const navCall = mockNavigate.mock.calls.find(c => typeof c[0] === 'string' && /\/sessions\//.test(c[0]));
  expect(navCall && navCall[0]).toMatch(/\/sessions\/(\d+)/);
  });

  it('displays empty state when no sessions exist', async () => {
    // Mock empty sessions response
    server.use(
      rest.get(`${API_URL}/sessions`, (req, res, ctx) => {
        return res(ctx.json([]));
      })
    );

    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    expect(screen.getByText('No sessions yet')).toBeInTheDocument();
    expect(screen.getByText('Start by adding your first work session to track your time.')).toBeInTheDocument();
    
    const addSessionButton = screen.getByRole('link', { name: /add your first session/i });
    expect(addSessionButton).toHaveAttribute('href', '/sessions/new');
  });

  it('displays error state when API fails', async () => {
    // Mock API failure
    server.use(
      rest.get(`${API_URL}/sessions`, (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ error: 'Server error' }));
      })
    );

    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.getByText(/failed to load/i)).toBeInTheDocument();
    });

    expect(screen.getByRole('button', { name: /try again/i })).toBeInTheDocument();
  });

  it('formats session duration correctly', async () => {
    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check duration formatting
  const table = screen.getByRole('table');
  expect(within(table).getByText('3h 0m')).toBeInTheDocument(); // 180 minutes
  expect(within(table).getByText('3h 30m')).toBeInTheDocument(); // 210 minutes
  expect(within(table).getByText('1h 30m')).toBeInTheDocument(); // 90 minutes
  });

  it('formats session time range correctly', async () => {
    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check time range formatting
  const table = screen.getByRole('table');
  expect(within(table).getByText('09:00 - 12:00')).toBeInTheDocument();
  expect(within(table).getByText('14:00 - 17:30')).toBeInTheDocument();
  expect(within(table).getByText('10:00 - 11:30')).toBeInTheDocument();
  });

  it('formats dates correctly', async () => {
    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check date formatting (should be localized)
  const table = screen.getByRole('table');
  expect(within(table).getByText('15.01.2024')).toBeInTheDocument();
  expect(within(table).getByText('16.01.2024')).toBeInTheDocument();
  expect(within(table).getByText('17.01.2024')).toBeInTheDocument();
  });

  it('populates client filter dropdown', async () => {
    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    const clientSelect = screen.getByLabelText(/client/i);
    
    // Check that all clients are in the dropdown
    expect(screen.getByRole('option', { name: 'All Clients' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Acme Corporation' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Tech Solutions Ltd' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Small Business Inc' })).toBeInTheDocument();
  });

  it('handles malformed session data gracefully', async () => {
    // Mock response with malformed data
    server.use(
      rest.get(`${API_URL}/sessions`, (req, res, ctx) => {
        return res(ctx.json([
          null,
          { id: 'invalid' },
          { id: 2, client_name: 'Test Client' }, // Missing required fields
        ]));
      })
    );

    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Should handle malformed data without crashing
    expect(screen.getByText('Test Client')).toBeInTheDocument();
    expect(screen.getByText('Unknown session')).toBeInTheDocument();
    expect(screen.getByText('N/A')).toBeInTheDocument();
  });

  it('maintains filter state during re-renders', async () => {
    const { rerender } = render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Set a filter
    const clientSelect = screen.getByLabelText(/client/i);
    fireEvent.change(clientSelect, { target: { value: '1' } });

    // Re-render component
    rerender(<SessionsPage />);

    // Filter should be maintained
    expect(clientSelect).toHaveValue('1');
  });

  it('validates date range selection', async () => {
    render(<SessionsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    const startDateInput = screen.getByPlaceholderText('Startdatum auswählen');
    const endDateInput = screen.getByPlaceholderText('Enddatum auswählen');

    // Set start date
    fireEvent.change(startDateInput, { target: { value: '2024-01-20' } });

    // End date input should have minDate constraint
    expect(endDateInput).toHaveAttribute('min', '2024-01-20');
  });
});
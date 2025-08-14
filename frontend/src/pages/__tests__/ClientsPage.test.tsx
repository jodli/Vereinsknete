import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '../../test-utils/test-utils';
import { rest } from 'msw';
import { server } from '../../test-utils/mocks/server';
import { handlers } from '../../test-utils/mocks/handlers';
import ClientsPage from '../ClientsPage';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Mock react-router-dom BEFORE importing component so hook is mocked inside component
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  Link: ({ children, to, ...props }: any) => (
    <a href={to} {...props}>{children}</a>
  ),
}));

describe('ClientsPage', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
    // Ensure English language is set for tests
    localStorage.setItem('preferredLanguage', 'en');
  });

  it('renders page title and subtitle', async () => {
    render(<ClientsPage />);

    // Wait for data to load
    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    expect(screen.getByText('Clients')).toBeInTheDocument();
    expect(screen.getByText('Manage your clients and their information')).toBeInTheDocument();
  });

  it('displays loading state initially', () => {
    render(<ClientsPage />);

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it('displays add new client button', async () => {
    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    const addButton = screen.getByRole('link', { name: /add client/i });
    expect(addButton).toBeInTheDocument();
    expect(addButton).toHaveAttribute('href', '/clients/new');
  });

  it('displays clients table with data', async () => {
    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check table headers
    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Contact Person')).toBeInTheDocument();
    expect(screen.getByText('Hourly Rate')).toBeInTheDocument();

    // Check client data
    expect(screen.getByText('Acme Corporation')).toBeInTheDocument();
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    expect(screen.getByText('€75.00')).toBeInTheDocument();

    expect(screen.getByText('Tech Solutions Ltd')).toBeInTheDocument();
    expect(screen.getByText('Bob Johnson')).toBeInTheDocument();
    expect(screen.getByText('€85.00')).toBeInTheDocument();

    expect(screen.getByText('Small Business Inc')).toBeInTheDocument();
    expect(screen.getByText('-')).toBeInTheDocument(); // No contact person
    expect(screen.getByText('€60.00')).toBeInTheDocument();
  });

  it('displays client avatars with icons', async () => {
    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Should have user icons for each client
    const userIcons = document.querySelectorAll('svg');
    expect(userIcons.length).toBeGreaterThan(0);
  });

  it('handles row clicks to navigate to client details', async () => {
    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Click on first client row
  const table = screen.getByRole('table');
  const clientRow = table.querySelector('tr[data-row-id="1"]');
  fireEvent.click(clientRow!);

  expect(mockNavigate).toHaveBeenCalled();
  const navCall = mockNavigate.mock.calls.find(c => typeof c[0] === 'string' && /\/clients\//.test(c[0]));
  expect(navCall && navCall[0]).toMatch(/\/clients\/(\d+)/);
  });

  it('displays empty state when no clients exist', async () => {
    // Mock empty clients response
    server.use(
      rest.get(`${API_URL}/clients`, (req, res, ctx) => {
        return res(ctx.json([]));
      })
    );

    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    expect(screen.getByText('No clients yet')).toBeInTheDocument();
    expect(screen.getByText('Get started by adding your first client')).toBeInTheDocument();
    
    const addClientButton = screen.getByRole('link', { name: /add your first client/i });
    expect(addClientButton).toHaveAttribute('href', '/clients/new');
  });

  it('displays error state when API fails', async () => {
    // Mock API failure
    server.use(
      rest.get(`${API_URL}/clients`, (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ error: 'Server error' }));
      })
    );

    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.getByText(/network request failed/i)).toBeInTheDocument();
    });

    expect(screen.getByRole('button', { name: /try again/i })).toBeInTheDocument();
  });

  it('retries data fetching when retry button is clicked', async () => {
    // Mock initial failure
    server.use(
      rest.get(`${API_URL}/clients`, (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ error: 'Server error' }));
      })
    );

    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.getByText(/network request failed/i)).toBeInTheDocument();
    });

    // Reset handlers to success - restore original handlers
    server.resetHandlers(...handlers);

    // Click retry button
    fireEvent.click(screen.getByRole('button', { name: /try again/i }));

    // Should show loading again
    expect(screen.getByText(/loading/i)).toBeInTheDocument();

    // Should eventually show data
    await waitFor(() => {
      expect(screen.queryByText(/network request failed/i)).not.toBeInTheDocument();
    });
    
    // Wait for the client data to appear
    await waitFor(() => {
      expect(screen.getByText('Acme Corporation')).toBeInTheDocument();
    }, { timeout: 5000 });
  });

  it('formats currency correctly', async () => {
    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Should format hourly rates as EUR currency
    expect(screen.getByText('€75.00')).toBeInTheDocument();
    expect(screen.getByText('€85.00')).toBeInTheDocument();
    expect(screen.getByText('€60.00')).toBeInTheDocument();
  });

  it('handles clients with and without contact persons', async () => {
    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Clients with contact persons
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    expect(screen.getByText('Bob Johnson')).toBeInTheDocument();

    // Client without contact person should show dash
    const contactCells = screen.getAllByText('-');
    expect(contactCells.length).toBeGreaterThan(0);
  });

  it('displays correct table structure', async () => {
    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check that table has correct number of rows (3 clients + header)
    const table = screen.getByRole('table');
    const rows = table.querySelectorAll('tr');
    expect(rows).toHaveLength(4); // 1 header + 3 data rows
  });

  it('shows hover effects on table rows', async () => {
    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

  const table = screen.getByRole('table');
  const clientRow = within(table).getByText('Acme Corporation').closest('tr');
    expect(clientRow).toHaveClass('hover:bg-gray-50', 'cursor-pointer');
  });

  it('displays client names as primary information', async () => {
    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Client names should be styled as primary text
  const table = screen.getByRole('table');
  const acmeCell = within(table).getByText('Acme Corporation');
    expect(acmeCell).toHaveClass('text-gray-900', 'font-medium');
  });

  it('shows contact person as secondary information', async () => {
    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Contact persons should be styled as secondary text
  const table = screen.getByRole('table');
  const contactCell = within(table).getByText('Jane Smith');
    expect(contactCell.parentElement).toHaveClass('text-gray-500');
  });

  it('handles network errors gracefully', async () => {
    // Mock network error
    server.use(
      rest.get(`${API_URL}/clients`, (req, res, ctx) => {
        return res.networkError('Network connection failed');
      })
    );

    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.getByText(/try again/i)).toBeInTheDocument();
    });
  });

  it('maintains responsive design', async () => {
    render(<ClientsPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check that responsive classes are applied
    const headerContainer = screen.getByText('Clients').parentElement;
    expect(headerContainer).toHaveClass('sm:flex-row', 'sm:items-center', 'sm:justify-between');
  });
});
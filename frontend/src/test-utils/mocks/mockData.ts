import { 
  Client, 
  // Session, 
  SessionWithDuration, 
  Invoice, 
  UserProfile, 
  DashboardMetrics 
} from '../../types';

export const mockUserProfile: UserProfile = {
  id: 1,
  name: 'John Doe',
  address: '123 Main St, City, Country',
  tax_id: 'TAX123456',
  bank_details: 'Bank: Test Bank, IBAN: DE89370400440532013000'
};

export const mockClients: Client[] = [
  {
    id: 1,
    name: 'Acme Corporation',
    address: '456 Business Ave, Business City',
    contact_person: 'Jane Smith',
    default_hourly_rate: 75.00
  },
  {
    id: 2,
    name: 'Tech Solutions Ltd',
    address: '789 Tech Street, Innovation District',
    contact_person: 'Bob Johnson',
    default_hourly_rate: 85.00
  },
  {
    id: 3,
    name: 'Small Business Inc',
    address: '321 Small St, Local Town',
    contact_person: null,
    default_hourly_rate: 60.00
  }
];

export const mockSessions: SessionWithDuration[] = [
  {
    id: 1,
    client_id: 1,
    name: 'Website Development',
    date: '2024-01-15',
    start_time: '09:00',
    end_time: '12:00',
    created_at: '2024-01-15T09:00:00Z',
    client_name: 'Acme Corporation',
    duration_minutes: 180
  },
  {
    id: 2,
    client_id: 2,
    name: 'Database Optimization',
    date: '2024-01-16',
    start_time: '14:00',
    end_time: '17:30',
    created_at: '2024-01-16T14:00:00Z',
    client_name: 'Tech Solutions Ltd',
    duration_minutes: 210
  },
  {
    id: 3,
    client_id: 1,
    name: 'Bug Fixes',
    date: '2024-01-17',
    start_time: '10:00',
    end_time: '11:30',
    created_at: '2024-01-17T10:00:00Z',
    client_name: 'Acme Corporation',
    duration_minutes: 90
  }
];

export const mockInvoices: Invoice[] = [
  {
    id: 1,
    invoice_number: 'INV-2024-001',
    client_name: 'Acme Corporation',
    date: '2024-01-31',
    total_amount: 337.50,
    status: 'paid',
    due_date: '2024-02-15',
    paid_date: '2024-02-10',
    created_at: '2024-01-31T10:00:00Z'
  },
  {
    id: 2,
    invoice_number: 'INV-2024-002',
    client_name: 'Tech Solutions Ltd',
    date: '2024-01-31',
    total_amount: 297.50,
    status: 'sent',
    due_date: '2024-02-15',
    paid_date: null,
    created_at: '2024-01-31T11:00:00Z'
  },
  {
    id: 3,
    invoice_number: 'INV-2024-003',
    client_name: 'Small Business Inc',
    date: '2024-02-01',
    total_amount: 180.00,
    status: 'created',
    due_date: '2024-02-16',
    paid_date: null,
    created_at: '2024-02-01T09:00:00Z'
  }
];

export const mockDashboardMetrics: DashboardMetrics = {
  total_revenue_period: 815.00,
  pending_invoices_amount: 477.50,
  total_invoices_count: 3,
  paid_invoices_count: 1,
  pending_invoices_count: 2
};

// Factory functions for creating test data
export const createMockClient = (overrides: Partial<Client> = {}): Client => ({
  id: Math.floor(Math.random() * 1000),
  name: 'Test Client',
  address: 'Test Address',
  contact_person: 'Test Contact',
  default_hourly_rate: 75.00,
  ...overrides
});

export const createMockSession = (overrides: Partial<SessionWithDuration> = {}): SessionWithDuration => ({
  id: Math.floor(Math.random() * 1000),
  client_id: 1,
  name: 'Test Session',
  date: '2024-01-15',
  start_time: '09:00',
  end_time: '10:00',
  created_at: '2024-01-15T09:00:00Z',
  client_name: 'Test Client',
  duration_minutes: 60,
  ...overrides
});

export const createMockInvoice = (overrides: Partial<Invoice> = {}): Invoice => ({
  id: Math.floor(Math.random() * 1000),
  invoice_number: 'INV-TEST-001',
  client_name: 'Test Client',
  date: '2024-01-31',
  total_amount: 75.00,
  status: 'created',
  due_date: '2024-02-15',
  paid_date: null,
  created_at: '2024-01-31T10:00:00Z',
  ...overrides
});
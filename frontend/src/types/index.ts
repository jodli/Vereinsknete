// User Profile types
export interface UserProfile {
    id: number;
    name: string;
    address: string;
    tax_id: string | null;
    bank_details: string | null;
}

export interface UserProfileFormData {
    name: string;
    address: string;
    tax_id?: string;
    bank_details?: string;
}

// Client types
export interface Client {
    id: number;
    name: string;
    address: string;
    contact_person: string | null;
    default_hourly_rate: number;
}

export interface ClientFormData {
    name: string;
    address: string;
    contact_person?: string;
    default_hourly_rate: number;
}

// Session types
export interface Session {
    id: number;
    client_id: number;
    name: string;
    date: string;
    start_time: string;
    end_time: string;
    created_at: string;
}

export interface SessionWithDuration {
    id: number;
    client_id: number;
    name: string;
    date: string;
    start_time: string;
    end_time: string;
    created_at: string;
    client_name: string;
    duration_minutes: number;
}

export interface SessionFormData {
    client_id: number;
    name: string;
    date: string;
    start_time: string;
    end_time: string;
}

export interface SessionFilterParams {
    client_id?: number;
    start_date?: string;
    end_date?: string;
}

// Invoice types
export interface InvoiceRequest {
    client_id: number;
    start_date: string;
    end_date: string;
    language?: string;
}

export interface InvoiceSessionItem {
    name: string;
    date: string;
    start_time: string;
    end_time: string;
    duration_hours: number;
    amount: number;
}

export interface InvoiceResponse {
    invoice_number: string;
    date: string;
    user_profile: UserProfile;
    client: Client;
    sessions: InvoiceSessionItem[];
    total_hours: number;
    total_amount: number;
}

export interface Invoice {
    id: number;
    invoice_number: string;
    client_name: string;
    date: string;
    total_amount: number;
    status: string;
    due_date: string | null;
    paid_date: string | null;
    created_at: string;
}

export interface UpdateInvoiceStatusRequest {
    status: string;
    paid_date?: string | null;
}

export interface DashboardMetrics {
    total_revenue_period: number;
    pending_invoices_amount: number;
    total_invoices_count: number;
    paid_invoices_count: number;
    pending_invoices_count: number;
}

export interface DashboardQuery {
    period: 'month' | 'quarter' | 'year';
    year: number;
    month?: number;
}

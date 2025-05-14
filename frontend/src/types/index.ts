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
    session: Session;
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

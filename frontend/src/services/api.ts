import {
    UserProfile,
    Client,
    Session,
    SessionWithDuration,
    InvoiceRequest,
    UserProfileFormData,
    ClientFormData,
    SessionFormData,
    SessionFilterParams,
    Invoice,
    UpdateInvoiceStatusRequest,
    DashboardMetrics,
    DashboardQuery
} from '../types';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// User Profile API calls
export const getUserProfile = async (): Promise<UserProfile | null> => {
    try {
        const response = await fetch(`${API_URL}/profile`);
        if (response.status === 404) {
            return null;
        }
        if (!response.ok) {
            throw new Error(`Failed to get user profile: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error fetching user profile:', error);
        throw error;
    }
};

export const updateUserProfile = async (profileData: UserProfileFormData): Promise<UserProfile> => {
    try {
        const response = await fetch(`${API_URL}/profile`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(profileData),
        });
        if (!response.ok) {
            throw new Error(`Failed to update user profile: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error updating user profile:', error);
        throw error;
    }
};

// Client API calls
export const getClients = async (): Promise<Client[]> => {
    try {
        const response = await fetch(`${API_URL}/clients`);
        if (!response.ok) {
            throw new Error(`Failed to get clients: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error fetching clients:', error);
        throw error;
    }
};

export const getClient = async (id: number): Promise<Client> => {
    try {
        const response = await fetch(`${API_URL}/clients/${id}`);
        if (!response.ok) {
            throw new Error(`Failed to get client: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error(`Error fetching client ${id}:`, error);
        throw error;
    }
};

export const createClient = async (clientData: ClientFormData): Promise<Client> => {
    try {
        const response = await fetch(`${API_URL}/clients`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(clientData),
        });
        if (!response.ok) {
            throw new Error(`Failed to create client: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error creating client:', error);
        throw error;
    }
};

export const updateClient = async (id: number, clientData: Partial<ClientFormData>): Promise<Client> => {
    try {
        const response = await fetch(`${API_URL}/clients/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(clientData),
        });
        if (!response.ok) {
            throw new Error(`Failed to update client: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error(`Error updating client ${id}:`, error);
        throw error;
    }
};

export const deleteClient = async (id: number): Promise<void> => {
    try {
        const response = await fetch(`${API_URL}/clients/${id}`, {
            method: 'DELETE',
        });
        if (!response.ok) {
            throw new Error(`Failed to delete client: ${response.statusText}`);
        }
    } catch (error) {
        console.error(`Error deleting client ${id}:`, error);
        throw error;
    }
};

// Session API calls
export const getSessions = async (filters?: SessionFilterParams): Promise<SessionWithDuration[]> => {
    try {
        let url = `${API_URL}/sessions`;
        if (filters) {
            const params = new URLSearchParams();
            if (filters.client_id) params.append('client_id', filters.client_id.toString());
            if (filters.start_date) params.append('start_date', filters.start_date);
            if (filters.end_date) params.append('end_date', filters.end_date);
            url += `?${params.toString()}`;
        }

        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Failed to get sessions: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error fetching sessions:', error);
        throw error;
    }
};

export const getSession = async (id: number): Promise<Session> => {
    try {
        const response = await fetch(`${API_URL}/sessions/${id}`);
        if (!response.ok) {
            throw new Error(`Failed to get session: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error(`Error fetching session ${id}:`, error);
        throw error;
    }
};

export const createSession = async (sessionData: SessionFormData): Promise<Session> => {
    try {
        const response = await fetch(`${API_URL}/sessions`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(sessionData),
        });
        if (!response.ok) {
            throw new Error(`Failed to create session: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error creating session:', error);
        throw error;
    }
};

export const updateSession = async (id: number, sessionData: SessionFormData): Promise<Session> => {
    try {
        const response = await fetch(`${API_URL}/sessions/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(sessionData),
        });
        if (!response.ok) {
            throw new Error(`Failed to update session: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error(`Error updating session ${id}:`, error);
        throw error;
    }
};

export const deleteSession = async (id: number): Promise<void> => {
    try {
        const response = await fetch(`${API_URL}/sessions/${id}`, {
            method: 'DELETE',
        });
        if (!response.ok) {
            throw new Error(`Failed to delete session: ${response.statusText}`);
        }
    } catch (error) {
        console.error(`Error deleting session ${id}:`, error);
        throw error;
    }
};

// Invoice API calls
export const generateInvoice = async (invoiceRequest: InvoiceRequest): Promise<{ invoice_id: number, invoice_number: string, pdf_bytes: string }> => {
    try {
        const response = await fetch(`${API_URL}/invoices/generate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(invoiceRequest),
        });
        if (!response.ok) {
            throw new Error(`Failed to generate invoice: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error generating invoice:', error);
        throw error;
    }
};

export const downloadInvoicePdf = async (invoiceId: number): Promise<Blob> => {
    try {
        const response = await fetch(`${API_URL}/invoices/${invoiceId}/pdf`);
        if (!response.ok) {
            throw new Error(`Failed to download invoice PDF: ${response.statusText}`);
        }
        return await response.blob();
    } catch (error) {
        console.error('Error downloading invoice PDF:', error);
        throw error;
    }
};

export const getAllInvoices = async (): Promise<Invoice[]> => {
    try {
        const response = await fetch(`${API_URL}/invoices`);
        if (!response.ok) {
            throw new Error(`Failed to get invoices: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error getting invoices:', error);
        throw error;
    }
};

export const updateInvoiceStatus = async (invoiceId: number, statusRequest: UpdateInvoiceStatusRequest): Promise<void> => {
    try {
        const response = await fetch(`${API_URL}/invoices/${invoiceId}/status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(statusRequest),
        });
        if (!response.ok) {
            throw new Error(`Failed to update invoice status: ${response.statusText}`);
        }
    } catch (error) {
        console.error('Error updating invoice status:', error);
        throw error;
    }
};

export const deleteInvoice = async (invoiceId: number): Promise<void> => {
    try {
        const response = await fetch(`${API_URL}/invoices/${invoiceId}`, {
            method: 'DELETE',
        });
        if (!response.ok) {
            throw new Error(`Failed to delete invoice: ${response.statusText}`);
        }
    } catch (error) {
        console.error('Error deleting invoice:', error);
        throw error;
    }
};

export const getDashboardMetrics = async (query: DashboardQuery): Promise<DashboardMetrics> => {
    try {
        const params = new URLSearchParams({
            period: query.period,
            year: query.year.toString(),
        });
        if (query.month) {
            params.append('month', query.month.toString());
        }

        const response = await fetch(`${API_URL}/dashboard/metrics?${params}`);
        if (!response.ok) {
            throw new Error(`Failed to get dashboard metrics: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error getting dashboard metrics:', error);
        throw error;
    }
};

-- Create tables for the VereinsKnete application

CREATE TABLE user_profile (
    id INTEGER PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    address TEXT NOT NULL,
    tax_id TEXT,
    bank_details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE clients (
    id INTEGER PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    address TEXT NOT NULL,
    contact_person TEXT,
    default_hourly_rate REAL NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sessions (
    id INTEGER PRIMARY KEY NOT NULL,
    client_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    date TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients (id) ON DELETE CASCADE
);

CREATE TABLE invoices (
    id INTEGER PRIMARY KEY NOT NULL,
    invoice_number TEXT NOT NULL,
    client_id INTEGER NOT NULL,
    date TEXT NOT NULL,
    total_amount REAL NOT NULL,
    pdf_path TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'created',
    due_date TEXT,
    paid_date TEXT,
    year INTEGER NOT NULL,
    sequence_number INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients (id) ON DELETE CASCADE,
    UNIQUE(year, sequence_number)
);

-- Add trigger to update the updated_at timestamp for user_profile
CREATE TRIGGER update_user_profile_timestamp
AFTER UPDATE ON user_profile
BEGIN
    UPDATE user_profile SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Add trigger to update the updated_at timestamp for clients
CREATE TRIGGER update_clients_timestamp
AFTER UPDATE ON clients
BEGIN
    UPDATE clients SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

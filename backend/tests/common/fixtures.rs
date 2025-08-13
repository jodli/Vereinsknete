use backend::models::client::{NewClient, UpdateClient};
use backend::models::invoice::{DashboardQuery, InvoiceRequest, UpdateInvoiceStatusRequest};
use backend::models::session::{NewSessionRequest, UpdateSessionRequest};
use backend::models::user_profile::{NewUserProfile, UpdateUserProfile};
use chrono::{NaiveDate, NaiveTime};

/// Creates a test client with default values
pub fn create_test_client() -> NewClient {
    NewClient {
        name: "Test Client".to_string(),
        address: "123 Test Street, Test City, 12345".to_string(),
        contact_person: Some("John Doe".to_string()),
        default_hourly_rate: 75.0,
    }
}

/// Creates a test client with custom name
pub fn create_test_client_with_name(name: &str) -> NewClient {
    NewClient {
        name: name.to_string(),
        address: "123 Test Street, Test City, 12345".to_string(),
        contact_person: Some("John Doe".to_string()),
        default_hourly_rate: 75.0,
    }
}

/// Creates a test client with minimal data
pub fn create_minimal_test_client() -> NewClient {
    NewClient {
        name: "Minimal Client".to_string(),
        address: "456 Minimal Ave, Min City, 67890".to_string(),
        contact_person: None,
        default_hourly_rate: 50.0,
    }
}

/// Creates an invalid test client (for validation testing)
pub fn create_invalid_test_client() -> NewClient {
    NewClient {
        name: "".to_string(),                 // Invalid: empty name
        address: "Short".to_string(),         // Invalid: too short
        contact_person: Some("".to_string()), // Invalid: empty contact person
        default_hourly_rate: -10.0,           // Invalid: negative rate
    }
}

/// Creates a test client update
pub fn create_test_client_update() -> UpdateClient {
    UpdateClient {
        name: Some("Updated Client".to_string()),
        address: Some("789 Updated Street, Updated City, 54321".to_string()),
        contact_person: Some("Jane Smith".to_string()),
        default_hourly_rate: Some(85.0),
    }
}

/// Creates a test session request
pub fn create_test_session() -> NewSessionRequest {
    NewSessionRequest {
        client_id: 1,
        name: "Test Session".to_string(),
        date: NaiveDate::from_ymd_opt(2024, 1, 15).unwrap(),
        start_time: NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
        end_time: NaiveTime::from_hms_opt(17, 0, 0).unwrap(),
    }
}

/// Creates a test session with custom client ID
pub fn create_test_session_with_client(client_id: i32) -> NewSessionRequest {
    NewSessionRequest {
        client_id,
        name: "Test Session".to_string(),
        date: NaiveDate::from_ymd_opt(2024, 1, 15).unwrap(),
        start_time: NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
        end_time: NaiveTime::from_hms_opt(17, 0, 0).unwrap(),
    }
}

/// Creates a test session with custom date and times
pub fn create_test_session_with_time(
    client_id: i32,
    date: NaiveDate,
    start: NaiveTime,
    end: NaiveTime,
) -> NewSessionRequest {
    NewSessionRequest {
        client_id,
        name: "Custom Time Session".to_string(),
        date,
        start_time: start,
        end_time: end,
    }
}

/// Creates an invalid test session (for validation testing)
pub fn create_invalid_test_session() -> NewSessionRequest {
    NewSessionRequest {
        client_id: 0,         // Invalid: zero client ID
        name: "".to_string(), // Invalid: empty name
        date: NaiveDate::from_ymd_opt(2024, 1, 15).unwrap(),
        start_time: NaiveTime::from_hms_opt(17, 0, 0).unwrap(), // Invalid: start after end
        end_time: NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
    }
}

/// Creates a test session update
pub fn create_test_session_update() -> UpdateSessionRequest {
    UpdateSessionRequest {
        client_id: 1,
        name: "Updated Session".to_string(),
        date: NaiveDate::from_ymd_opt(2024, 1, 16).unwrap(),
        start_time: NaiveTime::from_hms_opt(10, 0, 0).unwrap(),
        end_time: NaiveTime::from_hms_opt(18, 0, 0).unwrap(),
    }
}

/// Creates a test invoice request
pub fn create_test_invoice_request() -> InvoiceRequest {
    InvoiceRequest {
        client_id: 1,
        start_date: NaiveDate::from_ymd_opt(2024, 1, 1).unwrap(),
        end_date: NaiveDate::from_ymd_opt(2024, 1, 31).unwrap(),
        language: Some("en".to_string()),
    }
}

/// Creates a test invoice request with custom date range
pub fn create_test_invoice_request_with_dates(
    client_id: i32,
    start_date: NaiveDate,
    end_date: NaiveDate,
) -> InvoiceRequest {
    InvoiceRequest {
        client_id,
        start_date,
        end_date,
        language: Some("en".to_string()),
    }
}

/// Creates an invalid test invoice request
pub fn create_invalid_test_invoice_request() -> InvoiceRequest {
    InvoiceRequest {
        client_id: 0,                                              // Invalid: zero client ID
        start_date: NaiveDate::from_ymd_opt(2024, 1, 31).unwrap(), // Invalid: start after end
        end_date: NaiveDate::from_ymd_opt(2024, 1, 1).unwrap(),
        language: Some("invalid".to_string()), // Invalid language code
    }
}

/// Creates a test dashboard query
pub fn create_test_dashboard_query() -> DashboardQuery {
    DashboardQuery {
        period: "month".to_string(),
        year: 2024,
        month: Some(1),
    }
}

/// Creates a test dashboard query for year
pub fn create_test_dashboard_query_year() -> DashboardQuery {
    DashboardQuery {
        period: "year".to_string(),
        year: 2024,
        month: None,
    }
}

/// Creates a test invoice status update
pub fn create_test_invoice_status_update() -> UpdateInvoiceStatusRequest {
    UpdateInvoiceStatusRequest {
        status: "paid".to_string(),
        paid_date: Some("2024-01-15".to_string()),
    }
}

/// Creates a test user profile
pub fn create_test_user_profile() -> NewUserProfile {
    NewUserProfile {
        name: "Test User".to_string(),
        address: "123 User Street, User City, 12345".to_string(),
        tax_id: Some("TAX123456".to_string()),
        bank_details: Some("Bank: Test Bank, IBAN: DE89370400440532013000".to_string()),
    }
}

/// Creates a minimal test user profile
pub fn create_minimal_test_user_profile() -> NewUserProfile {
    NewUserProfile {
        name: "Minimal User".to_string(),
        address: "456 Minimal Ave, Min City, 67890".to_string(),
        tax_id: None,
        bank_details: None,
    }
}

/// Creates an invalid test user profile
pub fn create_invalid_test_user_profile() -> NewUserProfile {
    NewUserProfile {
        name: "".to_string(),               // Invalid: empty name
        address: "Short".to_string(),       // Invalid: too short
        tax_id: Some("".to_string()),       // Invalid: empty tax ID
        bank_details: Some("".to_string()), // Invalid: empty bank details
    }
}

/// Creates a test user profile update
pub fn create_test_user_profile_update() -> UpdateUserProfile {
    UpdateUserProfile {
        name: Some("Updated User".to_string()),
        address: Some("789 Updated Street, Updated City, 54321".to_string()),
        tax_id: Some("NEWTAX789".to_string()),
        bank_details: Some("Bank: New Bank, IBAN: DE89370400440532013001".to_string()),
    }
}

/// Test data sets for bulk operations
pub struct TestDataSet {
    pub clients: Vec<NewClient>,
    pub sessions: Vec<NewSessionRequest>,
}

impl TestDataSet {
    /// Creates a complete test dataset with multiple clients and sessions
    pub fn create_full_dataset() -> Self {
        let clients = vec![
            create_test_client_with_name("Alpha Corp"),
            create_test_client_with_name("Beta LLC"),
            create_test_client_with_name("Gamma Inc"),
        ];

        let sessions = vec![
            create_test_session_with_time(
                1,
                NaiveDate::from_ymd_opt(2024, 1, 10).unwrap(),
                NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
                NaiveTime::from_hms_opt(12, 0, 0).unwrap(),
            ),
            create_test_session_with_time(
                1,
                NaiveDate::from_ymd_opt(2024, 1, 11).unwrap(),
                NaiveTime::from_hms_opt(13, 0, 0).unwrap(),
                NaiveTime::from_hms_opt(17, 0, 0).unwrap(),
            ),
            create_test_session_with_time(
                2,
                NaiveDate::from_ymd_opt(2024, 1, 12).unwrap(),
                NaiveTime::from_hms_opt(10, 0, 0).unwrap(),
                NaiveTime::from_hms_opt(16, 0, 0).unwrap(),
            ),
        ];

        TestDataSet { clients, sessions }
    }
}

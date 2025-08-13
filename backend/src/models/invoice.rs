use crate::models::{client::Client, user_profile::UserProfile};
use crate::schema::invoices;
use chrono::NaiveDate;
use diesel::prelude::*;
use serde::{Deserialize, Serialize};
use validator::Validate;

#[derive(Debug, Serialize, Deserialize, Validate)]
pub struct InvoiceRequest {
    #[validate(range(min = 1, message = "Client ID must be positive"))]
    pub client_id: i32,

    pub start_date: NaiveDate,
    pub end_date: NaiveDate,

    #[validate(length(
        min = 2,
        max = 5,
        message = "Language must be 2-5 characters (e.g., 'en', 'de')"
    ))]
    #[serde(default)]
    pub language: Option<String>,
}

#[derive(Debug, Serialize)]
pub struct InvoiceSessionItem {
    pub name: String,
    pub date: String,
    pub start_time: String,
    pub end_time: String,
    pub duration_hours: f32,
    pub amount: f32,
}

#[derive(Debug, Serialize)]
pub struct InvoiceResponse {
    pub invoice_number: String,
    pub date: String,
    pub user_profile: UserProfile,
    pub client: Client,
    pub sessions: Vec<InvoiceSessionItem>,
    pub total_hours: f32,
    pub total_amount: f32,
}

#[derive(Debug, Serialize, Deserialize, Validate)]
pub struct UpdateInvoiceStatusRequest {
    #[validate(length(
        min = 1,
        max = 20,
        message = "Status must be between 1 and 20 characters"
    ))]
    pub status: String,

    pub paid_date: Option<String>,
}

#[derive(Debug, Serialize)]
pub struct InvoiceListItem {
    pub id: i32,
    pub invoice_number: String,
    pub client_name: String,
    pub date: String,
    pub total_amount: f32,
    pub status: String,
    pub due_date: Option<String>,
    pub paid_date: Option<String>,
    pub created_at: chrono::NaiveDateTime,
}

#[derive(Debug, Serialize)]
pub struct DashboardMetrics {
    pub total_revenue_period: f32,
    pub pending_invoices_amount: f32,
    pub total_invoices_count: i32,
    pub paid_invoices_count: i32,
    pub pending_invoices_count: i32,
}

#[derive(Debug, Deserialize, Validate)]
pub struct DashboardQuery {
    #[validate(length(min = 1, max = 20, message = "Period must be specified"))]
    pub period: String, // month, quarter, year

    #[validate(range(min = 2000, max = 2100, message = "Year must be between 2000 and 2100"))]
    pub year: i32,

    #[validate(range(min = 1, max = 12, message = "Month must be between 1 and 12"))]
    pub month: Option<i32>,
}

#[derive(Debug, Serialize, Deserialize, Queryable, Selectable)]
#[diesel(table_name = invoices)]
pub struct Invoice {
    pub id: i32,
    pub invoice_number: String,
    pub client_id: i32,
    pub date: String,
    pub total_amount: f32,
    pub pdf_path: String,
    pub status: String,
    pub due_date: Option<String>,
    pub paid_date: Option<String>,
    pub year: i32,
    pub sequence_number: i32,
    pub created_at: chrono::NaiveDateTime,
}

impl InvoiceRequest {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize language input
        if let Some(ref mut lang) = self.language {
            *lang = lang.trim().to_lowercase();
            if lang.is_empty() {
                self.language = None;
            }
        }

        // Validate basic fields
        self.validate()?;

        // Custom validation: end date must be after start date
        if self.end_date <= self.start_date {
            let mut errors = validator::ValidationErrors::new();
            let mut error = validator::ValidationError::new("invalid_date_range");
            error.message = Some("End date must be after start date".into());
            errors.add("end_date", error);
            return Err(errors);
        }

        Ok(())
    }
}

impl UpdateInvoiceStatusRequest {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize status
        self.status = self.status.trim().to_lowercase();

        // Validate
        self.validate()?;

        // Custom validation: valid status values
        if !matches!(
            self.status.as_str(),
            "created" | "sent" | "paid" | "overdue" | "cancelled"
        ) {
            let mut errors = validator::ValidationErrors::new();
            let mut error = validator::ValidationError::new("invalid_status");
            error.message =
                Some("Status must be one of: created, sent, paid, overdue, cancelled".into());
            errors.add("status", error);
            return Err(errors);
        }

        Ok(())
    }
}

impl DashboardQuery {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize period
        self.period = self.period.trim().to_lowercase();

        // Validate
        self.validate()?;

        // Custom validation: valid period values
        if !matches!(self.period.as_str(), "month" | "quarter" | "year") {
            let mut errors = validator::ValidationErrors::new();
            let mut error = validator::ValidationError::new("invalid_period");
            error.message = Some("Period must be one of: month, quarter, year".into());
            errors.add("period", error);
            return Err(errors);
        }

        // If period is month, month must be provided
        if self.period == "month" && self.month.is_none() {
            let mut errors = validator::ValidationErrors::new();
            let mut error = validator::ValidationError::new("month_required");
            error.message = Some("Month is required when period is 'month'".into());
            errors.add("month", error);
            return Err(errors);
        }

        Ok(())
    }
}

#[derive(Debug, Serialize, Deserialize, Insertable)]
#[diesel(table_name = invoices)]
pub struct NewInvoice {
    pub invoice_number: String,
    pub client_id: i32,
    pub date: String,
    pub total_amount: f32,
    pub pdf_path: String,
    pub status: String,
    pub due_date: Option<String>,
    pub year: i32,
    pub sequence_number: i32,
}

#[cfg(test)]
mod tests {
    use super::*;
    use chrono::NaiveDate;
    use validator::Validate;

    // Test fixtures
    fn create_valid_invoice_request() -> InvoiceRequest {
        InvoiceRequest {
            client_id: 1,
            start_date: NaiveDate::from_ymd_opt(2024, 1, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2024, 1, 31).unwrap(),
            language: Some("en".to_string()),
        }
    }

    fn create_german_invoice_request() -> InvoiceRequest {
        InvoiceRequest {
            client_id: 2,
            start_date: NaiveDate::from_ymd_opt(2024, 2, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2024, 2, 29).unwrap(),
            language: Some("de".to_string()),
        }
    }

    fn create_minimal_invoice_request() -> InvoiceRequest {
        InvoiceRequest {
            client_id: 3,
            start_date: NaiveDate::from_ymd_opt(2024, 3, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2024, 3, 31).unwrap(),
            language: None,
        }
    }

    fn create_valid_status_update() -> UpdateInvoiceStatusRequest {
        UpdateInvoiceStatusRequest {
            status: "paid".to_string(),
            paid_date: Some("2024-01-15".to_string()),
        }
    }

    fn create_valid_dashboard_query() -> DashboardQuery {
        DashboardQuery {
            period: "month".to_string(),
            year: 2024,
            month: Some(1),
        }
    }

    // InvoiceRequest validation tests
    #[test]
    fn test_invoice_request_valid() {
        let request = create_valid_invoice_request();
        assert!(request.validate().is_ok());
    }

    #[test]
    fn test_invoice_request_german_valid() {
        let request = create_german_invoice_request();
        assert!(request.validate().is_ok());
    }

    #[test]
    fn test_invoice_request_minimal_valid() {
        let request = create_minimal_invoice_request();
        assert!(request.validate().is_ok());
    }

    #[test]
    fn test_invoice_request_zero_client_id() {
        let mut request = create_valid_invoice_request();
        request.client_id = 0;

        let result = request.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("client_id"));
    }

    #[test]
    fn test_invoice_request_negative_client_id() {
        let mut request = create_valid_invoice_request();
        request.client_id = -1;

        let result = request.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("client_id"));
    }

    #[test]
    fn test_invoice_request_invalid_language_too_short() {
        let mut request = create_valid_invoice_request();
        request.language = Some("e".to_string()); // Too short

        let result = request.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("language"));
    }

    #[test]
    fn test_invoice_request_invalid_language_too_long() {
        let mut request = create_valid_invoice_request();
        request.language = Some("english".to_string()); // Too long

        let result = request.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("language"));
    }

    #[test]
    fn test_invoice_request_end_before_start() {
        let mut request = create_valid_invoice_request();
        request.start_date = NaiveDate::from_ymd_opt(2024, 1, 31).unwrap();
        request.end_date = NaiveDate::from_ymd_opt(2024, 1, 1).unwrap();

        let result = request.validate_and_sanitize();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("end_date"));
    }

    #[test]
    fn test_invoice_request_same_start_end_date() {
        let mut request = create_valid_invoice_request();
        let same_date = NaiveDate::from_ymd_opt(2024, 1, 15).unwrap();
        request.start_date = same_date;
        request.end_date = same_date;

        let result = request.validate_and_sanitize();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("end_date"));
    }

    // Sanitization tests
    #[test]
    fn test_invoice_request_sanitization() {
        let mut request = InvoiceRequest {
            client_id: 1,
            start_date: NaiveDate::from_ymd_opt(2024, 1, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2024, 1, 31).unwrap(),
            language: Some("  EN  ".to_string()),
        };

        assert!(request.validate_and_sanitize().is_ok());

        // Check sanitization worked
        assert_eq!(request.language, Some("en".to_string()));
    }

    #[test]
    fn test_invoice_request_sanitization_empty_language() {
        let mut request = InvoiceRequest {
            client_id: 1,
            start_date: NaiveDate::from_ymd_opt(2024, 1, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2024, 1, 31).unwrap(),
            language: Some("   ".to_string()), // Only whitespace
        };

        assert!(request.validate_and_sanitize().is_ok());

        // Empty language should be converted to None
        assert_eq!(request.language, None);
    }

    // UpdateInvoiceStatusRequest tests
    #[test]
    fn test_status_update_valid() {
        let update = create_valid_status_update();
        assert!(update.validate().is_ok());
    }

    #[test]
    fn test_status_update_all_valid_statuses() {
        let valid_statuses = ["created", "sent", "paid", "overdue", "cancelled"];

        for status in valid_statuses.iter() {
            let mut update = UpdateInvoiceStatusRequest {
                status: status.to_string(),
                paid_date: None,
            };

            assert!(update.validate_and_sanitize().is_ok());
            assert_eq!(update.status, *status);
        }
    }

    #[test]
    fn test_status_update_invalid_status() {
        let mut update = UpdateInvoiceStatusRequest {
            status: "invalid_status".to_string(),
            paid_date: None,
        };

        let result = update.validate_and_sanitize();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("status"));
    }

    #[test]
    fn test_status_update_empty_status() {
        let mut update = UpdateInvoiceStatusRequest {
            status: "".to_string(),
            paid_date: None,
        };

        let result = update.validate_and_sanitize();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("status"));
    }

    #[test]
    fn test_status_update_status_too_long() {
        let mut update = UpdateInvoiceStatusRequest {
            status: "a".repeat(21), // Exceeds 20 character limit
            paid_date: None,
        };

        let result = update.validate_and_sanitize();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("status"));
    }

    #[test]
    fn test_status_update_sanitization() {
        let mut update = UpdateInvoiceStatusRequest {
            status: "  PAID  ".to_string(),
            paid_date: Some("2024-01-15".to_string()),
        };

        assert!(update.validate_and_sanitize().is_ok());

        // Check sanitization worked
        assert_eq!(update.status, "paid");
    }

    // DashboardQuery tests
    #[test]
    fn test_dashboard_query_valid() {
        let query = create_valid_dashboard_query();
        assert!(query.validate().is_ok());
    }

    #[test]
    fn test_dashboard_query_year_period() {
        let mut query = DashboardQuery {
            period: "year".to_string(),
            year: 2024,
            month: None, // Month not required for year period
        };

        assert!(query.validate_and_sanitize().is_ok());
        assert_eq!(query.period, "year");
    }

    #[test]
    fn test_dashboard_query_quarter_period() {
        let mut query = DashboardQuery {
            period: "quarter".to_string(),
            year: 2024,
            month: None, // Month not required for quarter period
        };

        assert!(query.validate_and_sanitize().is_ok());
        assert_eq!(query.period, "quarter");
    }

    #[test]
    fn test_dashboard_query_invalid_period() {
        let mut query = DashboardQuery {
            period: "week".to_string(), // Invalid period
            year: 2024,
            month: Some(1),
        };

        let result = query.validate_and_sanitize();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("period"));
    }

    #[test]
    fn test_dashboard_query_empty_period() {
        let mut query = DashboardQuery {
            period: "".to_string(),
            year: 2024,
            month: Some(1),
        };

        let result = query.validate_and_sanitize();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("period"));
    }

    #[test]
    fn test_dashboard_query_month_without_month_value() {
        let mut query = DashboardQuery {
            period: "month".to_string(),
            year: 2024,
            month: None, // Month required for month period
        };

        let result = query.validate_and_sanitize();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("month"));
    }

    #[test]
    fn test_dashboard_query_invalid_year() {
        let query = DashboardQuery {
            period: "year".to_string(),
            year: 1999, // Below minimum
            month: None,
        };

        let result = query.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("year"));
    }

    #[test]
    fn test_dashboard_query_year_too_high() {
        let query = DashboardQuery {
            period: "year".to_string(),
            year: 2101, // Above maximum
            month: None,
        };

        let result = query.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("year"));
    }

    #[test]
    fn test_dashboard_query_invalid_month() {
        let query = DashboardQuery {
            period: "month".to_string(),
            year: 2024,
            month: Some(13), // Invalid month
        };

        let result = query.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("month"));
    }

    #[test]
    fn test_dashboard_query_month_zero() {
        let query = DashboardQuery {
            period: "month".to_string(),
            year: 2024,
            month: Some(0), // Invalid month
        };

        let result = query.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("month"));
    }

    #[test]
    fn test_dashboard_query_sanitization() {
        let mut query = DashboardQuery {
            period: "  MONTH  ".to_string(),
            year: 2024,
            month: Some(1),
        };

        assert!(query.validate_and_sanitize().is_ok());

        // Check sanitization worked
        assert_eq!(query.period, "month");
    }

    // Boundary value tests
    #[test]
    fn test_dashboard_query_boundary_values() {
        // Test minimum valid values
        let mut query = DashboardQuery {
            period: "month".to_string(),
            year: 2000,     // Minimum year
            month: Some(1), // Minimum month
        };
        assert!(query.validate_and_sanitize().is_ok());

        // Test maximum valid values
        query.year = 2100; // Maximum year
        query.month = Some(12); // Maximum month
        assert!(query.validate_and_sanitize().is_ok());
    }

    // Serialization tests
    #[test]
    fn test_invoice_request_serialization() {
        let request = create_valid_invoice_request();
        let json = serde_json::to_string(&request).expect("Should serialize to JSON");

        assert!(json.contains("\"client_id\":1"));
        assert!(json.contains("\"start_date\":\"2024-01-01\""));
        assert!(json.contains("\"end_date\":\"2024-01-31\""));
        assert!(json.contains("\"language\":\"en\""));
    }

    #[test]
    fn test_invoice_request_deserialization() {
        let json = r#"{
            "client_id": 2,
            "start_date": "2024-02-01",
            "end_date": "2024-02-29",
            "language": "de"
        }"#;

        let request: InvoiceRequest =
            serde_json::from_str(json).expect("Should deserialize from JSON");

        assert_eq!(request.client_id, 2);
        assert_eq!(
            request.start_date,
            NaiveDate::from_ymd_opt(2024, 2, 1).unwrap()
        );
        assert_eq!(
            request.end_date,
            NaiveDate::from_ymd_opt(2024, 2, 29).unwrap()
        );
        assert_eq!(request.language, Some("de".to_string()));
    }

    #[test]
    fn test_invoice_request_deserialization_without_language() {
        let json = r#"{
            "client_id": 3,
            "start_date": "2024-03-01",
            "end_date": "2024-03-31"
        }"#;

        let request: InvoiceRequest =
            serde_json::from_str(json).expect("Should deserialize from JSON");

        assert_eq!(request.client_id, 3);
        assert_eq!(request.language, None);
    }

    // Edge case tests
    #[test]
    fn test_invoice_request_leap_year() {
        let request = InvoiceRequest {
            client_id: 1,
            start_date: NaiveDate::from_ymd_opt(2024, 2, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2024, 2, 29).unwrap(), // Leap year
            language: Some("en".to_string()),
        };

        assert!(request.validate().is_ok());
    }

    #[test]
    fn test_invoice_request_single_day() {
        let mut request = InvoiceRequest {
            client_id: 1,
            start_date: NaiveDate::from_ymd_opt(2024, 1, 15).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2024, 1, 16).unwrap(), // Next day
            language: Some("en".to_string()),
        };

        assert!(request.validate_and_sanitize().is_ok());
    }

    #[test]
    fn test_language_codes() {
        let valid_languages = ["en", "de", "fr", "es", "it"];

        for lang in valid_languages.iter() {
            let request = InvoiceRequest {
                client_id: 1,
                start_date: NaiveDate::from_ymd_opt(2024, 1, 1).unwrap(),
                end_date: NaiveDate::from_ymd_opt(2024, 1, 31).unwrap(),
                language: Some(lang.to_string()),
            };

            assert!(
                request.validate().is_ok(),
                "Language '{}' should be valid",
                lang
            );
        }
    }
}

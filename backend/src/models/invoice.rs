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

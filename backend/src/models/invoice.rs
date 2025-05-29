use crate::models::{client::Client, user_profile::UserProfile};
use crate::schema::invoices;
use chrono::NaiveDate;
use diesel::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct InvoiceRequest {
    pub client_id: i32,
    pub start_date: NaiveDate,
    pub end_date: NaiveDate,
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

#[derive(Debug, Serialize, Deserialize)]
pub struct UpdateInvoiceStatusRequest {
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

#[derive(Debug, Deserialize)]
pub struct DashboardQuery {
    pub period: String, // month, quarter, year
    pub year: i32,
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

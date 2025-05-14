use crate::models::{client::Client, user_profile::UserProfile};
use chrono::NaiveDate;
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct InvoiceRequest {
    pub client_id: i32,
    pub start_date: NaiveDate,
    pub end_date: NaiveDate,
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

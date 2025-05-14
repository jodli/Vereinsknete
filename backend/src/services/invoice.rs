use crate::models::invoice::{InvoiceRequest, InvoiceResponse, InvoiceSessionItem};
use crate::services::{client, pdf, user_profile};
use crate::DbPool;
use anyhow::{Context, Result};
use chrono::{NaiveTime, Utc};
use diesel::prelude::*;

pub fn generate_invoice(pool: &DbPool, invoice_req: InvoiceRequest) -> Result<Vec<u8>> {
    // Get user profile
    let user_profile = user_profile::get_profile(pool)
        .context("Failed to get user profile")?
        .context("User profile not found")?;

    // Get client
    let client_data = client::get_client_by_id(pool, invoice_req.client_id)
        .context("Failed to get client")?
        .context("Client not found")?;

    // Get sessions for the client in the date range
    use crate::schema::sessions::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");
    let session_data = sessions
        .filter(client_id.eq(invoice_req.client_id))
        .filter(date.ge(invoice_req.start_date.format("%Y-%m-%d").to_string()))
        .filter(date.le(invoice_req.end_date.format("%Y-%m-%d").to_string()))
        .load::<crate::models::session::Session>(&mut conn)
        .context("Failed to get sessions")?;

    // Calculate totals and create invoice items
    let mut total_hours = 0.0_f32;
    let hourly_rate = client_data.default_hourly_rate;

    let invoice_items: Vec<InvoiceSessionItem> = session_data
        .iter()
        .map(|session| {
            let start = NaiveTime::parse_from_str(&session.start_time, "%H:%M").unwrap_or_default();
            let end = NaiveTime::parse_from_str(&session.end_time, "%H:%M").unwrap_or_default();

            // Calculate duration in hours
            let duration_hours = if end < start {
                // Handle sessions that go past midnight
                (end + chrono::Duration::hours(24) - start).num_minutes() as f32 / 60.0
            } else {
                (end - start).num_minutes() as f32 / 60.0
            };

            total_hours += duration_hours;

            InvoiceSessionItem {
                name: session.name.clone(),
                date: session.date.clone(),
                start_time: session.start_time.clone(),
                end_time: session.end_time.clone(),
                duration_hours,
                amount: duration_hours * hourly_rate,
            }
        })
        .collect();

    let total_amount = total_hours * hourly_rate;

    // Create invoice response
    let invoice = InvoiceResponse {
        invoice_number: format!("{}", Utc::now().format("%Y%m%d%H%M")),
        date: Utc::now().format("%Y-%m-%d").to_string(),
        user_profile,
        client: client_data,
        sessions: invoice_items,
        total_hours,
        total_amount,
    };

    // Generate PDF
    pdf::generate_invoice_pdf(&invoice).context("Failed to generate PDF")
}

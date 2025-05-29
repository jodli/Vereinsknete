use crate::models::invoice::{
    DashboardMetrics, DashboardQuery, Invoice, InvoiceListItem, InvoiceRequest, InvoiceResponse,
    InvoiceSessionItem, NewInvoice, UpdateInvoiceStatusRequest,
};
use crate::services::{client, pdf, user_profile};
use crate::DbPool;
use anyhow::{Context, Result};
use chrono::{Datelike, NaiveTime, Utc};
use diesel::prelude::*;
use std::fs;

pub fn generate_and_save_invoice(
    pool: &DbPool,
    invoice_req: InvoiceRequest,
) -> Result<(Vec<u8>, i32, String)> {
    // Get user profile
    let user_profile = user_profile::get_profile(pool)
        .context("Failed to get user profile")?
        .context("User profile not found")?;

    // Get client
    let client_data = client::get_client_by_id(pool, invoice_req.client_id)
        .context("Failed to get client")?
        .context("Client not found")?;

    let current_year = Utc::now().year();

    // Get next sequence number for this year
    let next_sequence_number = get_next_sequence_number(pool, current_year)?;

    // Generate invoice number: YYYY-NNNN
    let invoice_number_str = format!("{}-{:04}", current_year, next_sequence_number);

    // Extract language preference
    let language = invoice_req.language.as_deref();

    // Get sessions for the client in the date range
    use crate::schema::sessions;

    let mut conn = pool.get().expect("Failed to get DB connection");
    let session_data = sessions::table
        .filter(sessions::client_id.eq(invoice_req.client_id))
        .filter(sessions::date.ge(invoice_req.start_date.format("%Y-%m-%d").to_string()))
        .filter(sessions::date.le(invoice_req.end_date.format("%Y-%m-%d").to_string()))
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

    let total_amount_calc = total_hours * hourly_rate;

    // Create invoice response for PDF generation
    let invoice_response = InvoiceResponse {
        invoice_number: invoice_number_str.clone(),
        date: Utc::now().format("%Y-%m-%d").to_string(),
        user_profile,
        client: client_data,
        sessions: invoice_items,
        total_hours,
        total_amount: total_amount_calc,
    };

    // Generate PDF
    let pdf_bytes =
        pdf::generate_invoice_pdf(&invoice_response, language).context("Failed to generate PDF")?;

    // Save PDF to file
    let pdf_filename = format!("invoice_{}.pdf", invoice_number_str);
    let pdf_path_str = format!("invoices/{}", pdf_filename);

    // Create directory if it doesn't exist
    std::fs::create_dir_all("invoices").context("Failed to create invoices directory")?;
    std::fs::write(&pdf_path_str, &pdf_bytes).context("Failed to save PDF file")?;

    // Calculate due date (30 days from today)
    let due_date_str = (Utc::now() + chrono::Duration::days(30))
        .format("%Y-%m-%d")
        .to_string();

    // Save invoice to database
    let new_invoice = NewInvoice {
        invoice_number: invoice_number_str.clone(),
        client_id: invoice_req.client_id,
        date: Utc::now().format("%Y-%m-%d").to_string(),
        total_amount: total_amount_calc,
        pdf_path: pdf_path_str.clone(),
        status: "created".to_string(),
        due_date: Some(due_date_str),
        year: current_year,
        sequence_number: next_sequence_number,
    };

    use crate::schema::invoices;
    diesel::insert_into(invoices::table)
        .values(&new_invoice)
        .execute(&mut conn)
        .context("Failed to save invoice")?;

    // Get the ID of the inserted invoice
    let invoice_id = invoices::table
        .order(invoices::id.desc())
        .select(invoices::id)
        .first::<i32>(&mut conn)
        .context("Failed to get invoice ID")?;

    Ok((pdf_bytes, invoice_id, invoice_number_str))
}

fn get_next_sequence_number(pool: &DbPool, target_year: i32) -> Result<i32> {
    use crate::schema::invoices;

    let mut conn = pool.get().expect("Failed to get DB connection");

    let max_sequence: Option<i32> = invoices::table
        .filter(invoices::year.eq(target_year))
        .select(diesel::dsl::max(invoices::sequence_number))
        .first(&mut conn)
        .optional()
        .context("Failed to get max sequence number")?
        .flatten();

    Ok(max_sequence.unwrap_or(0) + 1)
}

pub fn get_all_invoices(pool: &DbPool) -> Result<Vec<InvoiceListItem>> {
    use crate::schema::{clients, invoices};

    let mut conn = pool.get().expect("Failed to get DB connection");

    let results = invoices::table
        .inner_join(clients::table.on(invoices::client_id.eq(clients::id)))
        .select((
            invoices::id,
            invoices::invoice_number,
            clients::name,
            invoices::date,
            invoices::total_amount,
            invoices::status,
            invoices::due_date,
            invoices::paid_date,
            invoices::created_at,
        ))
        .order(invoices::created_at.desc())
        .load::<(
            i32,
            String,
            String,
            String,
            f32,
            String,
            Option<String>,
            Option<String>,
            chrono::NaiveDateTime,
        )>(&mut conn)
        .context("Failed to get invoices")?;

    let invoice_list = results
        .into_iter()
        .map(
            |(
                invoice_id,
                invoice_number_val,
                client_name,
                invoice_date,
                total_amount_val,
                invoice_status,
                due_date_val,
                paid_date_val,
                created_at_val,
            )| {
                InvoiceListItem {
                    id: invoice_id,
                    invoice_number: invoice_number_val,
                    client_name,
                    date: invoice_date,
                    total_amount: total_amount_val,
                    status: invoice_status,
                    due_date: due_date_val,
                    paid_date: paid_date_val,
                    created_at: created_at_val,
                }
            },
        )
        .collect();

    Ok(invoice_list)
}

pub fn update_invoice_status(
    pool: &DbPool,
    invoice_id: i32,
    status_req: UpdateInvoiceStatusRequest,
) -> Result<()> {
    use crate::schema::invoices;

    let mut conn = pool.get().expect("Failed to get DB connection");

    let update_result = diesel::update(invoices::table.filter(invoices::id.eq(invoice_id)))
        .set((
            invoices::status.eq(&status_req.status),
            invoices::paid_date.eq(&status_req.paid_date),
        ))
        .execute(&mut conn)
        .context("Failed to update invoice status")?;

    if update_result == 0 {
        anyhow::bail!("Invoice not found");
    }

    Ok(())
}

pub fn get_dashboard_metrics(pool: &DbPool, query: DashboardQuery) -> Result<DashboardMetrics> {
    use crate::schema::invoices;

    let mut conn = pool.get().expect("Failed to get DB connection");

    // Calculate date range based on period
    let (start_date, end_date) = match query.period.as_str() {
        "month" => {
            let month = query.month.unwrap_or(Utc::now().month() as i32);
            let start = format!("{}-{:02}-01", query.year, month);
            let end = if month == 12 {
                format!("{}-01-01", query.year + 1)
            } else {
                format!("{}-{:02}-01", query.year, month + 1)
            };
            (start, end)
        }
        "quarter" => {
            let quarter = ((query.month.unwrap_or(Utc::now().month() as i32) - 1) / 3) + 1;
            let start_month = (quarter - 1) * 3 + 1;
            let start = format!("{}-{:02}-01", query.year, start_month);
            let end = if quarter == 4 {
                format!("{}-01-01", query.year + 1)
            } else {
                format!("{}-{:02}-01", query.year, start_month + 3)
            };
            (start, end)
        }
        "year" => {
            let start = format!("{}-01-01", query.year);
            let end = format!("{}-01-01", query.year + 1);
            (start, end)
        }
        _ => anyhow::bail!("Invalid period. Use 'month', 'quarter', or 'year'"),
    };

    // Get paid invoices in period for revenue
    let paid_invoices = invoices::table
        .filter(invoices::status.eq("paid"))
        .filter(invoices::date.ge(&start_date))
        .filter(invoices::date.lt(&end_date))
        .select(invoices::total_amount)
        .load::<f32>(&mut conn)
        .context("Failed to get paid invoices")?;

    let total_revenue_period: f32 = paid_invoices.iter().sum();

    // Get pending invoices (sent but not paid)
    let pending_invoices = invoices::table
        .filter(invoices::status.eq("sent"))
        .select(invoices::total_amount)
        .load::<f32>(&mut conn)
        .context("Failed to get pending invoices")?;

    let pending_invoices_amount: f32 = pending_invoices.iter().sum();

    // Get invoice counts for all time
    let total_invoices_count = invoices::table
        .count()
        .get_result::<i64>(&mut conn)
        .context("Failed to get total invoice count")? as i32;

    let paid_invoices_count = invoices::table
        .filter(invoices::status.eq("paid"))
        .count()
        .get_result::<i64>(&mut conn)
        .context("Failed to get paid invoice count")? as i32;

    let pending_invoices_count = invoices::table
        .filter(invoices::status.eq("sent"))
        .count()
        .get_result::<i64>(&mut conn)
        .context("Failed to get pending invoice count")? as i32;

    Ok(DashboardMetrics {
        total_revenue_period,
        pending_invoices_amount,
        total_invoices_count,
        paid_invoices_count,
        pending_invoices_count,
    })
}

pub fn get_invoice_pdf(pool: &DbPool, invoice_id: i32) -> Result<(Vec<u8>, String)> {
    use crate::schema::invoices;

    let mut conn = pool.get().expect("Failed to get DB connection");

    // Get the invoice to find the PDF path and invoice number
    let invoice = invoices::table
        .filter(invoices::id.eq(invoice_id))
        .first::<Invoice>(&mut conn)
        .optional()
        .context("Failed to query invoice")?
        .context("Invoice not found")?;

    // Read the PDF file
    let pdf_bytes = fs::read(&invoice.pdf_path)
        .context(format!("Failed to read PDF file: {}", invoice.pdf_path))?;

    Ok((pdf_bytes, invoice.invoice_number))
}

pub fn delete_invoice(pool: &DbPool, invoice_id: i32) -> Result<()> {
    use crate::schema::invoices;

    let mut conn = pool.get().expect("Failed to get DB connection");

    // First get the invoice to get the PDF file name
    let invoice = invoices::table
        .find(invoice_id)
        .first::<Invoice>(&mut conn)
        .context("Failed to get invoice")?;

    // Delete the PDF file if it exists
    let pdf_path = format!("invoices/invoice_{}.pdf", invoice.invoice_number);
    if std::path::Path::new(&pdf_path).exists() {
        fs::remove_file(&pdf_path).context(format!("Failed to delete PDF file: {}", pdf_path))?;
    }

    // Delete the invoice record from database
    diesel::delete(invoices::table.find(invoice_id))
        .execute(&mut conn)
        .context("Failed to delete invoice")?;

    Ok(())
}

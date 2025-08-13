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

/// Generates and saves an invoice with PDF
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `invoice_req` - Invoice generation request with client ID and date range
///
/// # Returns
/// * `Result<(Vec<u8>, i32, String)>` - PDF bytes, invoice ID, and invoice number or error
pub fn generate_and_save_invoice(
    pool: &DbPool,
    invoice_req: InvoiceRequest,
) -> Result<(Vec<u8>, i32, String)> {
    // Business logic validation
    if invoice_req.client_id <= 0 {
        log::warn!(
            "Attempted to generate invoice with invalid client ID: {}",
            invoice_req.client_id
        );
        anyhow::bail!("Invalid client ID");
    }

    if invoice_req.end_date <= invoice_req.start_date {
        log::warn!(
            "Attempted to generate invoice with invalid date range: {} to {}",
            invoice_req.start_date,
            invoice_req.end_date
        );
        anyhow::bail!("End date must be after start date");
    }

    // Check if date range is reasonable (not more than 1 year)
    let date_diff = invoice_req.end_date - invoice_req.start_date;
    if date_diff.num_days() > 365 {
        log::warn!(
            "Attempted to generate invoice with date range longer than 1 year: {} days",
            date_diff.num_days()
        );
        anyhow::bail!("Date range cannot exceed 1 year");
    }

    log::info!(
        "Generating invoice for client {} from {} to {}",
        invoice_req.client_id,
        invoice_req.start_date,
        invoice_req.end_date
    );

    // Get user profile
    let user_profile = user_profile::get_profile(pool)
        .context("Failed to get user profile")?
        .context("User profile not found - please create a user profile first")?;

    log::debug!("Retrieved user profile: {}", user_profile.name);

    // Get client
    let client_data = client::get_client_by_id(pool, invoice_req.client_id)
        .context("Failed to get client")?
        .context("Client not found")?;

    log::debug!("Retrieved client: {}", client_data.name);

    let current_year = Utc::now().year();

    // Get next sequence number for this year
    let next_sequence_number = get_next_sequence_number(pool, current_year)?;

    // Generate invoice number: YYYY-NNNN
    let invoice_number_str = format!("{}-{:04}", current_year, next_sequence_number);

    log::info!("Generated invoice number: {}", invoice_number_str);

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

    if session_data.is_empty() {
        log::warn!(
            "No sessions found for client {} in date range {} to {}",
            invoice_req.client_id,
            invoice_req.start_date,
            invoice_req.end_date
        );
        anyhow::bail!("No sessions found in the specified date range");
    }

    log::debug!("Found {} sessions for invoice", session_data.len());

    // Calculate totals and create invoice items
    let mut total_hours = 0.0_f32;
    let hourly_rate = client_data.default_hourly_rate;

    if hourly_rate <= 0.0 {
        log::error!(
            "Client {} has invalid hourly rate: {}",
            client_data.id,
            hourly_rate
        );
        anyhow::bail!("Client has invalid hourly rate");
    }

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

    if total_amount_calc <= 0.0 {
        log::warn!(
            "Invoice would have zero or negative amount: {}",
            total_amount_calc
        );
        anyhow::bail!("Invoice amount must be positive");
    }

    log::info!(
        "Invoice totals: {} hours, {} amount",
        total_hours,
        total_amount_calc
    );

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
    log::debug!("Generating PDF for invoice {}", invoice_number_str);
    let pdf_bytes =
        pdf::generate_invoice_pdf(&invoice_response, language).context("Failed to generate PDF")?;

    log::debug!("Generated PDF with {} bytes", pdf_bytes.len());

    // Save PDF to file
    let pdf_filename = format!("invoice_{}.pdf", invoice_number_str);
    let pdf_path_str = format!("invoices/{}", pdf_filename);

    // Create directory if it doesn't exist
    std::fs::create_dir_all("invoices").context("Failed to create invoices directory")?;
    std::fs::write(&pdf_path_str, &pdf_bytes).context("Failed to save PDF file")?;

    log::debug!("Saved PDF to: {}", pdf_path_str);

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

    log::info!(
        "Successfully generated and saved invoice {} with ID: {}",
        invoice_number_str,
        invoice_id
    );

    Ok((pdf_bytes, invoice_id, invoice_number_str))
}

/// Gets the next sequence number for invoice numbering in a given year
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `target_year` - Year for which to get the next sequence number
///
/// # Returns
/// * `Result<i32>` - Next sequence number or error
fn get_next_sequence_number(pool: &DbPool, target_year: i32) -> Result<i32> {
    use crate::schema::invoices;

    // Validate year
    let current_year = Utc::now().year();
    if target_year < 2000 || target_year > current_year + 1 {
        log::warn!("Invalid target year for invoice sequence: {}", target_year);
        anyhow::bail!("Invalid year for invoice generation");
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::debug!("Getting next sequence number for year: {}", target_year);

    let max_sequence: Option<i32> = invoices::table
        .filter(invoices::year.eq(target_year))
        .select(diesel::dsl::max(invoices::sequence_number))
        .first(&mut conn)
        .optional()
        .context("Failed to get max sequence number")?
        .flatten();

    let next_sequence = max_sequence.unwrap_or(0) + 1;

    log::debug!(
        "Next sequence number for year {}: {}",
        target_year,
        next_sequence
    );

    Ok(next_sequence)
}

// NOTE: All public functions appear before the test module to satisfy clippy::items-after-test-module

/// Retrieves all invoices with client information
///
/// # Arguments
/// * `pool` - Database connection pool
///
/// # Returns
/// * `Result<Vec<InvoiceListItem>>` - List of invoices with client names or error
pub fn get_all_invoices(pool: &DbPool) -> Result<Vec<InvoiceListItem>> {
    use crate::schema::{clients, invoices};

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::debug!("Fetching all invoices with client information");

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

    let invoice_list: Vec<_> = results
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

    log::debug!("Successfully fetched {} invoices", invoice_list.len());

    Ok(invoice_list)
}

/// Updates the status of an existing invoice
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `invoice_id` - ID of the invoice to update
/// * `status_req` - New status and optional paid date
///
/// # Returns
/// * `Result<()>` - Success or error
pub fn update_invoice_status(
    pool: &DbPool,
    invoice_id: i32,
    status_req: UpdateInvoiceStatusRequest,
) -> Result<()> {
    use crate::schema::invoices;

    // Validate input
    if invoice_id <= 0 {
        log::warn!("Invalid invoice ID for status update: {}", invoice_id);
        anyhow::bail!("Invalid invoice ID");
    }

    // Validate status
    let valid_statuses = ["created", "sent", "paid", "overdue", "cancelled"];
    if !valid_statuses.contains(&status_req.status.as_str()) {
        log::warn!(
            "Invalid status for invoice {}: {}",
            invoice_id,
            status_req.status
        );
        anyhow::bail!("Invalid status. Must be one of: created, sent, paid, overdue, cancelled");
    }

    // Validate paid_date is provided when status is "paid"
    if status_req.status == "paid" && status_req.paid_date.is_none() {
        log::warn!(
            "Attempted to mark invoice {} as paid without paid_date",
            invoice_id
        );
        anyhow::bail!("Paid date is required when marking invoice as paid");
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::info!(
        "Updating invoice {} status to: {}",
        invoice_id,
        status_req.status
    );

    let update_result = diesel::update(invoices::table.filter(invoices::id.eq(invoice_id)))
        .set((
            invoices::status.eq(&status_req.status),
            invoices::paid_date.eq(&status_req.paid_date),
        ))
        .execute(&mut conn)
        .context("Failed to update invoice status")?;

    if update_result == 0 {
        log::warn!("Attempted to update non-existent invoice: {}", invoice_id);
        anyhow::bail!("Invoice not found");
    }

    log::info!(
        "Successfully updated invoice {} status to: {}",
        invoice_id,
        status_req.status
    );

    Ok(())
}

/// Retrieves dashboard metrics for a specified period
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `query` - Dashboard query with period, year, and optional month
///
/// # Returns
/// * `Result<DashboardMetrics>` - Dashboard metrics or error
pub fn get_dashboard_metrics(pool: &DbPool, query: DashboardQuery) -> Result<DashboardMetrics> {
    use crate::schema::invoices;

    // Validate input
    let current_year = Utc::now().year();
    if query.year < 2000 || query.year > current_year + 1 {
        log::warn!("Invalid year for dashboard metrics: {}", query.year);
        anyhow::bail!("Invalid year");
    }

    if let Some(month) = query.month {
        if !(1..=12).contains(&month) {
            log::warn!("Invalid month for dashboard metrics: {}", month);
            anyhow::bail!("Invalid month");
        }
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::debug!(
        "Calculating dashboard metrics for period: {} year: {} month: {:?}",
        query.period,
        query.year,
        query.month
    );

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
        _ => {
            log::warn!("Invalid period for dashboard metrics: {}", query.period);
            anyhow::bail!("Invalid period. Use 'month', 'quarter', or 'year'");
        }
    };

    log::debug!("Date range for metrics: {} to {}", start_date, end_date);

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

    let metrics = DashboardMetrics {
        total_revenue_period,
        pending_invoices_amount,
        total_invoices_count,
        paid_invoices_count,
        pending_invoices_count,
    };

    log::debug!(
        "Dashboard metrics calculated: revenue={}, pending={}, total={}, paid={}, pending_count={}",
        metrics.total_revenue_period,
        metrics.pending_invoices_amount,
        metrics.total_invoices_count,
        metrics.paid_invoices_count,
        metrics.pending_invoices_count
    );

    Ok(metrics)
}

/// Retrieves the PDF file for a specific invoice
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `invoice_id` - ID of the invoice
///
/// # Returns
/// * `Result<(Vec<u8>, String)>` - PDF bytes and invoice number or error
pub fn get_invoice_pdf(pool: &DbPool, invoice_id: i32) -> Result<(Vec<u8>, String)> {
    use crate::schema::invoices;

    // Validate input
    if invoice_id <= 0 {
        log::warn!("Invalid invoice ID for PDF retrieval: {}", invoice_id);
        anyhow::bail!("Invalid invoice ID");
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::debug!("Retrieving PDF for invoice: {}", invoice_id);

    // Get the invoice to find the PDF path and invoice number
    let invoice = invoices::table
        .filter(invoices::id.eq(invoice_id))
        .first::<Invoice>(&mut conn)
        .optional()
        .context("Failed to query invoice")?
        .context("Invoice not found")?;

    log::debug!(
        "Found invoice {}, PDF path: {}",
        invoice.invoice_number,
        invoice.pdf_path
    );

    // Check if PDF file exists
    if !std::path::Path::new(&invoice.pdf_path).exists() {
        log::error!(
            "PDF file not found for invoice {}: {}",
            invoice.invoice_number,
            invoice.pdf_path
        );
        anyhow::bail!("PDF file not found");
    }

    // Read the PDF file
    let pdf_bytes = fs::read(&invoice.pdf_path)
        .context(format!("Failed to read PDF file: {}", invoice.pdf_path))?;

    log::debug!("Successfully read PDF file: {} bytes", pdf_bytes.len());

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

#[cfg(test)]
mod tests {
    use super::*;
    use chrono::{NaiveDate, Utc};
    use diesel_migrations::{embed_migrations, EmbeddedMigrations, MigrationHarness};
    use std::sync::atomic::{AtomicU32, Ordering};

    const MIGRATIONS: EmbeddedMigrations = embed_migrations!("migrations");
    static DB_COUNTER: AtomicU32 = AtomicU32::new(0);

    fn setup_pool() -> DbPool {
        let count = DB_COUNTER.fetch_add(1, Ordering::SeqCst) + 1;
        let db_name = format!(
            "file:invoice_service_test_{}?mode=memory&cache=shared",
            count
        );
        let manager = diesel::r2d2::ConnectionManager::<SqliteConnection>::new(db_name);
        let pool = diesel::r2d2::Pool::builder()
            .max_size(1)
            .build(manager)
            .unwrap();
        {
            let mut conn = pool.get().unwrap();
            conn.run_pending_migrations(MIGRATIONS).unwrap();
        }
        pool
    }

    // Helpers to insert required entities directly (bypassing services not under test focus)
    fn insert_profile(pool: &DbPool) -> i32 {
        use crate::schema::user_profile;
        #[derive(diesel::Insertable)]
        #[diesel(table_name = crate::schema::user_profile)]
        struct TestProfile {
            name: String,
            address: String,
            tax_id: Option<String>,
            bank_details: Option<String>,
        }
        let p = TestProfile {
            name: "Alice".into(),
            address: "Addr".into(),
            tax_id: None,
            bank_details: Some("Bank {invoice_number}".into()),
        };
        let mut conn = pool.get().unwrap();
        diesel::insert_into(user_profile::table)
            .values(&p)
            .execute(&mut conn)
            .unwrap();
        use crate::schema::user_profile::dsl::*;
        user_profile
            .order(id.desc())
            .select(id)
            .first(&mut conn)
            .unwrap()
    }

    fn insert_client(pool: &DbPool, name_val: &str, rate: f32) -> i32 {
        use crate::schema::clients;
        #[derive(diesel::Insertable)]
        #[diesel(table_name = crate::schema::clients)]
        struct TestClient {
            name: String,
            address: String,
            contact_person: Option<String>,
            default_hourly_rate: f32,
        }
        let c = TestClient {
            name: name_val.into(),
            address: "Addr".into(),
            contact_person: None,
            default_hourly_rate: rate,
        };
        let mut conn = pool.get().unwrap();
        diesel::insert_into(clients::table)
            .values(&c)
            .execute(&mut conn)
            .unwrap();
        use crate::schema::clients::dsl::*;
        clients
            .order(id.desc())
            .select(id)
            .first(&mut conn)
            .unwrap()
    }

    fn insert_session(pool: &DbPool, client_id: i32, date: &str, start: &str, end: &str) {
        use crate::schema::sessions;
        #[derive(diesel::Insertable)]
        #[diesel(table_name = crate::schema::sessions)]
        struct TestSession {
            client_id: i32,
            name: String,
            date: String,
            start_time: String,
            end_time: String,
            created_at: String,
        }
        let s = TestSession {
            client_id,
            name: "Work".into(),
            date: date.into(),
            start_time: start.into(),
            end_time: end.into(),
            created_at: format!("{}T00:00:00", date),
        };
        let mut conn = pool.get().unwrap();
        diesel::insert_into(sessions::table)
            .values(&s)
            .execute(&mut conn)
            .unwrap();
    }

    fn list_invoices(pool: &DbPool) -> Vec<InvoiceListItem> {
        get_all_invoices(pool).unwrap()
    }

    #[test]
    fn generate_invoice_success_and_sequence() {
        let pool = setup_pool();
        insert_profile(&pool);
        let client_id = insert_client(&pool, "Acme", 100.0);
        insert_session(&pool, client_id, "2025-01-10", "09:00", "11:00"); // 2h -> 200
        let req = InvoiceRequest {
            client_id,
            start_date: NaiveDate::from_ymd_opt(2025, 1, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2025, 1, 31).unwrap(),
            language: None,
        };
        let (_pdf, _id, number) = generate_and_save_invoice(&pool, req).unwrap();
        assert!(number.ends_with("0001"));
        // Second invoice same year increments sequence
        insert_session(&pool, client_id, "2025-01-15", "10:00", "11:00");
        let req2 = InvoiceRequest {
            client_id,
            start_date: NaiveDate::from_ymd_opt(2025, 1, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2025, 12, 31).unwrap(),
            language: None,
        };
        let (_pdf2, _id2, number2) = generate_and_save_invoice(&pool, req2).unwrap();
        assert!(number2.ends_with("0002"));
        assert_eq!(list_invoices(&pool).len(), 2);
    }

    #[test]
    fn generate_invoice_no_sessions_fails() {
        let pool = setup_pool();
        insert_profile(&pool);
        let client_id = insert_client(&pool, "Acme", 100.0);
        let req = InvoiceRequest {
            client_id,
            start_date: NaiveDate::from_ymd_opt(2025, 2, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2025, 2, 28).unwrap(),
            language: None,
        };
        let err = generate_and_save_invoice(&pool, req).unwrap_err();
        assert!(err.to_string().contains("No sessions"));
    }

    #[test]
    fn generate_invoice_invalid_date_range_fails() {
        let pool = setup_pool();
        insert_profile(&pool);
        let client_id = insert_client(&pool, "Acme", 100.0);
        let req = InvoiceRequest {
            client_id,
            start_date: NaiveDate::from_ymd_opt(2025, 3, 10).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2025, 3, 1).unwrap(),
            language: None,
        };
        let err = generate_and_save_invoice(&pool, req).unwrap_err();
        assert!(err
            .to_string()
            .contains("End date must be after start date"));
    }

    #[test]
    fn generate_invoice_invalid_rate_fails() {
        let pool = setup_pool();
        insert_profile(&pool);
        let client_id = insert_client(&pool, "Acme", 0.0); // invalid hourly rate (<=0)
        insert_session(&pool, client_id, "2025-01-10", "09:00", "10:00");
        let req = InvoiceRequest {
            client_id,
            start_date: NaiveDate::from_ymd_opt(2025, 1, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2025, 1, 31).unwrap(),
            language: None,
        };
        let err = generate_and_save_invoice(&pool, req).unwrap_err();
        assert!(err.to_string().contains("invalid hourly rate"));
    }

    #[test]
    fn update_invoice_status_flow_and_validation() {
        let pool = setup_pool();
        insert_profile(&pool);
        let client_id = insert_client(&pool, "Acme", 100.0);
        insert_session(&pool, client_id, "2025-01-10", "09:00", "11:00");
        let req = InvoiceRequest {
            client_id,
            start_date: NaiveDate::from_ymd_opt(2025, 1, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2025, 1, 31).unwrap(),
            language: None,
        };
        let (_pdf, id, _num) = generate_and_save_invoice(&pool, req).unwrap();
        // Invalid status
        let bad = update_invoice_status(
            &pool,
            id,
            UpdateInvoiceStatusRequest {
                status: "weird".into(),
                paid_date: None,
            },
        )
        .unwrap_err();
        assert!(bad.to_string().contains("Invalid status"));
        // Paid without date
        let bad2 = update_invoice_status(
            &pool,
            id,
            UpdateInvoiceStatusRequest {
                status: "paid".into(),
                paid_date: None,
            },
        )
        .unwrap_err();
        assert!(bad2.to_string().contains("Paid date is required"));
        // Valid transition to sent
        update_invoice_status(
            &pool,
            id,
            UpdateInvoiceStatusRequest {
                status: "sent".into(),
                paid_date: None,
            },
        )
        .unwrap();
        // Mark paid with date
        update_invoice_status(
            &pool,
            id,
            UpdateInvoiceStatusRequest {
                status: "paid".into(),
                paid_date: Some(Utc::now().format("%Y-%m-%d").to_string()),
            },
        )
        .unwrap();
    }

    #[test]
    fn dashboard_metrics_basic() {
        let pool = setup_pool();
        insert_profile(&pool);
        let client_id = insert_client(&pool, "Acme", 100.0);
        insert_session(&pool, client_id, "2025-01-10", "09:00", "10:00"); // 1h -> 100
        let req = InvoiceRequest {
            client_id,
            start_date: NaiveDate::from_ymd_opt(2025, 1, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2025, 1, 31).unwrap(),
            language: None,
        };
        let (_pdf, id, _num) = generate_and_save_invoice(&pool, req).unwrap();
        // Mark as paid so revenue counts
        update_invoice_status(
            &pool,
            id,
            UpdateInvoiceStatusRequest {
                status: "paid".into(),
                paid_date: Some(Utc::now().format("%Y-%m-%d").to_string()),
            },
        )
        .unwrap();
        let metrics = get_dashboard_metrics(
            &pool,
            DashboardQuery {
                period: "year".into(),
                year: Utc::now().year(),
                month: None,
            },
        )
        .unwrap();
        assert!(metrics.total_revenue_period >= 100.0);
        assert!(metrics.total_invoices_count >= 1);
    }
}

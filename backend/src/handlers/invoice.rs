use crate::errors::AppError;
use crate::models::invoice::{InvoiceRequest, UpdateInvoiceStatusRequest, DashboardQuery};
use crate::services::invoice as invoice_service;
use crate::DbPool;
use actix_web::{get, patch, post, web, Error, HttpResponse};
use base64::Engine;

#[post("/invoices/generate")]
async fn generate_invoice(
    pool: web::Data<DbPool>,
    invoice_req: web::Json<InvoiceRequest>,
) -> Result<HttpResponse, Error> {
    let (pdf_bytes, invoice_id, invoice_number) =
        web::block(move || invoice_service::generate_and_save_invoice(&pool, invoice_req.into_inner()))
            .await?
            .map_err(|e| {
                eprintln!("Error generating invoice: {:?}", e);
                AppError::InternalServerError(format!("Error generating invoice: {}", e))
            })?;

    Ok(HttpResponse::Ok()
        .content_type("application/pdf")
        .append_header((
            "Content-Disposition",
            "attachment; filename=\"invoice.pdf\"",
        ))
        .json(serde_json::json!({
            "invoice_id": invoice_id,
            "invoice_number": invoice_number,
            "pdf_bytes": base64::engine::general_purpose::STANDARD.encode(&pdf_bytes)
        })))
}

#[get("/invoices")]
async fn get_invoices(pool: web::Data<DbPool>) -> Result<HttpResponse, Error> {
    let invoices = web::block(move || invoice_service::get_all_invoices(&pool))
        .await?
        .map_err(|e| {
            eprintln!("Error getting invoices: {:?}", e);
            AppError::InternalServer(format!("Error getting invoices: {}", e))
        })?;

    Ok(HttpResponse::Ok().json(invoices))
}

#[patch("/invoices/{id}/status")]
async fn update_invoice_status(
    pool: web::Data<DbPool>,
    path: web::Path<i32>,
    status_req: web::Json<UpdateInvoiceStatusRequest>,
) -> Result<HttpResponse, Error> {
    let invoice_id = path.into_inner();

    web::block(move || {
        invoice_service::update_invoice_status(&pool, invoice_id, status_req.into_inner())
    })
    .await?
    .map_err(|e| {
        eprintln!("Error updating invoice status: {:?}", e);
        AppError::InternalServer(format!("Error updating invoice status: {}", e))
    })?;

    Ok(HttpResponse::Ok().json(serde_json::json!({"success": true})))
}

#[get("/dashboard/metrics")]
async fn get_dashboard_metrics(
    pool: web::Data<DbPool>,
    query: web::Query<DashboardQuery>,
) -> Result<HttpResponse, Error> {
    let metrics = web::block(move || {
        invoice_service::get_dashboard_metrics(&pool, query.into_inner())
    })
    .await?
    .map_err(|e| {
        eprintln!("Error getting dashboard metrics: {:?}", e);
        AppError::InternalServerError(format!("Error getting dashboard metrics: {}", e))
    })?;

    Ok(HttpResponse::Ok().json(metrics))
}

#[get("/invoices/{id}/pdf")]
async fn download_invoice_pdf(
    pool: web::Data<DbPool>,
    path: web::Path<i32>,
) -> Result<HttpResponse, Error> {
    let invoice_id = path.into_inner();

    let (pdf_bytes, invoice_number) = web::block(move || invoice_service::get_invoice_pdf(&pool, invoice_id))
        .await?
        .map_err(|e| {
            eprintln!("Error getting invoice PDF: {:?}", e);
            AppError::InternalServerError(format!("Error getting invoice PDF: {}", e))
        })?;

    Ok(HttpResponse::Ok()
        .content_type("application/pdf")
        .append_header((
            "Content-Disposition",
            format!("attachment; filename=\"invoice_{}.pdf\"", invoice_number),
        ))
        .body(pdf_bytes))
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(generate_invoice)
        .service(get_invoices)
        .service(update_invoice_status)
        .service(get_dashboard_metrics)
        .service(download_invoice_pdf);
}

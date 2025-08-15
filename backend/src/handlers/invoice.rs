use crate::config::Config;
use crate::errors::AppError;
use crate::models::invoice::{DashboardQuery, InvoiceRequest, UpdateInvoiceStatusRequest};
use crate::services::invoice as invoice_service;
use crate::DbPool;
use actix_web::{delete, get, patch, post, web, Error, HttpMessage, HttpRequest, HttpResponse};
use base64::Engine;
use serde_json::json;

fn get_request_id(req: &HttpRequest) -> String {
    req.extensions()
        .get::<String>()
        .cloned()
        .unwrap_or_else(|| "unknown".to_string())
}

#[post("/invoices/generate")]
async fn generate_invoice(
    pool: web::Data<DbPool>,
    config: web::Data<Config>,
    mut invoice_req: web::Json<InvoiceRequest>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "generate_invoice",
            "client_id": invoice_req.client_id,
            "start_date": invoice_req.start_date,
            "end_date": invoice_req.end_date,
            "message": "Generating invoice"
        })
    );

    // Validate and sanitize input
    if let Err(errors) = invoice_req.validate_and_sanitize() {
        log::warn!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "generate_invoice",
                "validation_errors": format!("{:?}", errors),
                "message": "Invoice request validation failed"
            })
        );
        return Err(AppError::Validation(format!("Validation failed: {:?}", errors)).into());
    }

    let invoice_dir = config.invoice_dir.clone();
    let (pdf_bytes, invoice_id, invoice_number) = web::block(move || {
        invoice_service::generate_and_save_invoice(&pool, invoice_req.into_inner(), &invoice_dir)
    })
    .await?
    .map_err(|e| {
        log::error!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "generate_invoice",
                "error": e.to_string(),
                "message": "Error generating invoice"
            })
        );
        AppError::InternalServer(format!("Error generating invoice: {}", e))
    })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "generate_invoice",
            "invoice_id": invoice_id,
            "invoice_number": invoice_number,
            "message": "Invoice generated successfully"
        })
    );

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
async fn get_invoices(pool: web::Data<DbPool>, req: HttpRequest) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_invoices",
            "message": "Fetching all invoices"
        })
    );

    let invoices = web::block(move || invoice_service::get_all_invoices(&pool))
        .await?
        .map_err(|e| {
            log::error!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "get_invoices",
                    "error": e.to_string(),
                    "message": "Database error while fetching invoices"
                })
            );
            AppError::InternalServer(format!("Error getting invoices: {}", e))
        })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_invoices",
            "count": invoices.len(),
            "message": "Successfully fetched invoices"
        })
    );

    Ok(HttpResponse::Ok().json(invoices))
}

#[patch("/invoices/{id}/status")]
async fn update_invoice_status(
    pool: web::Data<DbPool>,
    path: web::Path<i32>,
    mut status_req: web::Json<UpdateInvoiceStatusRequest>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    let invoice_id = path.into_inner();

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "update_invoice_status",
            "invoice_id": invoice_id,
            "new_status": status_req.status,
            "message": "Updating invoice status"
        })
    );

    // Validate and sanitize input
    if let Err(errors) = status_req.validate_and_sanitize() {
        log::warn!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "update_invoice_status",
                "invoice_id": invoice_id,
                "validation_errors": format!("{:?}", errors),
                "message": "Invoice status validation failed"
            })
        );
        return Err(AppError::Validation(format!("Validation failed: {:?}", errors)).into());
    }

    web::block(move || {
        invoice_service::update_invoice_status(&pool, invoice_id, status_req.into_inner())
    })
    .await?
    .map_err(|e| {
        log::error!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "update_invoice_status",
                "invoice_id": invoice_id,
                "error": e.to_string(),
                "message": "Database error while updating invoice status"
            })
        );
        AppError::InternalServer(format!("Error updating invoice status: {}", e))
    })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "update_invoice_status",
            "invoice_id": invoice_id,
            "message": "Invoice status updated successfully"
        })
    );

    Ok(HttpResponse::Ok().json(serde_json::json!({"success": true})))
}

#[get("/dashboard/metrics")]
async fn get_dashboard_metrics(
    pool: web::Data<DbPool>,
    mut query: web::Query<DashboardQuery>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_dashboard_metrics",
            "period": query.period,
            "year": query.year,
            "month": query.month,
            "message": "Fetching dashboard metrics"
        })
    );

    // Validate and sanitize input
    if let Err(errors) = query.validate_and_sanitize() {
        log::warn!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "get_dashboard_metrics",
                "validation_errors": format!("{:?}", errors),
                "message": "Dashboard query validation failed"
            })
        );
        return Err(AppError::Validation(format!("Validation failed: {:?}", errors)).into());
    }

    let metrics =
        web::block(move || invoice_service::get_dashboard_metrics(&pool, query.into_inner()))
            .await?
            .map_err(|e| {
                log::error!(
                    target: "business_logic",
                    "{}",
                    json!({
                        "request_id": request_id,
                        "action": "get_dashboard_metrics",
                        "error": e.to_string(),
                        "message": "Database error while fetching dashboard metrics"
                    })
                );
                AppError::InternalServer(format!("Error getting dashboard metrics: {}", e))
            })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_dashboard_metrics",
            "message": "Successfully fetched dashboard metrics"
        })
    );

    Ok(HttpResponse::Ok().json(metrics))
}

#[get("/invoices/{id}/pdf")]
async fn download_invoice_pdf(
    pool: web::Data<DbPool>,
    config: web::Data<Config>,
    path: web::Path<i32>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    let invoice_id = path.into_inner();

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "download_invoice_pdf",
            "invoice_id": invoice_id,
            "message": "Downloading invoice PDF"
        })
    );

    let invoice_dir = config.invoice_dir.clone();
    let (pdf_bytes, invoice_number) =
        web::block(move || invoice_service::get_invoice_pdf(&pool, invoice_id, &invoice_dir))
            .await?
            .map_err(|e| {
                log::error!(
                    target: "business_logic",
                    "{}",
                    json!({
                        "request_id": request_id,
                        "action": "download_invoice_pdf",
                        "invoice_id": invoice_id,
                        "error": e.to_string(),
                        "message": "Error getting invoice PDF"
                    })
                );
                AppError::InternalServer(format!("Error getting invoice PDF: {}", e))
            })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "download_invoice_pdf",
            "invoice_id": invoice_id,
            "invoice_number": invoice_number,
            "message": "Invoice PDF downloaded successfully"
        })
    );

    Ok(HttpResponse::Ok()
        .content_type("application/pdf")
        .append_header((
            "Content-Disposition",
            format!("attachment; filename=\"invoice_{}.pdf\"", invoice_number),
        ))
        .body(pdf_bytes))
}

#[delete("/invoices/{id}")]
async fn delete_invoice(
    pool: web::Data<DbPool>,
    config: web::Data<Config>,
    path: web::Path<i32>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    let invoice_id = path.into_inner();

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "delete_invoice",
            "invoice_id": invoice_id,
            "message": "Deleting invoice"
        })
    );

    let invoice_dir = config.invoice_dir.clone();
    web::block(move || invoice_service::delete_invoice(&pool, invoice_id, &invoice_dir))
        .await?
        .map_err(|e| {
            log::error!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "delete_invoice",
                    "invoice_id": invoice_id,
                    "error": e.to_string(),
                    "message": "Database error while deleting invoice"
                })
            );
            AppError::InternalServer(format!("Error deleting invoice: {}", e))
        })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "delete_invoice",
            "invoice_id": invoice_id,
            "message": "Invoice deleted successfully"
        })
    );

    Ok(HttpResponse::Ok().json(serde_json::json!({"success": true})))
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(generate_invoice)
        .service(get_invoices)
        .service(update_invoice_status)
        .service(get_dashboard_metrics)
        .service(download_invoice_pdf)
        .service(delete_invoice);
}

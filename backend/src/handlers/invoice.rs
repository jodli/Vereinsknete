use actix_web::{web, post, HttpResponse, Error};
use crate::DbPool;
use crate::models::invoice::InvoiceRequest;
use crate::services::invoice as invoice_service;

#[post("/invoices/generate")]
async fn generate_invoice(
    pool: web::Data<DbPool>,
    invoice_req: web::Json<InvoiceRequest>,
) -> Result<HttpResponse, Error> {
    let pdf_bytes = web::block(move || {
        invoice_service::generate_invoice(&pool, invoice_req.into_inner())
    })
    .await?
    .map_err(|e| {
        eprintln!("Error generating invoice: {:?}", e);
        HttpResponse::InternalServerError().json(format!("Error generating invoice: {}", e))
    })?;

    Ok(HttpResponse::Ok()
        .content_type("application/pdf")
        .append_header(("Content-Disposition", "attachment; filename=\"invoice.pdf\""))
        .body(pdf_bytes))
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(generate_invoice);
}

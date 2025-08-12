use crate::errors::AppError;
use crate::models::client::{NewClient, UpdateClient};
use crate::services::client as client_service;
use crate::DbPool;
use actix_web::{delete, get, post, put, web, Error, HttpMessage, HttpRequest, HttpResponse};
use serde_json::json;

fn get_request_id(req: &HttpRequest) -> String {
    req.extensions()
        .get::<String>()
        .cloned()
        .unwrap_or_else(|| "unknown".to_string())
}

#[get("/clients")]
async fn get_clients(pool: web::Data<DbPool>, req: HttpRequest) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_clients",
            "message": "Fetching all clients"
        })
    );

    let clients = web::block(move || client_service::get_all_clients(&pool))
        .await?
        .map_err(|e| {
            log::error!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "get_clients",
                    "error": e.to_string(),
                    "message": "Database error while fetching clients"
                })
            );
            AppError::Database(e)
        })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_clients",
            "count": clients.len(),
            "message": "Successfully fetched clients"
        })
    );

    Ok(HttpResponse::Ok().json(clients))
}

#[get("/clients/{id}")]
async fn get_client(
    pool: web::Data<DbPool>,
    client_id: web::Path<i32>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    let client_id = client_id.into_inner();

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_client",
            "client_id": client_id,
            "message": "Fetching client details"
        })
    );

    let client = web::block(move || client_service::get_client_by_id(&pool, client_id))
        .await?
        .map_err(|e| {
            log::error!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "get_client",
                    "client_id": client_id,
                    "error": e.to_string(),
                    "message": "Database error while fetching client"
                })
            );
            AppError::Database(e)
        })?;

    match client {
        Some(client) => {
            log::info!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "get_client",
                    "client_id": client_id,
                    "message": "Client found successfully"
                })
            );
            Ok(HttpResponse::Ok().json(client))
        }
        None => {
            log::warn!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "get_client",
                    "client_id": client_id,
                    "message": "Client not found"
                })
            );
            Ok(HttpResponse::NotFound().json("Client not found"))
        }
    }
}

#[post("/clients")]
async fn create_client(
    pool: web::Data<DbPool>,
    mut client_data: web::Json<NewClient>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "create_client",
            "client_name": client_data.name,
            "message": "Creating new client"
        })
    );

    // Validate and sanitize input
    if let Err(errors) = client_data.validate_and_sanitize() {
        log::warn!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "create_client",
                "validation_errors": format!("{:?}", errors),
                "message": "Client validation failed"
            })
        );
        return Err(AppError::Validation(format!("Validation failed: {:?}", errors)).into());
    }

    let client = web::block(move || client_service::create_client(&pool, client_data.into_inner()))
        .await?
        .map_err(|e| {
            log::error!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "create_client",
                    "error": e.to_string(),
                    "message": "Database error while creating client"
                })
            );
            AppError::Database(e)
        })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "create_client",
            "client_id": client.id,
            "message": "Client created successfully"
        })
    );

    Ok(HttpResponse::Created().json(client))
}

#[put("/clients/{id}")]
async fn update_client(
    pool: web::Data<DbPool>,
    client_id: web::Path<i32>,
    mut client_data: web::Json<UpdateClient>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    let client_id = client_id.into_inner();

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "update_client",
            "client_id": client_id,
            "message": "Updating client"
        })
    );

    // Validate and sanitize input
    if let Err(errors) = client_data.validate_and_sanitize() {
        log::warn!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "update_client",
                "client_id": client_id,
                "validation_errors": format!("{:?}", errors),
                "message": "Client validation failed"
            })
        );
        return Err(AppError::Validation(format!("Validation failed: {:?}", errors)).into());
    }

    let client = web::block(move || {
        client_service::update_client(&pool, client_id, client_data.into_inner())
    })
    .await?
    .map_err(|e| {
        log::error!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "update_client",
                "client_id": client_id,
                "error": e.to_string(),
                "message": "Database error while updating client"
            })
        );
        AppError::Database(e)
    })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "update_client",
            "client_id": client_id,
            "message": "Client updated successfully"
        })
    );

    Ok(HttpResponse::Ok().json(client))
}

#[delete("/clients/{id}")]
async fn delete_client(
    pool: web::Data<DbPool>,
    client_id: web::Path<i32>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    let client_id = client_id.into_inner();

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "delete_client",
            "client_id": client_id,
            "message": "Deleting client"
        })
    );

    let deleted = web::block(move || client_service::delete_client(&pool, client_id))
        .await?
        .map_err(|e| {
            log::error!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "delete_client",
                    "client_id": client_id,
                    "error": e.to_string(),
                    "message": "Database error while deleting client"
                })
            );
            AppError::Database(e)
        })?;

    if deleted > 0 {
        log::info!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "delete_client",
                "client_id": client_id,
                "message": "Client deleted successfully"
            })
        );
        Ok(HttpResponse::NoContent().finish())
    } else {
        log::warn!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "delete_client",
                "client_id": client_id,
                "message": "Client not found for deletion"
            })
        );
        Ok(HttpResponse::NotFound().json("Client not found"))
    }
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(get_clients)
        .service(get_client)
        .service(create_client)
        .service(update_client)
        .service(delete_client);
}

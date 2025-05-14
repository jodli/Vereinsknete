use actix_web::{web, get, post, put, delete, HttpResponse, Error};
use crate::DbPool;
use crate::models::client::{NewClient, UpdateClient};
use crate::services::client as client_service;

#[get("/clients")]
async fn get_clients(pool: web::Data<DbPool>) -> Result<HttpResponse, Error> {
    let clients = web::block(move || {
        client_service::get_all_clients(&pool)
    })
    .await?
    .map_err(|e| {
        eprintln!("Error getting clients: {:?}", e);
        HttpResponse::InternalServerError().finish()
    })?;

    Ok(HttpResponse::Ok().json(clients))
}

#[get("/clients/{id}")]
async fn get_client(
    pool: web::Data<DbPool>,
    client_id: web::Path<i32>,
) -> Result<HttpResponse, Error> {
    let client = web::block(move || {
        client_service::get_client_by_id(&pool, client_id.into_inner())
    })
    .await?
    .map_err(|e| {
        eprintln!("Error getting client: {:?}", e);
        HttpResponse::InternalServerError().finish()
    })?;

    match client {
        Some(client) => Ok(HttpResponse::Ok().json(client)),
        None => Ok(HttpResponse::NotFound().json("Client not found"))
    }
}

#[post("/clients")]
async fn create_client(
    pool: web::Data<DbPool>,
    client_data: web::Json<NewClient>,
) -> Result<HttpResponse, Error> {
    let client = web::block(move || {
        client_service::create_client(&pool, client_data.into_inner())
    })
    .await?
    .map_err(|e| {
        eprintln!("Error creating client: {:?}", e);
        HttpResponse::InternalServerError().finish()
    })?;

    Ok(HttpResponse::Created().json(client))
}

#[put("/clients/{id}")]
async fn update_client(
    pool: web::Data<DbPool>,
    client_id: web::Path<i32>,
    client_data: web::Json<UpdateClient>,
) -> Result<HttpResponse, Error> {
    let client = web::block(move || {
        client_service::update_client(&pool, client_id.into_inner(), client_data.into_inner())
    })
    .await?
    .map_err(|e| {
        eprintln!("Error updating client: {:?}", e);
        HttpResponse::InternalServerError().finish()
    })?;

    Ok(HttpResponse::Ok().json(client))
}

#[delete("/clients/{id}")]
async fn delete_client(
    pool: web::Data<DbPool>,
    client_id: web::Path<i32>,
) -> Result<HttpResponse, Error> {
    let deleted = web::block(move || {
        client_service::delete_client(&pool, client_id.into_inner())
    })
    .await?
    .map_err(|e| {
        eprintln!("Error deleting client: {:?}", e);
        HttpResponse::InternalServerError().finish()
    })?;

    if deleted > 0 {
        Ok(HttpResponse::NoContent().finish())
    } else {
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

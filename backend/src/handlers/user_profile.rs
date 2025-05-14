use crate::errors::AppError;
use crate::models::user_profile::{NewUserProfile, UpdateUserProfile};
use crate::services::user_profile as user_service;
use crate::DbPool;
use actix_web::{get, put, web, Error, HttpResponse};

#[get("/profile")]
async fn get_profile(pool: web::Data<DbPool>) -> Result<HttpResponse, Error> {
    let profile = web::block(move || user_service::get_profile(&pool))
        .await?
        .map_err(|e| {
            eprintln!("Error getting profile: {:?}", e);
            AppError::Database(e)
        })?;

    match profile {
        Some(profile) => Ok(HttpResponse::Ok().json(profile)),
        None => Ok(HttpResponse::NotFound().json("User profile not found")),
    }
}

#[put("/profile")]
async fn update_profile(
    pool: web::Data<DbPool>,
    profile_data: web::Json<UpdateUserProfile>,
) -> Result<HttpResponse, Error> {
    // Check if profile exists
    let pool_clone = pool.clone();
    let existing_profile = web::block(move || user_service::get_profile(&pool_clone))
        .await?
        .map_err(|e| {
            eprintln!("Error checking profile: {:?}", e);
            AppError::Database(e)
        })?;

    // If profile exists, update it. If not, create it.
    match existing_profile {
        Some(profile) => {
            let profile_id = profile.id;
            let updated_profile = web::block(move || {
                user_service::update_profile(&pool, profile_id, profile_data.into_inner())
            })
            .await?
            .map_err(|e| {
                eprintln!("Error updating profile: {:?}", e);
                AppError::Database(e)
            })?;

            Ok(HttpResponse::Ok().json(updated_profile))
        }
        None => {
            // Profile doesn't exist, we need to create it
            // For creation we need all fields to be provided
            if profile_data.name.is_none() || profile_data.address.is_none() {
                return Ok(HttpResponse::BadRequest()
                    .json("Name and address are required for creating a user profile"));
            }

            let new_profile = NewUserProfile {
                name: profile_data.name.clone().unwrap(),
                address: profile_data.address.clone().unwrap(),
                tax_id: profile_data.tax_id.clone(),
                bank_details: profile_data.bank_details.clone(),
            };

            let created_profile =
                web::block(move || user_service::create_profile(&pool, new_profile))
                    .await?
                    .map_err(|e| {
                        eprintln!("Error creating profile: {:?}", e);
                        AppError::Database(e)
                    })?;

            Ok(HttpResponse::Created().json(created_profile))
        }
    }
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(get_profile).service(update_profile);
}

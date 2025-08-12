use crate::errors::AppError;
use crate::models::user_profile::{NewUserProfile, UpdateUserProfile};
use crate::services::user_profile as user_service;
use crate::DbPool;
use actix_web::{get, put, web, Error, HttpMessage, HttpRequest, HttpResponse};
use serde_json::json;

fn get_request_id(req: &HttpRequest) -> String {
    req.extensions()
        .get::<String>()
        .cloned()
        .unwrap_or_else(|| "unknown".to_string())
}

#[get("/profile")]
async fn get_profile(pool: web::Data<DbPool>, req: HttpRequest) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_profile",
            "message": "Fetching user profile"
        })
    );

    let profile = web::block(move || user_service::get_profile(&pool))
        .await?
        .map_err(|e| {
            log::error!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "get_profile",
                    "error": e.to_string(),
                    "message": "Database error while fetching profile"
                })
            );
            AppError::Database(e)
        })?;

    match profile {
        Some(profile) => {
            log::info!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "get_profile",
                    "profile_id": profile.id,
                    "message": "Profile found successfully"
                })
            );
            Ok(HttpResponse::Ok().json(profile))
        }
        None => {
            log::warn!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "get_profile",
                    "message": "User profile not found"
                })
            );
            Ok(HttpResponse::NotFound().json("User profile not found"))
        }
    }
}

#[put("/profile")]
async fn update_profile(
    pool: web::Data<DbPool>,
    mut profile_data: web::Json<UpdateUserProfile>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "update_profile",
            "message": "Updating user profile"
        })
    );

    // Validate and sanitize input
    if let Err(errors) = profile_data.validate_and_sanitize() {
        log::warn!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "update_profile",
                "validation_errors": format!("{:?}", errors),
                "message": "Profile validation failed"
            })
        );
        return Err(AppError::Validation(format!("Validation failed: {:?}", errors)).into());
    }

    // Check if profile exists
    let pool_clone = pool.clone();
    let existing_profile = web::block(move || user_service::get_profile(&pool_clone))
        .await?
        .map_err(|e| {
            log::error!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "update_profile",
                    "error": e.to_string(),
                    "message": "Database error while checking existing profile"
                })
            );
            AppError::Database(e)
        })?;

    // If profile exists, update it. If not, create it.
    match existing_profile {
        Some(profile) => {
            let profile_id = profile.id;
            log::info!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "update_profile",
                    "profile_id": profile_id,
                    "message": "Updating existing profile"
                })
            );

            let updated_profile = web::block(move || {
                user_service::update_profile(&pool, profile_id, profile_data.into_inner())
            })
            .await?
            .map_err(|e| {
                log::error!(
                    target: "business_logic",
                    "{}",
                    json!({
                        "request_id": request_id,
                        "action": "update_profile",
                        "profile_id": profile_id,
                        "error": e.to_string(),
                        "message": "Database error while updating profile"
                    })
                );
                AppError::Database(e)
            })?;

            log::info!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "update_profile",
                    "profile_id": profile_id,
                    "message": "Profile updated successfully"
                })
            );

            Ok(HttpResponse::Ok().json(updated_profile))
        }
        None => {
            // Profile doesn't exist, we need to create it
            // For creation we need all fields to be provided
            if profile_data.name.is_none() || profile_data.address.is_none() {
                log::warn!(
                    target: "business_logic",
                    "{}",
                    json!({
                        "request_id": request_id,
                        "action": "update_profile",
                        "message": "Name and address are required for creating a user profile"
                    })
                );
                return Err(AppError::BadRequest(
                    "Name and address are required for creating a user profile".to_string(),
                )
                .into());
            }

            let mut new_profile = NewUserProfile {
                name: profile_data.name.clone().unwrap(),
                address: profile_data.address.clone().unwrap(),
                tax_id: profile_data.tax_id.clone(),
                bank_details: profile_data.bank_details.clone(),
            };

            // Validate the new profile
            if let Err(errors) = new_profile.validate_and_sanitize() {
                log::warn!(
                    target: "business_logic",
                    "{}",
                    json!({
                        "request_id": request_id,
                        "action": "update_profile",
                        "validation_errors": format!("{:?}", errors),
                        "message": "New profile validation failed"
                    })
                );
                return Err(
                    AppError::Validation(format!("Validation failed: {:?}", errors)).into(),
                );
            }

            log::info!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "update_profile",
                    "message": "Creating new profile"
                })
            );

            let created_profile =
                web::block(move || user_service::create_profile(&pool, new_profile))
                    .await?
                    .map_err(|e| {
                        log::error!(
                            target: "business_logic",
                            "{}",
                            json!({
                                "request_id": request_id,
                                "action": "update_profile",
                                "error": e.to_string(),
                                "message": "Database error while creating profile"
                            })
                        );
                        AppError::Database(e)
                    })?;

            log::info!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "update_profile",
                    "profile_id": created_profile.id,
                    "message": "Profile created successfully"
                })
            );

            Ok(HttpResponse::Created().json(created_profile))
        }
    }
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(get_profile).service(update_profile);
}

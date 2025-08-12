use crate::models::user_profile::{NewUserProfile, UpdateUserProfile, UserProfile};
use crate::DbPool;
use diesel::prelude::*;

/// Retrieves the user profile (assumes single profile system)
///
/// # Arguments
/// * `pool` - Database connection pool
///
/// # Returns
/// * `Result<Option<UserProfile>, diesel::result::Error>` - User profile if exists or database error
pub fn get_profile(pool: &DbPool) -> Result<Option<UserProfile>, diesel::result::Error> {
    use crate::schema::user_profile::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::debug!("Fetching user profile");

    let result = user_profile
        .select(UserProfile::as_select())
        .first(&mut conn)
        .optional();

    match &result {
        Ok(Some(profile)) => log::debug!("Successfully found user profile with ID: {}", profile.id),
        Ok(None) => log::debug!("No user profile found"),
        Err(e) => log::error!("Failed to fetch user profile: {}", e),
    }

    result
}

/// Creates a new user profile in the database
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `new_profile` - Profile data to create
///
/// # Returns
/// * `Result<UserProfile, diesel::result::Error>` - Created profile or database error
pub fn create_profile(
    pool: &DbPool,
    new_profile: NewUserProfile,
) -> Result<UserProfile, diesel::result::Error> {
    use crate::schema::user_profile;
    use crate::schema::user_profile::dsl::*;

    // Business logic validation
    if new_profile.name.trim().is_empty() {
        log::warn!("Attempted to create profile with empty name");
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::CheckViolation,
            Box::new("Profile name cannot be empty".to_string()),
        ));
    }

    if new_profile.address.trim().is_empty() {
        log::warn!("Attempted to create profile with empty address");
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::CheckViolation,
            Box::new("Profile address cannot be empty".to_string()),
        ));
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::info!("Creating new user profile: {}", new_profile.name);

    // Check if profile already exists (single profile system)
    let existing_count: i64 = user_profile
        .select(diesel::dsl::count_star())
        .first(&mut conn)?;

    if existing_count > 0 {
        log::warn!("Attempted to create profile when one already exists");
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::UniqueViolation,
            Box::new("User profile already exists".to_string()),
        ));
    }

    diesel::insert_into(user_profile::table)
        .values(&new_profile)
        .execute(&mut conn)?;

    // SQLite doesn't support RETURNING, so fetch the inserted profile
    let result = user_profile
        .order(id.desc())
        .limit(1)
        .select(UserProfile::as_select())
        .get_result(&mut conn);

    match &result {
        Ok(profile) => log::info!("Successfully created user profile with ID: {}", profile.id),
        Err(e) => log::error!("Failed to create user profile: {}", e),
    }

    result
}

/// Updates an existing user profile in the database
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `profile_id` - ID of the profile to update
/// * `update_profile` - Updated profile data
///
/// # Returns
/// * `Result<UserProfile, diesel::result::Error>` - Updated profile or database error
pub fn update_profile(
    pool: &DbPool,
    profile_id: i32,
    update_profile: UpdateUserProfile,
) -> Result<UserProfile, diesel::result::Error> {
    use crate::schema::user_profile::dsl::*;

    // Validate input
    if profile_id <= 0 {
        log::warn!("Invalid profile ID for update: {}", profile_id);
        return Err(diesel::result::Error::NotFound);
    }

    // Business logic validation
    if let Some(ref profile_name) = update_profile.name {
        if profile_name.trim().is_empty() {
            log::warn!("Attempted to update profile {} with empty name", profile_id);
            return Err(diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                Box::new("Profile name cannot be empty".to_string()),
            ));
        }
    }

    if let Some(ref profile_address) = update_profile.address {
        if profile_address.trim().is_empty() {
            log::warn!(
                "Attempted to update profile {} with empty address",
                profile_id
            );
            return Err(diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                Box::new("Profile address cannot be empty".to_string()),
            ));
        }
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::info!("Updating user profile with ID: {}", profile_id);

    // Check if profile exists
    let existing_profile = user_profile
        .filter(id.eq(profile_id))
        .select(UserProfile::as_select())
        .first(&mut conn)
        .optional()?;

    if existing_profile.is_none() {
        log::warn!("Attempted to update non-existent profile: {}", profile_id);
        return Err(diesel::result::Error::NotFound);
    }

    diesel::update(user_profile.filter(id.eq(profile_id)))
        .set(&update_profile)
        .execute(&mut conn)?;

    // Fetch the updated record
    let result = user_profile
        .filter(id.eq(profile_id))
        .select(UserProfile::as_select())
        .get_result(&mut conn);

    match &result {
        Ok(_) => log::info!("Successfully updated user profile with ID: {}", profile_id),
        Err(e) => log::error!("Failed to update user profile {}: {}", profile_id, e),
    }

    result
}

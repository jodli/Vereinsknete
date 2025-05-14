use diesel::prelude::*;
use crate::DbPool;
use crate::models::user_profile::{UserProfile, NewUserProfile, UpdateUserProfile};

pub fn get_profile(pool: &DbPool) -> Result<Option<UserProfile>, diesel::result::Error> {
    use crate::schema::user_profile::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");
    user_profile
        .select(UserProfile::as_select())
        .first(&mut conn)
        .optional()
}

pub fn create_profile(pool: &DbPool, new_profile: NewUserProfile) -> Result<UserProfile, diesel::result::Error> {
    use crate::schema::user_profile;

    let mut conn = pool.get().expect("Failed to get DB connection");

    diesel::insert_into(user_profile::table)
        .values(&new_profile)
        .returning(UserProfile::as_returning())
        .get_result(&mut conn)
}

pub fn update_profile(pool: &DbPool, profile_id: i32, update_profile: UpdateUserProfile) -> Result<UserProfile, diesel::result::Error> {
    use crate::schema::user_profile::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");

    diesel::update(user_profile.filter(id.eq(profile_id)))
        .set(&update_profile)
        .returning(UserProfile::as_returning())
        .get_result(&mut conn)
}

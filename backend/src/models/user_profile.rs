use diesel::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize, Queryable, Selectable)]
#[diesel(table_name = crate::schema::user_profile)]
#[diesel(check_for_backend(diesel::sqlite::Sqlite))]
pub struct UserProfile {
    pub id: i32,
    pub name: String,
    pub address: String,
    pub tax_id: Option<String>,
    pub bank_details: Option<String>,
}

#[derive(Debug, Deserialize, Insertable)]
#[diesel(table_name = crate::schema::user_profile)]
pub struct NewUserProfile {
    pub name: String,
    pub address: String,
    pub tax_id: Option<String>,
    pub bank_details: Option<String>,
}

#[derive(Debug, Deserialize, AsChangeset)]
#[diesel(table_name = crate::schema::user_profile)]
pub struct UpdateUserProfile {
    pub name: Option<String>,
    pub address: Option<String>,
    pub tax_id: Option<String>,
    pub bank_details: Option<String>,
}

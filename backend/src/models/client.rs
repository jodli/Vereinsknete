use diesel::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize, Queryable, Selectable)]
#[diesel(table_name = crate::schema::clients)]
#[diesel(check_for_backend(diesel::sqlite::Sqlite))]
pub struct Client {
    pub id: i32,
    pub name: String,
    pub address: String,
    pub contact_person: Option<String>,
    pub default_hourly_rate: f32,
}

#[derive(Debug, Deserialize, Insertable)]
#[diesel(table_name = crate::schema::clients)]
pub struct NewClient {
    pub name: String,
    pub address: String,
    pub contact_person: Option<String>,
    pub default_hourly_rate: f32,
}

#[derive(Debug, Deserialize, AsChangeset)]
#[diesel(table_name = crate::schema::clients)]
pub struct UpdateClient {
    pub name: Option<String>,
    pub address: Option<String>,
    pub contact_person: Option<String>,
    pub default_hourly_rate: Option<f32>,
}

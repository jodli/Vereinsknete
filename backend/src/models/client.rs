use diesel::prelude::*;
use serde::{Deserialize, Serialize};
use validator::Validate;

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

#[derive(Debug, Deserialize, Insertable, Validate)]
#[diesel(table_name = crate::schema::clients)]
pub struct NewClient {
    #[validate(length(
        min = 1,
        max = 100,
        message = "Name must be between 1 and 100 characters"
    ))]
    pub name: String,

    #[validate(length(
        min = 10,
        max = 500,
        message = "Address must be between 10 and 500 characters"
    ))]
    pub address: String,

    #[validate(length(
        min = 1,
        max = 100,
        message = "Contact person must be between 1 and 100 characters"
    ))]
    pub contact_person: Option<String>,

    #[validate(range(
        min = 0.0,
        max = 1000.0,
        message = "Hourly rate must be between 0 and 1000"
    ))]
    pub default_hourly_rate: f32,
}

#[derive(Debug, Deserialize, AsChangeset, Validate)]
#[diesel(table_name = crate::schema::clients)]
pub struct UpdateClient {
    #[validate(length(
        min = 1,
        max = 100,
        message = "Name must be between 1 and 100 characters"
    ))]
    pub name: Option<String>,

    #[validate(length(
        min = 10,
        max = 500,
        message = "Address must be between 10 and 500 characters"
    ))]
    pub address: Option<String>,

    #[validate(length(
        min = 1,
        max = 100,
        message = "Contact person must be between 1 and 100 characters"
    ))]
    pub contact_person: Option<String>,

    #[validate(range(
        min = 0.0,
        max = 1000.0,
        message = "Hourly rate must be between 0 and 1000"
    ))]
    pub default_hourly_rate: Option<f32>,
}

impl NewClient {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize input
        self.name = self.name.trim().to_string();
        self.address = self.address.trim().to_string();
        if let Some(ref mut contact) = self.contact_person {
            *contact = contact.trim().to_string();
            if contact.is_empty() {
                self.contact_person = None;
            }
        }

        // Validate
        self.validate()
    }
}

impl UpdateClient {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize input
        if let Some(ref mut name) = self.name {
            *name = name.trim().to_string();
        }
        if let Some(ref mut address) = self.address {
            *address = address.trim().to_string();
        }
        if let Some(ref mut contact) = self.contact_person {
            *contact = contact.trim().to_string();
            if contact.is_empty() {
                self.contact_person = None;
            }
        }

        // Validate
        self.validate()
    }
}

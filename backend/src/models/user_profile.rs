use diesel::prelude::*;
use serde::{Deserialize, Serialize};
use validator::Validate;

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

#[derive(Debug, Deserialize, Insertable, Validate)]
#[diesel(table_name = crate::schema::user_profile)]
pub struct NewUserProfile {
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
        max = 50,
        message = "Tax ID must be between 1 and 50 characters"
    ))]
    pub tax_id: Option<String>,

    #[validate(length(
        min = 1,
        max = 500,
        message = "Bank details must be between 1 and 500 characters"
    ))]
    pub bank_details: Option<String>,
}

#[derive(Debug, Deserialize, AsChangeset, Validate)]
#[diesel(table_name = crate::schema::user_profile)]
pub struct UpdateUserProfile {
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
        max = 50,
        message = "Tax ID must be between 1 and 50 characters"
    ))]
    pub tax_id: Option<String>,

    #[validate(length(
        min = 1,
        max = 500,
        message = "Bank details must be between 1 and 500 characters"
    ))]
    pub bank_details: Option<String>,
}

impl NewUserProfile {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize input
        self.name = self.name.trim().to_string();
        self.address = self.address.trim().to_string();

        if let Some(ref mut tax_id) = self.tax_id {
            *tax_id = tax_id.trim().to_string();
            if tax_id.is_empty() {
                self.tax_id = None;
            }
        }

        if let Some(ref mut bank_details) = self.bank_details {
            *bank_details = bank_details.trim().to_string();
            if bank_details.is_empty() {
                self.bank_details = None;
            }
        }

        // Validate
        self.validate()
    }
}

impl UpdateUserProfile {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize input
        if let Some(ref mut name) = self.name {
            *name = name.trim().to_string();
        }

        if let Some(ref mut address) = self.address {
            *address = address.trim().to_string();
        }

        if let Some(ref mut tax_id) = self.tax_id {
            *tax_id = tax_id.trim().to_string();
            if tax_id.is_empty() {
                self.tax_id = None;
            }
        }

        if let Some(ref mut bank_details) = self.bank_details {
            *bank_details = bank_details.trim().to_string();
            if bank_details.is_empty() {
                self.bank_details = None;
            }
        }

        // Validate
        self.validate()
    }
}

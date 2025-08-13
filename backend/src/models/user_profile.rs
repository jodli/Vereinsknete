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

#[derive(Debug, Serialize, Deserialize, Insertable, Validate)]
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

#[derive(Debug, Serialize, Deserialize, AsChangeset, Validate)]
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

#[cfg(test)]
mod tests {
    use super::*;
    use validator::Validate;

    // Test fixtures
    fn create_valid_user_profile() -> NewUserProfile {
        NewUserProfile {
            name: "John Doe".to_string(),
            address: "123 Main Street, Anytown, 12345".to_string(),
            tax_id: Some("TAX123456789".to_string()),
            bank_details: Some(
                "Bank: Example Bank\nIBAN: DE89370400440532013000\nBIC: COBADEFFXXX".to_string(),
            ),
        }
    }

    fn create_minimal_user_profile() -> NewUserProfile {
        NewUserProfile {
            name: "Jane Smith".to_string(),
            address: "456 Oak Avenue, Somewhere, 67890".to_string(),
            tax_id: None,
            bank_details: None,
        }
    }

    fn create_german_user_profile() -> NewUserProfile {
        NewUserProfile {
            name: "Hans Müller".to_string(),
            address: "Musterstraße 123, 12345 Berlin, Deutschland".to_string(),
            tax_id: Some("DE123456789".to_string()),
            bank_details: Some("Sparkasse Berlin\nIBAN: DE89370400440532013000".to_string()),
        }
    }

    fn create_valid_update_profile() -> UpdateUserProfile {
        UpdateUserProfile {
            name: Some("Updated Name".to_string()),
            address: Some("789 Updated Street, New City, 54321".to_string()),
            tax_id: Some("NEWTAX987654321".to_string()),
            bank_details: Some("Updated Bank Details".to_string()),
        }
    }

    // NewUserProfile validation tests
    #[test]
    fn test_new_user_profile_valid() {
        let profile = create_valid_user_profile();
        assert!(profile.validate().is_ok());
    }

    #[test]
    fn test_new_user_profile_minimal_valid() {
        let profile = create_minimal_user_profile();
        assert!(profile.validate().is_ok());
    }

    #[test]
    fn test_new_user_profile_german_valid() {
        let profile = create_german_user_profile();
        assert!(profile.validate().is_ok());
    }

    #[test]
    fn test_new_user_profile_empty_name() {
        let mut profile = create_valid_user_profile();
        profile.name = "".to_string();

        let result = profile.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("name"));
    }

    #[test]
    fn test_new_user_profile_name_too_long() {
        let mut profile = create_valid_user_profile();
        profile.name = "a".repeat(101); // Exceeds 100 character limit

        let result = profile.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("name"));
    }

    #[test]
    fn test_new_user_profile_address_too_short() {
        let mut profile = create_valid_user_profile();
        profile.address = "Short".to_string(); // Less than 10 characters

        let result = profile.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("address"));
    }

    #[test]
    fn test_new_user_profile_address_too_long() {
        let mut profile = create_valid_user_profile();
        profile.address = "a".repeat(501); // Exceeds 500 character limit

        let result = profile.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("address"));
    }

    #[test]
    fn test_new_user_profile_empty_tax_id() {
        let mut profile = create_valid_user_profile();
        profile.tax_id = Some("".to_string());

        let result = profile.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("tax_id"));
    }

    #[test]
    fn test_new_user_profile_tax_id_too_long() {
        let mut profile = create_valid_user_profile();
        profile.tax_id = Some("a".repeat(51)); // Exceeds 50 character limit

        let result = profile.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("tax_id"));
    }

    #[test]
    fn test_new_user_profile_none_tax_id() {
        let mut profile = create_valid_user_profile();
        profile.tax_id = None;

        // None tax_id should be valid
        assert!(profile.validate().is_ok());
    }

    #[test]
    fn test_new_user_profile_empty_bank_details() {
        let mut profile = create_valid_user_profile();
        profile.bank_details = Some("".to_string());

        let result = profile.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("bank_details"));
    }

    #[test]
    fn test_new_user_profile_bank_details_too_long() {
        let mut profile = create_valid_user_profile();
        profile.bank_details = Some("a".repeat(501)); // Exceeds 500 character limit

        let result = profile.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("bank_details"));
    }

    #[test]
    fn test_new_user_profile_none_bank_details() {
        let mut profile = create_valid_user_profile();
        profile.bank_details = None;

        // None bank_details should be valid
        assert!(profile.validate().is_ok());
    }

    // Sanitization tests
    #[test]
    fn test_new_user_profile_sanitization() {
        let mut profile = NewUserProfile {
            name: "  John Doe  ".to_string(),
            address: "  123 Main Street, Anytown, 12345  ".to_string(),
            tax_id: Some("  TAX123456789  ".to_string()),
            bank_details: Some("  Bank Details  ".to_string()),
        };

        assert!(profile.validate_and_sanitize().is_ok());

        // Check sanitization worked
        assert_eq!(profile.name, "John Doe");
        assert_eq!(profile.address, "123 Main Street, Anytown, 12345");
        assert_eq!(profile.tax_id, Some("TAX123456789".to_string()));
        assert_eq!(profile.bank_details, Some("Bank Details".to_string()));
    }

    #[test]
    fn test_new_user_profile_sanitization_empty_optional_fields() {
        let mut profile = NewUserProfile {
            name: "John Doe".to_string(),
            address: "123 Main Street, Anytown, 12345".to_string(),
            tax_id: Some("   ".to_string()),       // Only whitespace
            bank_details: Some("   ".to_string()), // Only whitespace
        };

        assert!(profile.validate_and_sanitize().is_ok());

        // Empty optional fields should be converted to None
        assert_eq!(profile.tax_id, None);
        assert_eq!(profile.bank_details, None);
    }

    // UpdateUserProfile tests
    #[test]
    fn test_update_user_profile_valid() {
        let update = create_valid_update_profile();
        assert!(update.validate().is_ok());
    }

    #[test]
    fn test_update_user_profile_partial() {
        let update = UpdateUserProfile {
            name: Some("Only Name Updated".to_string()),
            address: None,
            tax_id: None,
            bank_details: None,
        };

        assert!(update.validate().is_ok());
    }

    #[test]
    fn test_update_user_profile_empty_update() {
        let update = UpdateUserProfile {
            name: None,
            address: None,
            tax_id: None,
            bank_details: None,
        };

        // Empty update should be valid
        assert!(update.validate().is_ok());
    }

    #[test]
    fn test_update_user_profile_invalid_name() {
        let update = UpdateUserProfile {
            name: Some("".to_string()), // Invalid empty name
            address: None,
            tax_id: None,
            bank_details: None,
        };

        let result = update.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("name"));
    }

    #[test]
    fn test_update_user_profile_invalid_address() {
        let update = UpdateUserProfile {
            name: None,
            address: Some("Short".to_string()), // Invalid short address
            tax_id: None,
            bank_details: None,
        };

        let result = update.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("address"));
    }

    #[test]
    fn test_update_user_profile_invalid_tax_id() {
        let update = UpdateUserProfile {
            name: None,
            address: None,
            tax_id: Some("".to_string()), // Invalid empty tax_id
            bank_details: None,
        };

        let result = update.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("tax_id"));
    }

    #[test]
    fn test_update_user_profile_invalid_bank_details() {
        let update = UpdateUserProfile {
            name: None,
            address: None,
            tax_id: None,
            bank_details: Some("".to_string()), // Invalid empty bank_details
        };

        let result = update.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("bank_details"));
    }

    #[test]
    fn test_update_user_profile_sanitization() {
        let mut update = UpdateUserProfile {
            name: Some("  Updated Name  ".to_string()),
            address: Some("  789 Updated Street  ".to_string()),
            tax_id: Some("  NEWTAX987  ".to_string()),
            bank_details: Some("  Updated Bank  ".to_string()),
        };

        assert!(update.validate_and_sanitize().is_ok());

        // Check sanitization worked
        assert_eq!(update.name, Some("Updated Name".to_string()));
        assert_eq!(update.address, Some("789 Updated Street".to_string()));
        assert_eq!(update.tax_id, Some("NEWTAX987".to_string()));
        assert_eq!(update.bank_details, Some("Updated Bank".to_string()));
    }

    #[test]
    fn test_update_user_profile_sanitization_empty_fields() {
        let mut update = UpdateUserProfile {
            name: None,
            address: None,
            tax_id: Some("   ".to_string()),       // Only whitespace
            bank_details: Some("   ".to_string()), // Only whitespace
        };

        assert!(update.validate_and_sanitize().is_ok());

        // Empty fields should be converted to None
        assert_eq!(update.tax_id, None);
        assert_eq!(update.bank_details, None);
    }

    // Boundary value tests
    #[test]
    fn test_new_user_profile_boundary_values() {
        // Test minimum valid values
        let mut profile = NewUserProfile {
            name: "A".to_string(),               // Minimum 1 character
            address: "1234567890".to_string(),   // Minimum 10 characters
            tax_id: Some("B".to_string()),       // Minimum 1 character
            bank_details: Some("C".to_string()), // Minimum 1 character
        };
        assert!(profile.validate().is_ok());

        // Test maximum valid values
        profile.name = "A".repeat(100); // Maximum 100 characters
        profile.address = "A".repeat(500); // Maximum 500 characters
        profile.tax_id = Some("B".repeat(50)); // Maximum 50 characters
        profile.bank_details = Some("C".repeat(500)); // Maximum 500 characters
        assert!(profile.validate().is_ok());
    }

    // Serialization tests
    #[test]
    fn test_new_user_profile_serialization() {
        let profile = create_valid_user_profile();
        let json = serde_json::to_string(&profile).expect("Should serialize to JSON");

        assert!(json.contains("John Doe"));
        assert!(json.contains("123 Main Street"));
        assert!(json.contains("TAX123456789"));
        assert!(json.contains("Bank: Example Bank"));
    }

    #[test]
    fn test_new_user_profile_deserialization() {
        let json = r#"{
            "name": "Deserialized User",
            "address": "456 Deserialize Ave, JSON City, 98765",
            "tax_id": "DESERTAX123",
            "bank_details": "Deserialized Bank Details"
        }"#;

        let profile: NewUserProfile =
            serde_json::from_str(json).expect("Should deserialize from JSON");

        assert_eq!(profile.name, "Deserialized User");
        assert_eq!(profile.address, "456 Deserialize Ave, JSON City, 98765");
        assert_eq!(profile.tax_id, Some("DESERTAX123".to_string()));
        assert_eq!(
            profile.bank_details,
            Some("Deserialized Bank Details".to_string())
        );
    }

    #[test]
    fn test_new_user_profile_deserialization_minimal() {
        let json = r#"{
            "name": "Minimal User",
            "address": "789 Minimal Street, Min City, 12345"
        }"#;

        let profile: NewUserProfile =
            serde_json::from_str(json).expect("Should deserialize from JSON");

        assert_eq!(profile.name, "Minimal User");
        assert_eq!(profile.address, "789 Minimal Street, Min City, 12345");
        assert_eq!(profile.tax_id, None);
        assert_eq!(profile.bank_details, None);
    }

    // Special character tests
    #[test]
    fn test_user_profile_with_special_characters() {
        let profile = NewUserProfile {
            name: "José María García-López".to_string(),
            address: "Calle de Alcalá 123, 28009 Madrid, España".to_string(),
            tax_id: Some("ES-B12345678".to_string()),
            bank_details: Some(
                "Banco Santander\nIBAN: ES91 2100 0418 4502 0005 1332\nBIC: CAIXESBBXXX"
                    .to_string(),
            ),
        };

        assert!(profile.validate().is_ok());
    }

    // Real-world data tests
    #[test]
    fn test_user_profile_realistic_bank_details() {
        let profile = NewUserProfile {
            name: "Business Owner".to_string(),
            address: "123 Business District, Corporate City, 12345".to_string(),
            tax_id: Some("TAX-ID-123456789".to_string()),
            bank_details: Some(
                "Bank Name: First National Bank\n\
                Account Holder: Business Owner\n\
                IBAN: DE89 3704 0044 0532 0130 00\n\
                BIC: COBADEFFXXX\n\
                Account Type: Business Checking"
                    .to_string(),
            ),
        };

        assert!(profile.validate().is_ok());
    }

    #[test]
    fn test_user_profile_multiline_address() {
        let profile = NewUserProfile {
            name: "Multi Line User".to_string(),
            address: "123 Main Street\nApartment 4B\nAnytown, State 12345\nCountry".to_string(),
            tax_id: Some("MULTI123".to_string()),
            bank_details: None,
        };

        assert!(profile.validate().is_ok());
    }

    // Edge cases
    #[test]
    fn test_user_profile_unicode_characters() {
        let profile = NewUserProfile {
            name: "用户名称".to_string(), // Chinese characters
            address: "住址信息 123, 城市名称, 12345".to_string(),
            tax_id: Some("税号123456".to_string()),
            bank_details: Some("银行详情信息".to_string()),
        };

        assert!(profile.validate().is_ok());
    }

    #[test]
    fn test_user_profile_numbers_and_symbols() {
        let profile = NewUserProfile {
            name: "User #123 & Co.".to_string(),
            address: "123-456 Main St., Suite #789, City (State) 12345-6789".to_string(),
            tax_id: Some("TAX#123-456-789".to_string()),
            bank_details: Some("Account #123456789 @ Bank & Trust Co.".to_string()),
        };

        assert!(profile.validate().is_ok());
    }
}

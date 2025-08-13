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

#[derive(Debug, Serialize, Deserialize, Insertable, Validate)]
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

#[derive(Debug, Serialize, Deserialize, AsChangeset, Validate)]
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

#[cfg(test)]
mod tests {
    use super::*;
    use validator::Validate;

    // Test fixtures
    fn create_valid_client() -> NewClient {
        NewClient {
            name: "Test Client".to_string(),
            address: "123 Test Street, Test City, 12345".to_string(),
            contact_person: Some("John Doe".to_string()),
            default_hourly_rate: 75.0,
        }
    }

    fn create_minimal_client() -> NewClient {
        NewClient {
            name: "Minimal Client".to_string(),
            address: "456 Minimal Ave, Min City, 67890".to_string(),
            contact_person: None,
            default_hourly_rate: 50.0,
        }
    }

    // NewClient validation tests
    #[test]
    fn test_new_client_valid() {
        let client = create_valid_client();
        assert!(client.validate().is_ok());
    }

    #[test]
    fn test_new_client_minimal_valid() {
        let client = create_minimal_client();
        assert!(client.validate().is_ok());
    }

    #[test]
    fn test_new_client_empty_name() {
        let mut client = create_valid_client();
        client.name = "".to_string();

        let result = client.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("name"));
    }

    #[test]
    fn test_new_client_name_too_long() {
        let mut client = create_valid_client();
        client.name = "a".repeat(101); // Exceeds 100 character limit

        let result = client.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("name"));
    }

    #[test]
    fn test_new_client_address_too_short() {
        let mut client = create_valid_client();
        client.address = "Short".to_string(); // Less than 10 characters

        let result = client.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("address"));
    }

    #[test]
    fn test_new_client_address_too_long() {
        let mut client = create_valid_client();
        client.address = "a".repeat(501); // Exceeds 500 character limit

        let result = client.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("address"));
    }

    #[test]
    fn test_new_client_negative_hourly_rate() {
        let mut client = create_valid_client();
        client.default_hourly_rate = -10.0;

        let result = client.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("default_hourly_rate"));
    }

    #[test]
    fn test_new_client_hourly_rate_too_high() {
        let mut client = create_valid_client();
        client.default_hourly_rate = 1001.0; // Exceeds 1000 limit

        let result = client.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("default_hourly_rate"));
    }

    #[test]
    fn test_new_client_empty_contact_person() {
        let mut client = create_valid_client();
        client.contact_person = Some("".to_string());

        let result = client.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("contact_person"));
    }

    #[test]
    fn test_new_client_none_contact_person() {
        let mut client = create_valid_client();
        client.contact_person = None;

        // None contact person should be valid
        assert!(client.validate().is_ok());
    }

    // Sanitization tests
    #[test]
    fn test_new_client_sanitization() {
        let mut client = NewClient {
            name: "  Test Client  ".to_string(),
            address: "  123 Test Street, Test City, 12345  ".to_string(),
            contact_person: Some("  John Doe  ".to_string()),
            default_hourly_rate: 75.0,
        };

        assert!(client.validate_and_sanitize().is_ok());

        // Check sanitization worked
        assert_eq!(client.name, "Test Client");
        assert_eq!(client.address, "123 Test Street, Test City, 12345");
        assert_eq!(client.contact_person, Some("John Doe".to_string()));
    }

    #[test]
    fn test_new_client_sanitization_empty_contact() {
        let mut client = NewClient {
            name: "Test Client".to_string(),
            address: "123 Test Street, Test City, 12345".to_string(),
            contact_person: Some("   ".to_string()), // Only whitespace
            default_hourly_rate: 75.0,
        };

        assert!(client.validate_and_sanitize().is_ok());

        // Empty contact person should be converted to None
        assert_eq!(client.contact_person, None);
    }

    // Serialization tests
    #[test]
    fn test_new_client_serialization() {
        let client = create_valid_client();
        let json = serde_json::to_string(&client).expect("Should serialize to JSON");

        assert!(json.contains("Test Client"));
        assert!(json.contains("123 Test Street"));
        assert!(json.contains("John Doe"));
        assert!(json.contains("75"));
    }

    #[test]
    fn test_new_client_deserialization() {
        let json = r#"{
            "name": "Deserialized Client",
            "address": "456 Deserialize Ave, JSON City, 98765",
            "contact_person": "Jane Smith",
            "default_hourly_rate": 90.0
        }"#;

        let client: NewClient = serde_json::from_str(json).expect("Should deserialize from JSON");

        assert_eq!(client.name, "Deserialized Client");
        assert_eq!(client.address, "456 Deserialize Ave, JSON City, 98765");
        assert_eq!(client.contact_person, Some("Jane Smith".to_string()));
        assert_eq!(client.default_hourly_rate, 90.0);
    }

    // UpdateClient tests
    #[test]
    fn test_update_client_valid() {
        let update = UpdateClient {
            name: Some("Updated Client".to_string()),
            address: Some("789 Updated Street, Updated City, 54321".to_string()),
            contact_person: Some("Jane Smith".to_string()),
            default_hourly_rate: Some(85.0),
        };

        assert!(update.validate().is_ok());
    }

    #[test]
    fn test_update_client_partial() {
        let update = UpdateClient {
            name: Some("Only Name Updated".to_string()),
            address: None,
            contact_person: None,
            default_hourly_rate: None,
        };

        assert!(update.validate().is_ok());
    }

    #[test]
    fn test_update_client_invalid_name() {
        let update = UpdateClient {
            name: Some("".to_string()), // Invalid empty name
            address: None,
            contact_person: None,
            default_hourly_rate: None,
        };

        let result = update.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("name"));
    }

    #[test]
    fn test_update_client_invalid_hourly_rate() {
        let update = UpdateClient {
            name: None,
            address: None,
            contact_person: None,
            default_hourly_rate: Some(-5.0), // Invalid negative rate
        };

        let result = update.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("default_hourly_rate"));
    }

    #[test]
    fn test_update_client_sanitization() {
        let mut update = UpdateClient {
            name: Some("  Updated Client  ".to_string()),
            address: Some("  789 Updated Street  ".to_string()),
            contact_person: Some("  Jane Smith  ".to_string()),
            default_hourly_rate: Some(85.0),
        };

        assert!(update.validate_and_sanitize().is_ok());

        // Check sanitization worked
        assert_eq!(update.name, Some("Updated Client".to_string()));
        assert_eq!(update.address, Some("789 Updated Street".to_string()));
        assert_eq!(update.contact_person, Some("Jane Smith".to_string()));
    }

    #[test]
    fn test_update_client_sanitization_empty_contact() {
        let mut update = UpdateClient {
            name: None,
            address: None,
            contact_person: Some("   ".to_string()), // Only whitespace
            default_hourly_rate: None,
        };

        assert!(update.validate_and_sanitize().is_ok());

        // Empty contact person should be converted to None
        assert_eq!(update.contact_person, None);
    }

    // Edge case tests
    #[test]
    fn test_new_client_boundary_values() {
        // Test minimum valid values
        let mut client = NewClient {
            name: "A".to_string(),                 // Minimum 1 character
            address: "1234567890".to_string(),     // Minimum 10 characters
            contact_person: Some("B".to_string()), // Minimum 1 character
            default_hourly_rate: 0.0,              // Minimum 0.0
        };
        assert!(client.validate().is_ok());

        // Test maximum valid values
        client.name = "A".repeat(100); // Maximum 100 characters
        client.address = "A".repeat(500); // Maximum 500 characters
        client.contact_person = Some("B".repeat(100)); // Maximum 100 characters
        client.default_hourly_rate = 1000.0; // Maximum 1000.0
        assert!(client.validate().is_ok());
    }

    #[test]
    fn test_client_with_special_characters() {
        let client = NewClient {
            name: "Müller & Co. GmbH".to_string(),
            address: "Straße 123, 12345 München, Deutschland".to_string(),
            contact_person: Some("José María García-López".to_string()),
            default_hourly_rate: 87.50,
        };

        assert!(client.validate().is_ok());
    }
}

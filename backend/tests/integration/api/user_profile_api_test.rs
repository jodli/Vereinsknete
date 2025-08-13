use crate::common::helpers::{create_test_app, response_to_string};
use crate::common::test_db::setup_test_db;
use actix_web::{http::StatusCode, test};
use backend::models::user_profile::UpdateUserProfile;

#[cfg(test)]
mod user_profile_api_tests {
    use super::*;

    #[actix_rt::test]
    async fn test_profile_create_and_update_flow() {
        let pool = setup_test_db();
        let app = test::init_service(create_test_app(pool.clone())).await;

        // Try get profile first (should 404)
        let req = test::TestRequest::get().uri("/api/profile").to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::NOT_FOUND);

        // Create profile via PUT with all fields
        let update = UpdateUserProfile {
            name: Some("John Test".into()),
            address: Some("Long Street 123, 12345 City".into()),
            tax_id: Some("TAX123".into()),
            bank_details: Some("Bank XYZ\nIBAN: TESTIBAN".into()),
        };
        let req = test::TestRequest::put()
            .uri("/api/profile")
            .set_json(&update)
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::CREATED);
        let body = response_to_string(resp).await;
        let created: serde_json::Value = serde_json::from_str(&body).unwrap();
        assert_eq!(created.get("name").unwrap(), "John Test");

        // Update profile (change name only)
        let update2 = UpdateUserProfile {
            name: Some("John Updated".into()),
            address: None,
            tax_id: None,
            bank_details: None,
        };
        let req = test::TestRequest::put()
            .uri("/api/profile")
            .set_json(&update2)
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);
        let body2 = response_to_string(resp).await;
        let updated: serde_json::Value = serde_json::from_str(&body2).unwrap();
        assert_eq!(updated.get("name").unwrap(), "John Updated");

        // Get profile again
        let req = test::TestRequest::get().uri("/api/profile").to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);
    }
}

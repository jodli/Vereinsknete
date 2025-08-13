use crate::common::helpers::{create_test_app, response_to_string};
use crate::common::test_db::setup_test_db;
use actix_web::{http::StatusCode, test};
use backend::models::client::{NewClient, UpdateClient};

#[cfg(test)]
mod client_api_tests {
    use super::*;

    #[actix_rt::test]
    async fn test_client_crud_flow() {
        let pool = setup_test_db();
        let app = test::init_service(create_test_app(pool.clone())).await;

        // Create client
        let new_client = NewClient {
            name: "Acme Corp".into(),
            address: "Example Street 1, 12345 Sampletown".into(),
            contact_person: Some("Jane Doe".into()),
            default_hourly_rate: 80.0,
        };
        let req = test::TestRequest::post()
            .uri("/api/clients")
            .set_json(&new_client)
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::CREATED);
        let body = response_to_string(resp).await;
        let created: serde_json::Value = serde_json::from_str(&body).unwrap();
        let client_id = created.get("id").unwrap().as_i64().unwrap() as i32;
        assert_eq!(created.get("name").unwrap(), "Acme Corp");

        // Get single client
        let req = test::TestRequest::get()
            .uri(&format!("/api/clients/{}", client_id))
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);

        // Update client
        let update = UpdateClient {
            name: Some("Acme Corporation".into()),
            address: None,
            contact_person: None,
            default_hourly_rate: Some(90.0),
        };
        let req = test::TestRequest::put()
            .uri(&format!("/api/clients/{}", client_id))
            .set_json(&update)
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);
        let updated_body = response_to_string(resp).await;
        let updated: serde_json::Value = serde_json::from_str(&updated_body).unwrap();
        assert_eq!(updated.get("name").unwrap(), "Acme Corporation");

        // List clients
        let req = test::TestRequest::get().uri("/api/clients").to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);
        let list_body = response_to_string(resp).await;
        let clients: serde_json::Value = serde_json::from_str(&list_body).unwrap();
        assert!(!clients.as_array().unwrap().is_empty());

        // Delete client
        let req = test::TestRequest::delete()
            .uri(&format!("/api/clients/{}", client_id))
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::NO_CONTENT);

        // Fetch deleted client -> 404
        let req = test::TestRequest::get()
            .uri(&format!("/api/clients/{}", client_id))
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::NOT_FOUND);
    }

    #[actix_rt::test]
    async fn test_create_client_validation_error() {
        let pool = setup_test_db();
        let app = test::init_service(create_test_app(pool.clone())).await;

        // Missing required fields / invalid address length
        let bad_client = NewClient {
            name: "".into(),
            address: "short".into(),
            contact_person: None,
            default_hourly_rate: -5.0,
        };
        let req = test::TestRequest::post()
            .uri("/api/clients")
            .set_json(&bad_client)
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::UNPROCESSABLE_ENTITY);
    }
}

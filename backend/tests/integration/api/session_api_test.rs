use crate::common::helpers::{create_test_app, response_to_string};
use crate::common::test_db::setup_test_db;
use actix_web::{http::StatusCode, test};
use backend::models::client::NewClient;
use backend::models::session::{NewSessionRequest, UpdateSessionRequest};
use chrono::{NaiveDate, NaiveTime};

#[cfg(test)]
mod session_api_tests {
    use super::*;

    // Generic helper that works with the initialized test service returned by test::init_service
    async fn create_client<S>(app: &S) -> i32
    where
        S: actix_web::dev::Service<
            actix_http::Request,
            Response = actix_web::dev::ServiceResponse,
            Error = actix_web::Error,
        >,
    {
        let new_client = NewClient {
            name: "Client A".into(),
            address: "Address 123456789".into(),
            contact_person: None,
            default_hourly_rate: 50.0,
        };
        let req = test::TestRequest::post()
            .uri("/api/clients")
            .set_json(&new_client)
            .to_request();
        let resp = test::call_service(app, req).await;
        assert_eq!(resp.status(), StatusCode::CREATED);
        let body = response_to_string(resp).await;
        serde_json::from_str::<serde_json::Value>(&body)
            .unwrap()
            .get("id")
            .unwrap()
            .as_i64()
            .unwrap() as i32
    }

    #[actix_rt::test]
    async fn test_session_crud_and_filters() {
        let pool = setup_test_db();
        let app = test::init_service(create_test_app(pool.clone())).await;
        let client_id = create_client(&app).await;

        // Create session
        let new_session = NewSessionRequest {
            client_id,
            name: "Planning".into(),
            date: NaiveDate::from_ymd_opt(2024, 1, 10).unwrap(),
            start_time: NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(11, 30, 0).unwrap(),
        };
        let req = test::TestRequest::post()
            .uri("/api/sessions")
            .set_json(&new_session)
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::CREATED);
        let body = response_to_string(resp).await;
        let created: serde_json::Value = serde_json::from_str(&body).unwrap();
        let session_id = created.get("id").unwrap().as_i64().unwrap() as i32;

        // Fetch session
        let req = test::TestRequest::get()
            .uri(&format!("/api/sessions/{}", session_id))
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);

        // List sessions (no filter)
        let req = test::TestRequest::get().uri("/api/sessions").to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);

        // Update session (full update required by current UpdateSessionRequest definition)
        let update = UpdateSessionRequest {
            client_id,
            name: "Planning Updated".into(),
            date: new_session.date,
            start_time: new_session.start_time,
            end_time: new_session.end_time,
        };
        let req = test::TestRequest::put()
            .uri(&format!("/api/sessions/{}", session_id))
            .set_json(&update)
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);

        // Get sessions by client
        let req = test::TestRequest::get()
            .uri(&format!("/api/clients/{}/sessions", client_id))
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);

        // Delete session
        let req = test::TestRequest::delete()
            .uri(&format!("/api/sessions/{}", session_id))
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::NO_CONTENT);
    }

    #[actix_rt::test]
    async fn test_session_create_validation_error() {
        let pool = setup_test_db();
        let app = test::init_service(create_test_app(pool.clone())).await;
        let client_id = create_client(&app).await;

        // end before start
        let bad_session = NewSessionRequest {
            client_id,
            name: "Bad".into(),
            date: NaiveDate::from_ymd_opt(2024, 1, 10).unwrap(),
            start_time: NaiveTime::from_hms_opt(12, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(11, 0, 0).unwrap(),
        };
        let req = test::TestRequest::post()
            .uri("/api/sessions")
            .set_json(&bad_session)
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::UNPROCESSABLE_ENTITY);
    }
}

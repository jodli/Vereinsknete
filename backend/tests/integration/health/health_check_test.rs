use crate::common::helpers::{create_test_app, response_to_string};
use crate::common::test_db::setup_test_db;
use actix_web::{http::StatusCode, test};

#[cfg(test)]
mod health_check_tests {
    use super::*;

    #[actix_rt::test]
    async fn test_health_ok() {
        let pool = setup_test_db();
        let app = test::init_service(create_test_app(pool)).await;
        let req = test::TestRequest::get().uri("/health").to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);
        let body = response_to_string(resp).await;
        let json: serde_json::Value = serde_json::from_str(&body).unwrap();
        assert_eq!(json.get("status").unwrap(), "healthy");
        assert!(json.get("checks").unwrap().get("database").is_some());
    }

    #[actix_rt::test]
    async fn test_metrics_endpoint() {
        let pool = setup_test_db();
        let app = test::init_service(create_test_app(pool)).await;
        let req = test::TestRequest::get().uri("/metrics").to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);
        assert!(resp
            .headers()
            .get("content-type")
            .unwrap()
            .to_str()
            .unwrap()
            .starts_with("text/plain"));
    }
}

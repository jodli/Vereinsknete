use crate::common::{
    helpers::{create_test_app, response_to_string},
    test_db::setup_test_db,
};
use actix_web::{http::StatusCode, test};
use backend::models::{
    client::NewClient,
    invoice::{InvoiceRequest, UpdateInvoiceStatusRequest},
    session::NewSessionRequest,
    user_profile::UpdateUserProfile,
};
use chrono::{NaiveDate, NaiveTime};

#[cfg(test)]
mod invoice_api_tests {
    use super::*;

    async fn bootstrap<S>(app: &S) -> (i32, i32)
    where
        S: actix_web::dev::Service<
            actix_http::Request,
            Response = actix_web::dev::ServiceResponse,
            Error = actix_web::Error,
        >,
    {
        // Create profile
        let profile = UpdateUserProfile {
            name: Some("Prof Name".into()),
            address: Some("Address 1234567890".into()),
            tax_id: None,
            bank_details: Some("Bank\nIBAN XYZ {invoice_number}".into()),
        };
        let req = test::TestRequest::put()
            .uri("/api/profile")
            .set_json(&profile)
            .to_request();
        let resp = test::call_service(app, req).await;
        assert!(resp.status().is_success());

        // Create client
        let client = NewClient {
            name: "Client X".into(),
            address: "Client Addr 123456".into(),
            contact_person: None,
            default_hourly_rate: 100.0,
        };
        let req = test::TestRequest::post()
            .uri("/api/clients")
            .set_json(&client)
            .to_request();
        let resp = test::call_service(app, req).await;
        assert_eq!(resp.status(), StatusCode::CREATED);
        let body = response_to_string(resp).await;
        let created: serde_json::Value = serde_json::from_str(&body).unwrap();
        let client_id = created.get("id").unwrap().as_i64().unwrap() as i32;

        // Create a session
        let session = NewSessionRequest {
            client_id,
            name: "Work".into(),
            date: NaiveDate::from_ymd_opt(2024, 1, 10).unwrap(),
            start_time: NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(12, 0, 0).unwrap(),
        };
        let req = test::TestRequest::post()
            .uri("/api/sessions")
            .set_json(&session)
            .to_request();
        let resp = test::call_service(app, req).await;
        assert_eq!(resp.status(), StatusCode::CREATED);
        let body_sess = response_to_string(resp).await;
        let sess_json: serde_json::Value = serde_json::from_str(&body_sess).unwrap();
        let session_id = sess_json.get("id").unwrap().as_i64().unwrap() as i32;

        (client_id, session_id)
    }

    #[actix_rt::test]
    async fn test_invoice_generation_and_flow() {
        let pool = setup_test_db();
        let app = test::init_service(create_test_app(pool.clone())).await;
        let (client_id, _session_id) = bootstrap(&app).await;

        // Generate invoice
        let req_body = InvoiceRequest {
            client_id,
            start_date: NaiveDate::from_ymd_opt(2024, 1, 1).unwrap(),
            end_date: NaiveDate::from_ymd_opt(2024, 1, 31).unwrap(),
            language: Some("en".into()),
        };
        let req = test::TestRequest::post()
            .uri("/api/invoices/generate")
            .set_json(&req_body)
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);
        let body = response_to_string(resp).await;
        let gen: serde_json::Value = serde_json::from_str(&body).unwrap();
        let invoice_id = gen.get("invoice_id").unwrap().as_i64().unwrap() as i32;
        assert!(gen.get("pdf_bytes").unwrap().as_str().unwrap().len() > 10);

        // List invoices
        let req = test::TestRequest::get().uri("/api/invoices").to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);

        // Update invoice status -> sent
        let status_req = UpdateInvoiceStatusRequest {
            status: "sent".into(),
            paid_date: None,
        };
        let req = test::TestRequest::patch()
            .uri(&format!("/api/invoices/{}/status", invoice_id))
            .set_json(&status_req)
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);

        // Dashboard metrics (month)
        let req = test::TestRequest::get()
            .uri("/api/dashboard/metrics?period=month&year=2024&month=1")
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);

        // Download PDF
        let req = test::TestRequest::get()
            .uri(&format!("/api/invoices/{}/pdf", invoice_id))
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);
        assert_eq!(
            resp.headers().get("content-type").unwrap(),
            "application/pdf"
        );

        // Delete invoice
        let req = test::TestRequest::delete()
            .uri(&format!("/api/invoices/{}", invoice_id))
            .to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);
    }
}

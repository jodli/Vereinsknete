use chrono::{NaiveDate, NaiveTime};
use diesel::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize, Queryable, Selectable)]
#[diesel(table_name = crate::schema::sessions)]
#[diesel(check_for_backend(diesel::sqlite::Sqlite))]
pub struct Session {
    pub id: i32,
    pub client_id: i32,
    pub name: String,
    pub date: String,
    pub start_time: String,
    pub end_time: String,
    pub created_at: String,
}

#[derive(Debug, Deserialize)]
pub struct NewSessionRequest {
    pub client_id: i32,
    pub name: String,
    pub date: NaiveDate,
    pub start_time: NaiveTime,
    pub end_time: NaiveTime,
}

#[derive(Debug, Insertable)]
#[diesel(table_name = crate::schema::sessions)]
pub struct NewSession {
    pub client_id: i32,
    pub name: String,
    pub date: String,
    pub start_time: String,
    pub end_time: String,
    pub created_at: String,
}

impl From<NewSessionRequest> for NewSession {
    fn from(req: NewSessionRequest) -> Self {
        NewSession {
            client_id: req.client_id,
            name: req.name,
            date: req.date.format("%Y-%m-%d").to_string(),
            start_time: req.start_time.format("%H:%M").to_string(),
            end_time: req.end_time.format("%H:%M").to_string(),
            created_at: chrono::Local::now().format("%Y-%m-%dT%H:%M:%S").to_string(),
        }
    }
}

#[derive(Debug, Serialize)]
pub struct SessionWithDuration {
    #[serde(flatten)]
    pub session: Session,
    pub client_name: String,
    pub duration_minutes: i64,
}

#[derive(Debug, Deserialize)]
pub struct SessionFilterParams {
    pub client_id: Option<i32>,
    pub start_date: Option<NaiveDate>,
    pub end_date: Option<NaiveDate>,
}

use diesel::prelude::*;
use chrono::NaiveTime;
use crate::DbPool;
use crate::models::session::{Session, NewSession, NewSessionRequest, SessionFilterParams, SessionWithDuration};
use crate::models::client::Client;

pub fn create_session(pool: &DbPool, session_req: NewSessionRequest) -> Result<Session, diesel::result::Error> {
    use crate::schema::sessions;

    let mut conn = pool.get().expect("Failed to get DB connection");
    let new_session = NewSession::from(session_req);

    diesel::insert_into(sessions::table)
        .values(&new_session)
        .returning(Session::as_returning())
        .get_result(&mut conn)
}

pub fn get_all_sessions(pool: &DbPool, filter: Option<SessionFilterParams>) -> Result<Vec<SessionWithDuration>, diesel::result::Error> {
    use crate::schema::sessions::dsl::*;
    use crate::schema::clients;

    let mut conn = pool.get().expect("Failed to get DB connection");
    let mut query = sessions.inner_join(clients::table).into_boxed();

    if let Some(filter_params) = filter {
        if let Some(client_filter) = filter_params.client_id {
            query = query.filter(client_id.eq(client_filter));
        }

        if let Some(start) = filter_params.start_date {
            query = query.filter(date.ge(start.format("%Y-%m-%d").to_string()));
        }

        if let Some(end) = filter_params.end_date {
            query = query.filter(date.le(end.format("%Y-%m-%d").to_string()));
        }
    }

    let results: Vec<(Session, Client)> = query
        .load::<(Session, Client)>(&mut conn)?;

    let sessions_with_duration = results
        .into_iter()
        .map(|(session, client)| {
            let start = NaiveTime::parse_from_str(&session.start_time, "%H:%M").unwrap_or_default();
            let end = NaiveTime::parse_from_str(&session.end_time, "%H:%M").unwrap_or_default();

            // Calculate duration in minutes
            let duration_minutes = if end < start {
                // Handle sessions that go past midnight
                (end + chrono::Duration::hours(24) - start).num_minutes()
            } else {
                (end - start).num_minutes()
            };

            SessionWithDuration {
                session,
                client_name: client.name,
                duration_minutes,
            }
        })
        .collect();

    Ok(sessions_with_duration)
}

pub fn get_sessions_by_client(pool: &DbPool, client_id: i32) -> Result<Vec<Session>, diesel::result::Error> {
    use crate::schema::sessions::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");
    sessions
        .filter(client_id.eq(client_id))
        .select(Session::as_select())
        .load(&mut conn)
}

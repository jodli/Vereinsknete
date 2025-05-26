use crate::models::client::Client;
use crate::models::session::{
    NewSession, NewSessionRequest, Session, SessionFilterParams, SessionWithDuration, UpdateSession, UpdateSessionRequest,
};
use crate::DbPool;
use chrono::NaiveTime;
use diesel::prelude::*;

pub fn create_session(
    pool: &DbPool,
    session_req: NewSessionRequest,
) -> Result<Session, diesel::result::Error> {
    use crate::schema::sessions;
    use crate::schema::sessions::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");
    let new_session = NewSession::from(session_req);

    diesel::insert_into(sessions::table)
        .values(&new_session)
        .execute(&mut conn)?;

    // SQLite doesn't support returning clause, so we'll fetch the inserted session by id
    sessions
        .order(id.desc())
        .limit(1)
        .select(Session::as_select())
        .get_result(&mut conn)
}

pub fn get_all_sessions(
    pool: &DbPool,
    filter: Option<SessionFilterParams>,
) -> Result<Vec<SessionWithDuration>, diesel::result::Error> {
    use crate::schema::clients::dsl::clients;
    use crate::schema::sessions::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");
    let mut query = sessions.into_boxed();

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

    // First get all sessions
    let session_results: Vec<Session> = query.select(Session::as_select()).load(&mut conn)?;

    // Now build the results with clients
    let mut results = Vec::new();
    for session in session_results {
        let client = clients
            .find(session.client_id)
            .select(Client::as_select())
            .first(&mut conn)?;

        results.push((session, client));
    }

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

pub fn get_sessions_by_client(
    pool: &DbPool,
    client_id: i32,
) -> Result<Vec<Session>, diesel::result::Error> {
    let mut conn = pool.get().expect("Failed to get DB connection");

    crate::schema::sessions::dsl::sessions
        .filter(crate::schema::sessions::client_id.eq(client_id))
        .select(Session::as_select())
        .load(&mut conn)
}

pub fn get_session_by_id(
    pool: &DbPool,
    session_id: i32,
) -> Result<Session, diesel::result::Error> {
    use crate::schema::sessions::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");

    sessions
        .find(session_id)
        .select(Session::as_select())
        .first(&mut conn)
}

pub fn update_session(
    pool: &DbPool,
    session_id: i32,
    session_req: UpdateSessionRequest,
) -> Result<Session, diesel::result::Error> {
    use crate::schema::sessions::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");
    let update_session = UpdateSession::from(session_req);

    diesel::update(sessions.find(session_id))
        .set(&update_session)
        .execute(&mut conn)?;

    // Fetch the updated session
    sessions
        .find(session_id)
        .select(Session::as_select())
        .first(&mut conn)
}

pub fn delete_session(
    pool: &DbPool,
    session_id: i32,
) -> Result<(), diesel::result::Error> {
    use crate::schema::sessions::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");

    diesel::delete(sessions.find(session_id))
        .execute(&mut conn)
        .map(|_| ())
}

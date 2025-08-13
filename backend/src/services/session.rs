use crate::models::client::Client;
use crate::models::session::{
    NewSession, NewSessionRequest, Session, SessionFilterParams, SessionWithDuration,
    UpdateSession, UpdateSessionRequest,
};
use crate::DbPool;
use chrono::NaiveTime;
use diesel::prelude::*;

/// Creates a new session in the database
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `session_req` - Session data to create
///
/// # Returns
/// * `Result<Session, diesel::result::Error>` - Created session or database error
pub fn create_session(
    pool: &DbPool,
    session_req: NewSessionRequest,
) -> Result<Session, diesel::result::Error> {
    use crate::schema::sessions;
    use crate::schema::sessions::dsl::*;

    // Business logic validation
    if session_req.client_id <= 0 {
        log::warn!(
            "Attempted to create session with invalid client ID: {}",
            session_req.client_id
        );
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::CheckViolation,
            Box::new("Invalid client ID".to_string()),
        ));
    }

    if session_req.name.trim().is_empty() {
        log::warn!("Attempted to create session with empty name");
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::CheckViolation,
            Box::new("Session name cannot be empty".to_string()),
        ));
    }

    if session_req.end_time <= session_req.start_time {
        log::warn!(
            "Attempted to create session with invalid time range: {} - {}",
            session_req.start_time,
            session_req.end_time
        );
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::CheckViolation,
            Box::new("End time must be after start time".to_string()),
        ));
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::info!(
        "Creating new session: {} for client {}",
        session_req.name,
        session_req.client_id
    );

    // Verify client exists
    use crate::schema::clients;
    let client_exists: i64 = clients::table
        .filter(clients::id.eq(session_req.client_id))
        .select(diesel::dsl::count_star())
        .first(&mut conn)?;

    if client_exists == 0 {
        log::warn!(
            "Attempted to create session for non-existent client: {}",
            session_req.client_id
        );
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::ForeignKeyViolation,
            Box::new("Client does not exist".to_string()),
        ));
    }

    let new_session = NewSession::from(session_req);

    diesel::insert_into(sessions::table)
        .values(&new_session)
        .execute(&mut conn)?;

    // SQLite doesn't support RETURNING, so fetch the inserted session
    let result = sessions
        .order(id.desc())
        .limit(1)
        .select(Session::as_select())
        .get_result(&mut conn);

    match &result {
        Ok(session) => log::info!("Successfully created session with ID: {}", session.id),
        Err(e) => log::error!("Failed to create session: {}", e),
    }

    result
}

/// Retrieves all sessions with optional filtering
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `filter` - Optional filter parameters
///
/// # Returns
/// * `Result<Vec<SessionWithDuration>, diesel::result::Error>` - List of sessions with duration or database error
pub fn get_all_sessions(
    pool: &DbPool,
    filter: Option<SessionFilterParams>,
) -> Result<Vec<SessionWithDuration>, diesel::result::Error> {
    use crate::schema::clients::dsl::clients;
    use crate::schema::sessions::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");
    let mut query = sessions.into_boxed();

    log::debug!("Fetching sessions with filters: {:?}", filter);

    // Apply filters
    if let Some(filter_params) = filter {
        if let Some(client_filter) = filter_params.client_id {
            if client_filter <= 0 {
                log::warn!("Invalid client ID filter: {}", client_filter);
                return Err(diesel::result::Error::DatabaseError(
                    diesel::result::DatabaseErrorKind::CheckViolation,
                    Box::new("Invalid client ID filter".to_string()),
                ));
            }
            query = query.filter(client_id.eq(client_filter));
        }

        if let Some(start) = filter_params.start_date {
            query = query.filter(date.ge(start.format("%Y-%m-%d").to_string()));
        }

        if let Some(end) = filter_params.end_date {
            query = query.filter(date.le(end.format("%Y-%m-%d").to_string()));
        }

        // Validate date range
        if let (Some(start), Some(end)) = (filter_params.start_date, filter_params.end_date) {
            if end < start {
                log::warn!("Invalid date range: {} to {}", start, end);
                return Err(diesel::result::Error::DatabaseError(
                    diesel::result::DatabaseErrorKind::CheckViolation,
                    Box::new("End date must be after start date".to_string()),
                ));
            }
        }
    }

    // Get all sessions
    let session_results: Vec<Session> = query.select(Session::as_select()).load(&mut conn)?;

    log::debug!("Found {} sessions", session_results.len());

    // Build results with client information
    let mut results = Vec::new();
    for session in session_results {
        let client = clients
            .find(session.client_id)
            .select(Client::as_select())
            .first(&mut conn)?;

        results.push((session, client));
    }

    let sessions_with_duration: Vec<_> = results
        .into_iter()
        .map(|(session, client)| {
            let start = NaiveTime::parse_from_str(&session.start_time, "%H:%M").unwrap_or_default();
            let end = NaiveTime::parse_from_str(&session.end_time, "%H:%M").unwrap_or_default();

            // Calculate duration in minutes
            let duration_minutes = if end < start {
                // Handle sessions that go past midnight (end time next day)
                // NaiveTime arithmetic wraps at 24h, so we can't add 24h to end directly.
                // Example: 23:00 -> 01:00 should be 2h (120m).
                // Compute as 24h - (start - end).
                (chrono::Duration::hours(24) - (start - end)).num_minutes()
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

    log::debug!(
        "Successfully processed {} sessions with duration",
        sessions_with_duration.len()
    );
    Ok(sessions_with_duration)
}

/// Retrieves all sessions for a specific client
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `client_id` - ID of the client
///
/// # Returns
/// * `Result<Vec<Session>, diesel::result::Error>` - List of sessions or database error
pub fn get_sessions_by_client(
    pool: &DbPool,
    client_id: i32,
) -> Result<Vec<Session>, diesel::result::Error> {
    // Validate input
    if client_id <= 0 {
        log::warn!("Invalid client ID for session lookup: {}", client_id);
        return Err(diesel::result::Error::NotFound);
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::debug!("Fetching sessions for client: {}", client_id);

    let result = crate::schema::sessions::dsl::sessions
        .filter(crate::schema::sessions::client_id.eq(client_id))
        .select(Session::as_select())
        .load(&mut conn);

    match &result {
        Ok(sessions_list) => log::debug!(
            "Found {} sessions for client {}",
            sessions_list.len(),
            client_id
        ),
        Err(e) => log::error!("Failed to fetch sessions for client {}: {}", client_id, e),
    }

    result
}

/// Retrieves a specific session by ID
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `session_id` - ID of the session to retrieve
///
/// # Returns
/// * `Result<Session, diesel::result::Error>` - Session or database error
pub fn get_session_by_id(pool: &DbPool, session_id: i32) -> Result<Session, diesel::result::Error> {
    use crate::schema::sessions::dsl::*;

    // Validate input
    if session_id <= 0 {
        log::warn!("Invalid session ID: {}", session_id);
        return Err(diesel::result::Error::NotFound);
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::debug!("Fetching session with ID: {}", session_id);

    let result = sessions
        .find(session_id)
        .select(Session::as_select())
        .first(&mut conn);

    match &result {
        Ok(_) => log::debug!("Successfully found session with ID: {}", session_id),
        Err(e) => log::error!("Failed to fetch session {}: {}", session_id, e),
    }

    result
}

/// Updates an existing session in the database
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `session_id` - ID of the session to update
/// * `session_req` - Updated session data
///
/// # Returns
/// * `Result<Session, diesel::result::Error>` - Updated session or database error
pub fn update_session(
    pool: &DbPool,
    session_id: i32,
    session_req: UpdateSessionRequest,
) -> Result<Session, diesel::result::Error> {
    use crate::schema::sessions::dsl::*;

    // Validate input
    if session_id <= 0 {
        log::warn!("Invalid session ID for update: {}", session_id);
        return Err(diesel::result::Error::NotFound);
    }

    // Business logic validation
    if session_req.client_id <= 0 {
        log::warn!(
            "Attempted to update session {} with invalid client ID: {}",
            session_id,
            session_req.client_id
        );
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::CheckViolation,
            Box::new("Invalid client ID".to_string()),
        ));
    }

    if session_req.name.trim().is_empty() {
        log::warn!("Attempted to update session {} with empty name", session_id);
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::CheckViolation,
            Box::new("Session name cannot be empty".to_string()),
        ));
    }

    if session_req.end_time <= session_req.start_time {
        log::warn!(
            "Attempted to update session {} with invalid time range: {} - {}",
            session_id,
            session_req.start_time,
            session_req.end_time
        );
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::CheckViolation,
            Box::new("End time must be after start time".to_string()),
        ));
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::info!("Updating session with ID: {}", session_id);

    // Check if session exists
    let existing_session = sessions
        .find(session_id)
        .select(Session::as_select())
        .first(&mut conn)
        .optional()?;

    if existing_session.is_none() {
        log::warn!("Attempted to update non-existent session: {}", session_id);
        return Err(diesel::result::Error::NotFound);
    }

    // Verify client exists
    use crate::schema::clients;
    let client_exists: i64 = clients::table
        .filter(clients::id.eq(session_req.client_id))
        .select(diesel::dsl::count_star())
        .first(&mut conn)?;

    if client_exists == 0 {
        log::warn!(
            "Attempted to update session {} with non-existent client: {}",
            session_id,
            session_req.client_id
        );
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::ForeignKeyViolation,
            Box::new("Client does not exist".to_string()),
        ));
    }

    let update_session = UpdateSession::from(session_req);

    diesel::update(sessions.find(session_id))
        .set(&update_session)
        .execute(&mut conn)?;

    // Fetch the updated session
    let result = sessions
        .find(session_id)
        .select(Session::as_select())
        .first(&mut conn);

    match &result {
        Ok(_) => log::info!("Successfully updated session with ID: {}", session_id),
        Err(e) => log::error!("Failed to update session {}: {}", session_id, e),
    }

    result
}

/// Deletes a session from the database
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `session_id` - ID of the session to delete
///
/// # Returns
/// * `Result<(), diesel::result::Error>` - Success or database error
pub fn delete_session(pool: &DbPool, session_id: i32) -> Result<(), diesel::result::Error> {
    use crate::schema::sessions::dsl::*;

    // Validate input
    if session_id <= 0 {
        log::warn!("Invalid session ID for deletion: {}", session_id);
        return Err(diesel::result::Error::NotFound);
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::info!("Deleting session with ID: {}", session_id);

    // Check if session is used in any invoices
    use crate::schema::invoices;
    let invoice_count: i64 = invoices::table
        .select(diesel::dsl::count_star())
        .first(&mut conn)?;

    // Note: This is a simplified check. In a real application, you'd need to check
    // if the session is within the date range of any existing invoices for the same client
    if invoice_count > 0 {
        log::debug!(
            "Session {} may be referenced in existing invoices",
            session_id
        );
    }

    let result = diesel::delete(sessions.find(session_id))
        .execute(&mut conn)
        .map(|count| {
            if count > 0 {
                log::info!("Successfully deleted session with ID: {}", session_id);
            } else {
                log::warn!("No session found to delete with ID: {}", session_id);
            }
        });

    if let Err(ref e) = result {
        log::error!("Failed to delete session {}: {}", session_id, e);
    }

    result
}

#[cfg(test)]
mod tests {
    use super::*;
    use chrono::{NaiveDate, NaiveTime};
    use diesel_migrations::{embed_migrations, EmbeddedMigrations, MigrationHarness};
    use std::sync::atomic::{AtomicU32, Ordering};

    use crate::models::session::SessionFilterParams;

    const MIGRATIONS: EmbeddedMigrations = embed_migrations!("migrations");
    static DB_COUNTER: AtomicU32 = AtomicU32::new(0);

    fn setup_pool() -> DbPool {
        let count = DB_COUNTER.fetch_add(1, Ordering::SeqCst) + 1;
        let db_name = format!(
            "file:session_service_test_{}?mode=memory&cache=shared",
            count
        );
        let manager = diesel::r2d2::ConnectionManager::<SqliteConnection>::new(db_name);
        let pool = diesel::r2d2::Pool::builder()
            .max_size(1)
            .build(manager)
            .unwrap();
        {
            let mut conn = pool.get().unwrap();
            conn.run_pending_migrations(MIGRATIONS).unwrap();
        }
        pool
    }

    fn insert_client(pool: &DbPool, name_val: &str) -> i32 {
        use crate::schema::clients;
        #[derive(Insertable)]
        #[diesel(table_name = crate::schema::clients)]
        struct TestClient {
            name: String,
            address: String,
            contact_person: Option<String>,
            default_hourly_rate: f32,
        }
        let client = TestClient {
            name: name_val.into(),
            address: "Street 1".into(),
            contact_person: None,
            default_hourly_rate: 50.0,
        };
        let mut conn = pool.get().unwrap();
        diesel::insert_into(clients::table)
            .values(&client)
            .execute(&mut conn)
            .unwrap();
        // fetch id
        use crate::schema::clients::dsl::*;
        clients
            .order(id.desc())
            .select(id)
            .first(&mut conn)
            .unwrap()
    }

    fn valid_new_session_req(client_id: i32) -> NewSessionRequest {
        NewSessionRequest {
            client_id,
            name: "Consulting".into(),
            date: NaiveDate::from_ymd_opt(2025, 1, 10).unwrap(),
            start_time: NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(11, 0, 0).unwrap(),
        }
    }

    #[test]
    fn create_session_success() {
        let pool = setup_pool();
        let cid = insert_client(&pool, "Acme");
        let s = create_session(&pool, valid_new_session_req(cid)).unwrap();
        assert_eq!(s.client_id, cid);
        assert_eq!(s.name, "Consulting");
    }

    #[test]
    fn create_session_invalid_client_id_check_violation() {
        let pool = setup_pool();
        let req = valid_new_session_req(0);
        let err = create_session(&pool, req).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                _
            )
        );
    }

    #[test]
    fn create_session_empty_name_fails() {
        let pool = setup_pool();
        let cid = insert_client(&pool, "Acme");
        let mut req = valid_new_session_req(cid);
        req.name = "   ".into();
        let err = create_session(&pool, req).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                _
            )
        );
    }

    #[test]
    fn create_session_invalid_time_range_fails() {
        let pool = setup_pool();
        let cid = insert_client(&pool, "Acme");
        let mut req = valid_new_session_req(cid);
        req.end_time = req.start_time; // end == start
        let err = create_session(&pool, req).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                _
            )
        );
    }

    #[test]
    fn create_session_nonexistent_client_fk_violation() {
        let pool = setup_pool();
        // Do not insert client
        let req = valid_new_session_req(9999);
        let err = create_session(&pool, req).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::ForeignKeyViolation,
                _
            )
        );
    }

    #[test]
    fn get_all_sessions_basic_and_duration() {
        let pool = setup_pool();
        let cid = insert_client(&pool, "Acme");
        create_session(&pool, valid_new_session_req(cid)).unwrap();
        let list = get_all_sessions(&pool, None).unwrap();
        assert_eq!(list.len(), 1);
        assert_eq!(list[0].duration_minutes, 120);
    }

    #[test]
    fn get_all_sessions_invalid_client_filter() {
        let pool = setup_pool();
        let filter = SessionFilterParams {
            client_id: Some(0),
            start_date: None,
            end_date: None,
        };
        let err = get_all_sessions(&pool, Some(filter)).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                _
            )
        );
    }

    #[test]
    fn get_all_sessions_invalid_date_range() {
        let pool = setup_pool();
        let cid = insert_client(&pool, "Acme");
        create_session(&pool, valid_new_session_req(cid)).unwrap();
        let filter = SessionFilterParams {
            client_id: None,
            start_date: Some(NaiveDate::from_ymd_opt(2025, 2, 1).unwrap()),
            end_date: Some(NaiveDate::from_ymd_opt(2025, 1, 1).unwrap()),
        };
        let err = get_all_sessions(&pool, Some(filter)).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                _
            )
        );
    }

    #[test]
    fn get_all_sessions_overnight_duration() {
        let pool = setup_pool();
        let cid = insert_client(&pool, "NightCo");
        // Simulate an overnight span by inserting a record with start 23:00 and end 01:00 (next day) which the service wraps.
        use crate::schema::sessions;
        #[derive(Insertable)]
        #[diesel(table_name = crate::schema::sessions)]
        struct TestSession {
            client_id: i32,
            name: String,
            date: String,
            start_time: String,
            end_time: String,
            created_at: String,
        }
        {
            let mut conn = pool.get().unwrap();
            let sess = TestSession {
                client_id: cid,
                name: "Overnight".into(),
                date: "2025-01-10".into(),
                start_time: "23:00".into(),
                end_time: "01:00".into(),
                created_at: "2025-01-10T23:00:00".into(),
            };
            diesel::insert_into(sessions::table)
                .values(&sess)
                .execute(&mut conn)
                .unwrap();
        }
        let list = get_all_sessions(&pool, None).unwrap();
        assert_eq!(list.len(), 1);
        assert_eq!(list[0].duration_minutes, 120);
    }

    #[test]
    fn update_session_success() {
        let pool = setup_pool();
        let cid = insert_client(&pool, "Acme");
        let s = create_session(&pool, valid_new_session_req(cid)).unwrap();
        let req = UpdateSessionRequest {
            client_id: cid,
            name: "Updated".into(),
            date: NaiveDate::from_ymd_opt(2025, 1, 11).unwrap(),
            start_time: NaiveTime::from_hms_opt(10, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(12, 0, 0).unwrap(),
        };
        let updated = update_session(&pool, s.id, req).unwrap();
        assert_eq!(updated.name, "Updated");
        assert_eq!(updated.date, "2025-01-11");
    }

    #[test]
    fn update_session_invalid_id() {
        let pool = setup_pool();
        let cid = insert_client(&pool, "Acme");
        let req = UpdateSessionRequest {
            client_id: cid,
            name: "Updated".into(),
            date: NaiveDate::from_ymd_opt(2025, 1, 11).unwrap(),
            start_time: NaiveTime::from_hms_opt(10, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(12, 0, 0).unwrap(),
        };
        let err = update_session(&pool, 0, req).unwrap_err();
        matches!(err, diesel::result::Error::NotFound);
    }

    #[test]
    fn update_session_nonexistent_session() {
        let pool = setup_pool();
        let cid = insert_client(&pool, "Acme");
        let req = UpdateSessionRequest {
            client_id: cid,
            name: "Updated".into(),
            date: NaiveDate::from_ymd_opt(2025, 1, 11).unwrap(),
            start_time: NaiveTime::from_hms_opt(10, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(12, 0, 0).unwrap(),
        };
        let err = update_session(&pool, 12345, req).unwrap_err();
        matches!(err, diesel::result::Error::NotFound);
    }

    #[test]
    fn update_session_nonexistent_client_fk_violation() {
        let pool = setup_pool();
        let cid = insert_client(&pool, "Acme");
        let s = create_session(&pool, valid_new_session_req(cid)).unwrap();
        let req = UpdateSessionRequest {
            client_id: 9999,
            name: "Updated".into(),
            date: NaiveDate::from_ymd_opt(2025, 1, 11).unwrap(),
            start_time: NaiveTime::from_hms_opt(10, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(12, 0, 0).unwrap(),
        };
        let err = update_session(&pool, s.id, req).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::ForeignKeyViolation,
                _
            )
        );
    }

    #[test]
    fn update_session_invalid_time_range() {
        let pool = setup_pool();
        let cid = insert_client(&pool, "Acme");
        let s = create_session(&pool, valid_new_session_req(cid)).unwrap();
        let req = UpdateSessionRequest {
            client_id: cid,
            name: "Updated".into(),
            date: NaiveDate::from_ymd_opt(2025, 1, 11).unwrap(),
            start_time: NaiveTime::from_hms_opt(12, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(10, 0, 0).unwrap(),
        };
        let err = update_session(&pool, s.id, req).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                _
            )
        );
    }

    #[test]
    fn delete_session_success() {
        let pool = setup_pool();
        let cid = insert_client(&pool, "Acme");
        let s = create_session(&pool, valid_new_session_req(cid)).unwrap();
        delete_session(&pool, s.id).unwrap();
        // Confirm deletion
        use crate::schema::sessions::dsl::*;
        let mut conn = pool.get().unwrap();
        let count: i64 = sessions
            .filter(id.eq(s.id))
            .select(diesel::dsl::count_star())
            .first(&mut conn)
            .unwrap();
        assert_eq!(count, 0);
    }

    #[test]
    fn delete_session_invalid_id() {
        let pool = setup_pool();
        let err = delete_session(&pool, 0).unwrap_err();
        matches!(err, diesel::result::Error::NotFound);
    }
}

use crate::models::client::{Client, NewClient, UpdateClient};
use crate::DbPool;
use diesel::prelude::*;

/// Retrieves all clients from the database
///
/// # Arguments
/// * `pool` - Database connection pool
///
/// # Returns
/// * `Result<Vec<Client>, diesel::result::Error>` - List of all clients or database error
pub fn get_all_clients(pool: &DbPool) -> Result<Vec<Client>, diesel::result::Error> {
    use crate::schema::clients::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::debug!("Fetching all clients from database");

    let result = clients.select(Client::as_select()).load(&mut conn);

    match &result {
        Ok(clients_list) => log::debug!("Successfully fetched {} clients", clients_list.len()),
        Err(e) => log::error!("Failed to fetch clients: {}", e),
    }

    result
}

/// Retrieves a specific client by ID
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `client_id` - ID of the client to retrieve
///
/// # Returns
/// * `Result<Option<Client>, diesel::result::Error>` - Client if found, None if not found, or database error
pub fn get_client_by_id(
    pool: &DbPool,
    client_id: i32,
) -> Result<Option<Client>, diesel::result::Error> {
    use crate::schema::clients::dsl::*;

    // Validate input
    if client_id <= 0 {
        log::warn!("Invalid client ID provided: {}", client_id);
        return Err(diesel::result::Error::NotFound);
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::debug!("Fetching client with ID: {}", client_id);

    let result = clients
        .filter(id.eq(client_id))
        .select(Client::as_select())
        .first(&mut conn)
        .optional();

    match &result {
        Ok(Some(_)) => log::debug!("Successfully found client with ID: {}", client_id),
        Ok(None) => log::debug!("No client found with ID: {}", client_id),
        Err(e) => log::error!("Failed to fetch client {}: {}", client_id, e),
    }

    result
}

/// Creates a new client in the database
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `new_client` - Client data to create
///
/// # Returns
/// * `Result<Client, diesel::result::Error>` - Created client or database error
pub fn create_client(
    pool: &DbPool,
    new_client: NewClient,
) -> Result<Client, diesel::result::Error> {
    use crate::schema::clients;
    use crate::schema::clients::dsl::*;

    // Business logic validation
    if new_client.name.trim().is_empty() {
        log::warn!("Attempted to create client with empty name");
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::CheckViolation,
            Box::new("Client name cannot be empty".to_string()),
        ));
    }

    if new_client.default_hourly_rate < 0.0 {
        log::warn!(
            "Attempted to create client with negative hourly rate: {}",
            new_client.default_hourly_rate
        );
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::CheckViolation,
            Box::new("Hourly rate cannot be negative".to_string()),
        ));
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::info!("Creating new client: {}", new_client.name);

    // Check for duplicate names
    let existing_count: i64 = clients
        .filter(name.eq(&new_client.name))
        .select(diesel::dsl::count_star())
        .first(&mut conn)?;

    if existing_count > 0 {
        log::warn!(
            "Attempted to create client with duplicate name: {}",
            new_client.name
        );
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::UniqueViolation,
            Box::new("Client name already exists".to_string()),
        ));
    }

    diesel::insert_into(clients::table)
        .values(&new_client)
        .execute(&mut conn)?;

    // SQLite doesn't support RETURNING, so fetch the inserted client
    let result = clients
        .order(id.desc())
        .limit(1)
        .select(Client::as_select())
        .get_result(&mut conn);

    match &result {
        Ok(client) => log::info!("Successfully created client with ID: {}", client.id),
        Err(e) => log::error!("Failed to create client: {}", e),
    }

    result
}

/// Updates an existing client in the database
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `client_id` - ID of the client to update
/// * `update_client` - Updated client data
///
/// # Returns
/// * `Result<Client, diesel::result::Error>` - Updated client or database error
pub fn update_client(
    pool: &DbPool,
    client_id: i32,
    update_client: UpdateClient,
) -> Result<Client, diesel::result::Error> {
    use crate::schema::clients::dsl::*;

    // Validate input
    if client_id <= 0 {
        log::warn!("Invalid client ID for update: {}", client_id);
        return Err(diesel::result::Error::NotFound);
    }

    // Business logic validation
    if let Some(ref client_name) = update_client.name {
        if client_name.trim().is_empty() {
            log::warn!("Attempted to update client {} with empty name", client_id);
            return Err(diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                Box::new("Client name cannot be empty".to_string()),
            ));
        }
    }

    if let Some(rate) = update_client.default_hourly_rate {
        if rate < 0.0 {
            log::warn!(
                "Attempted to update client {} with negative hourly rate: {}",
                client_id,
                rate
            );
            return Err(diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                Box::new("Hourly rate cannot be negative".to_string()),
            ));
        }
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::info!("Updating client with ID: {}", client_id);

    // Check if client exists
    let existing_client = clients
        .filter(id.eq(client_id))
        .select(Client::as_select())
        .first(&mut conn)
        .optional()?;

    if existing_client.is_none() {
        log::warn!("Attempted to update non-existent client: {}", client_id);
        return Err(diesel::result::Error::NotFound);
    }

    // Check for duplicate names if name is being updated
    if let Some(ref new_name) = update_client.name {
        let duplicate_count: i64 = clients
            .filter(name.eq(new_name))
            .filter(id.ne(client_id))
            .select(diesel::dsl::count_star())
            .first(&mut conn)?;

        if duplicate_count > 0 {
            log::warn!(
                "Attempted to update client {} with duplicate name: {}",
                client_id,
                new_name
            );
            return Err(diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::UniqueViolation,
                Box::new("Client name already exists".to_string()),
            ));
        }
    }

    diesel::update(clients.filter(id.eq(client_id)))
        .set(&update_client)
        .execute(&mut conn)?;

    // Fetch the updated record
    let result = clients
        .filter(id.eq(client_id))
        .select(Client::as_select())
        .get_result(&mut conn);

    match &result {
        Ok(_) => log::info!("Successfully updated client with ID: {}", client_id),
        Err(e) => log::error!("Failed to update client {}: {}", client_id, e),
    }

    result
}

/// Deletes a client from the database
///
/// # Arguments
/// * `pool` - Database connection pool
/// * `client_id` - ID of the client to delete
///
/// # Returns
/// * `Result<usize, diesel::result::Error>` - Number of deleted records or database error
pub fn delete_client(pool: &DbPool, client_id: i32) -> Result<usize, diesel::result::Error> {
    use crate::schema::clients::dsl::*;

    // Validate input
    if client_id <= 0 {
        log::warn!("Invalid client ID for deletion: {}", client_id);
        return Err(diesel::result::Error::NotFound);
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::info!("Deleting client with ID: {}", client_id);

    // Check if client has associated sessions
    use crate::schema::sessions;
    let session_count: i64 = sessions::table
        .filter(sessions::client_id.eq(client_id))
        .select(diesel::dsl::count_star())
        .first(&mut conn)?;

    if session_count > 0 {
        log::warn!(
            "Attempted to delete client {} with {} associated sessions",
            client_id,
            session_count
        );
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::ForeignKeyViolation,
            Box::new(format!(
                "Cannot delete client with {} associated sessions",
                session_count
            )),
        ));
    }

    let result = diesel::delete(clients.filter(id.eq(client_id))).execute(&mut conn);

    match &result {
        Ok(count) => {
            if *count > 0 {
                log::info!("Successfully deleted client with ID: {}", client_id);
            } else {
                log::warn!("No client found to delete with ID: {}", client_id);
            }
        }
        Err(e) => log::error!("Failed to delete client {}: {}", client_id, e),
    }

    result
}

#[cfg(test)]
mod tests {
    use super::*;
    use diesel_migrations::{embed_migrations, EmbeddedMigrations, MigrationHarness};
    use std::sync::atomic::{AtomicU32, Ordering};

    const MIGRATIONS: EmbeddedMigrations = embed_migrations!("migrations");

    static DB_COUNTER: AtomicU32 = AtomicU32::new(0);

    fn setup_pool() -> DbPool {
        let count = DB_COUNTER.fetch_add(1, Ordering::SeqCst) + 1;
        let db_name = format!(
            "file:client_service_test_{}?mode=memory&cache=shared",
            count
        );
        let manager = diesel::r2d2::ConnectionManager::<SqliteConnection>::new(db_name);
        let pool = diesel::r2d2::Pool::builder()
            .max_size(1)
            .build(manager)
            .expect("failed to build pool");
        // Run migrations
        {
            let mut conn = pool.get().unwrap();
            conn.run_pending_migrations(MIGRATIONS).unwrap();
        }
        pool
    }

    fn new_client(name: &str, rate: f32) -> NewClient {
        NewClient {
            name: name.to_string(),
            address: "Teststr. 1".to_string(),
            contact_person: Some("Tester".to_string()),
            default_hourly_rate: rate,
        }
    }

    #[test]
    fn create_client_success() {
        let pool = setup_pool();
        let c = create_client(&pool, new_client("Acme", 120.0)).expect("should create");
        assert_eq!(c.name, "Acme");
        // get by id happy path
        let fetched = get_client_by_id(&pool, c.id).unwrap();
        assert!(fetched.is_some());
    }

    #[test]
    fn create_client_empty_name_fails() {
        let pool = setup_pool();
        let err = create_client(&pool, new_client("", 100.0)).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                _
            )
        );
    }

    #[test]
    fn create_client_negative_rate_fails() {
        let pool = setup_pool();
        let err = create_client(&pool, new_client("Valid", -1.0)).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                _
            )
        );
    }

    #[test]
    fn create_client_duplicate_name_fails() {
        let pool = setup_pool();
        create_client(&pool, new_client("Dup", 50.0)).unwrap();
        let err = create_client(&pool, new_client("Dup", 60.0)).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::UniqueViolation,
                _
            )
        );
    }

    #[test]
    fn get_client_by_id_invalid_id() {
        let pool = setup_pool();
        let err = get_client_by_id(&pool, 0).unwrap_err();
        matches!(err, diesel::result::Error::NotFound);
    }

    #[test]
    fn get_client_by_id_nonexistent_returns_none() {
        let pool = setup_pool();
        // create a different client so table not empty
        create_client(&pool, new_client("Someone", 10.0)).unwrap();
        let result = get_client_by_id(&pool, 999).unwrap();
        assert!(result.is_none());
    }

    #[test]
    fn update_client_success() {
        let pool = setup_pool();
        let c = create_client(&pool, new_client("Old", 10.0)).unwrap();
        let upd = UpdateClient {
            name: Some("New".into()),
            address: None,
            contact_person: None,
            default_hourly_rate: Some(25.0),
        };
        let updated = update_client(&pool, c.id, upd).unwrap();
        assert_eq!(updated.name, "New");
        assert!((updated.default_hourly_rate - 25.0).abs() < f32::EPSILON);
    }

    #[test]
    fn update_client_duplicate_name_fails() {
        let pool = setup_pool();
        let c1 = create_client(&pool, new_client("C1", 10.0)).unwrap();
        let _c2 = create_client(&pool, new_client("C2", 20.0)).unwrap();
        let upd = UpdateClient {
            name: Some("C2".into()),
            address: None,
            contact_person: None,
            default_hourly_rate: None,
        };
        let err = update_client(&pool, c1.id, upd).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::UniqueViolation,
                _
            )
        );
    }

    #[test]
    fn update_client_invalid_rate_fails() {
        let pool = setup_pool();
        let c = create_client(&pool, new_client("Test", 10.0)).unwrap();
        let upd = UpdateClient {
            name: None,
            address: None,
            contact_person: None,
            default_hourly_rate: Some(-5.0),
        };
        let err = update_client(&pool, c.id, upd).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                _
            )
        );
    }

    #[test]
    fn update_client_empty_name_fails() {
        let pool = setup_pool();
        let c = create_client(&pool, new_client("Test", 10.0)).unwrap();
        let upd = UpdateClient {
            name: Some("   ".into()),
            address: None,
            contact_person: None,
            default_hourly_rate: None,
        };
        let err = update_client(&pool, c.id, upd).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::CheckViolation,
                _
            )
        );
    }

    #[test]
    fn update_client_nonexistent_id() {
        let pool = setup_pool();
        let err = update_client(
            &pool,
            12345,
            UpdateClient {
                name: Some("X".into()),
                address: None,
                contact_person: None,
                default_hourly_rate: None,
            },
        )
        .unwrap_err();
        matches!(err, diesel::result::Error::NotFound);
    }

    #[test]
    fn delete_client_success() {
        let pool = setup_pool();
        let c = create_client(&pool, new_client("ToDelete", 10.0)).unwrap();
        let deleted = delete_client(&pool, c.id).unwrap();
        assert_eq!(deleted, 1);
    }

    #[test]
    fn delete_client_with_sessions_fails() {
        let pool = setup_pool();
        let c = create_client(&pool, new_client("WithSessions", 10.0)).unwrap();
        // Insert a session referencing this client
        use crate::schema::sessions;
        #[derive(Insertable)]
        #[diesel(table_name = crate::schema::sessions)]
        struct TestSessionInsert {
            client_id: i32,
            name: String,
            date: String,
            start_time: String,
            end_time: String,
            created_at: String,
        }
        let session = TestSessionInsert {
            client_id: c.id,
            name: "S".into(),
            date: "2024-01-01".into(),
            start_time: "09:00".into(),
            end_time: "10:00".into(),
            created_at: "2024-01-01T09:00:00".into(),
        };
        {
            let mut conn = pool.get().unwrap();
            diesel::insert_into(sessions::table)
                .values(&session)
                .execute(&mut conn)
                .unwrap();
        }
        let err = delete_client(&pool, c.id).unwrap_err();
        matches!(
            err,
            diesel::result::Error::DatabaseError(
                diesel::result::DatabaseErrorKind::ForeignKeyViolation,
                _
            )
        );
    }

    #[test]
    fn delete_client_invalid_id() {
        let pool = setup_pool();
        let err = delete_client(&pool, 0).unwrap_err();
        matches!(err, diesel::result::Error::NotFound);
    }

    #[test]
    fn get_all_clients_counts() {
        let pool = setup_pool();
        for i in 0..3 {
            create_client(&pool, new_client(&format!("C{}", i), 10.0)).unwrap();
        }
        let all = get_all_clients(&pool).unwrap();
        assert_eq!(all.len(), 3);
    }
}

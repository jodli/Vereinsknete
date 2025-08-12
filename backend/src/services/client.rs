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

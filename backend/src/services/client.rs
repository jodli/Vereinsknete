use crate::models::client::{Client, NewClient, UpdateClient};
use crate::DbPool;
use diesel::prelude::*;

pub fn get_all_clients(pool: &DbPool) -> Result<Vec<Client>, diesel::result::Error> {
    use crate::schema::clients::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");
    clients.select(Client::as_select()).load(&mut conn)
}

pub fn get_client_by_id(
    pool: &DbPool,
    client_id: i32,
) -> Result<Option<Client>, diesel::result::Error> {
    use crate::schema::clients::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");
    clients
        .filter(id.eq(client_id))
        .select(Client::as_select())
        .first(&mut conn)
        .optional()
}

pub fn create_client(
    pool: &DbPool,
    new_client: NewClient,
) -> Result<Client, diesel::result::Error> {
    use crate::schema::clients;
    use crate::schema::clients::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");

    diesel::insert_into(clients::table)
        .values(&new_client)
        .execute(&mut conn)?;

    // SQLite doesn't support returning clause, so we'll fetch the inserted client by id
    clients
        .order(id.desc())
        .limit(1)
        .select(Client::as_select())
        .get_result(&mut conn)
}

pub fn update_client(
    pool: &DbPool,
    client_id: i32,
    update_client: UpdateClient,
) -> Result<Client, diesel::result::Error> {
    use crate::schema::clients::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");

    diesel::update(clients.filter(id.eq(client_id)))
        .set(&update_client)
        .execute(&mut conn)?;

    // Fetch the updated record
    clients
        .filter(id.eq(client_id))
        .select(Client::as_select())
        .get_result(&mut conn)
}

pub fn delete_client(pool: &DbPool, client_id: i32) -> Result<usize, diesel::result::Error> {
    use crate::schema::clients::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");

    diesel::delete(clients.filter(id.eq(client_id))).execute(&mut conn)
}

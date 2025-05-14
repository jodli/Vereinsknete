// @generated automatically by Diesel CLI.

diesel::table! {
    user_profile (id) {
        id -> Integer,
        name -> Text,
        address -> Text,
        tax_id -> Nullable<Text>,
        bank_details -> Nullable<Text>,
    }
}

diesel::table! {
    clients (id) {
        id -> Integer,
        name -> Text,
        address -> Text,
        contact_person -> Nullable<Text>,
        default_hourly_rate -> Double,
    }
}

diesel::table! {
    sessions (id) {
        id -> Integer,
        client_id -> Integer,
        name -> Text,
        date -> Text, // Stored as ISO 8601 date string
        start_time -> Text, // Stored as HH:MM time string
        end_time -> Text, // Stored as HH:MM time string
        created_at -> Text, // Stored as ISO 8601 datetime string
    }
}

diesel::allow_tables_to_appear_in_same_query!(user_profile, clients, sessions,);

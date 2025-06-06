// @generated automatically by Diesel CLI.

diesel::table! {
    clients (id) {
        id -> Integer,
        name -> Text,
        address -> Text,
        contact_person -> Nullable<Text>,
        default_hourly_rate -> Float,
        created_at -> Timestamp,
        updated_at -> Timestamp,
    }
}

diesel::table! {
    invoices (id) {
        id -> Integer,
        invoice_number -> Text,
        client_id -> Integer,
        date -> Text,
        total_amount -> Float,
        pdf_path -> Text,
        status -> Text,
        due_date -> Nullable<Text>,
        paid_date -> Nullable<Text>,
        year -> Integer,
        sequence_number -> Integer,
        created_at -> Timestamp,
    }
}

diesel::table! {
    sessions (id) {
        id -> Integer,
        client_id -> Integer,
        name -> Text,
        date -> Text,
        start_time -> Text,
        end_time -> Text,
        created_at -> Timestamp,
    }
}

diesel::table! {
    user_profile (id) {
        id -> Integer,
        name -> Text,
        address -> Text,
        tax_id -> Nullable<Text>,
        bank_details -> Nullable<Text>,
        created_at -> Timestamp,
        updated_at -> Timestamp,
    }
}

diesel::joinable!(invoices -> clients (client_id));
diesel::joinable!(sessions -> clients (client_id));

diesel::allow_tables_to_appear_in_same_query!(
    clients,
    invoices,
    sessions,
    user_profile,
);

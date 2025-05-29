use crate::i18n::{translate, Language};
use crate::models::invoice::InvoiceResponse;
use anyhow::Result;
use chrono::NaiveDate;
use genpdf::{
    elements::{self},
    fonts, style, Element, Margins,
};
use std::io::Cursor;

const FONT_DIR: &str = "/usr/share/fonts/truetype/liberation";
const DEFAULT_FONT_NAME: &str = "LiberationSans";

/// Replace placeholders in text with actual values
/// Supported placeholders:
/// - {invoice_number}: The invoice number
fn replace_placeholders(text: &str, invoice: &InvoiceResponse) -> String {
    text.replace("{invoice_number}", &invoice.invoice_number)
}

/// Format a date string based on language preference
/// Input date string is expected to be in ISO format (YYYY-MM-DD)
fn format_date_for_language(date_str: &str, language: Language) -> String {
    // First try ISO format (YYYY-MM-DD) since that's what's being passed in
    if let Ok(parsed_date) = NaiveDate::parse_from_str(date_str, "%Y-%m-%d") {
        match language {
            Language::German => parsed_date.format("%d.%m.%Y").to_string(),
            _ => parsed_date.format("%Y-%m-%d").to_string(),
        }
    } else {
        // If all parsing fails, return original string
        date_str.to_string()
    }
}

pub fn generate_invoice_pdf(invoice: &InvoiceResponse, language: Option<&str>) -> Result<Vec<u8>> {
    // Determine language from parameter or fall back to default (German)
    let lang = match language {
        Some(lang_str) => Language::from_str(lang_str),
        None => Language::default(),
    };

    // Load system font
    let default_font = fonts::from_files(FONT_DIR, DEFAULT_FONT_NAME, None)?;

    // Create a document
    let mut doc = genpdf::Document::new(default_font);
    let mut decorator = genpdf::SimplePageDecorator::new();
    decorator.set_margins(20);
    doc.set_page_decorator(decorator);

    // Set document properties
    doc.set_title(format!(
        "{} #{}",
        translate(lang, "invoice", "invoice"),
        &invoice.invoice_number
    ));
    doc.set_minimal_conformance();
    doc.set_line_spacing(1.5);

    // Add invoice header with larger font and bold styling
    let header = elements::Paragraph::new(format!(
        "{} #{}",
        translate(lang, "invoice", "invoice"),
        &invoice.invoice_number
    ))
    .styled(style::Style::new().bold().with_font_size(22));
    doc.push(header);

    // Add date with some space below
    doc.push(elements::Paragraph::new(format!(
        "{}: {}",
        translate(lang, "invoice", "date"),
        format_date_for_language(&invoice.date, lang)
    )));
    doc.push(elements::Break::new(1.5));

    // Create a two-column layout for FROM and TO sections
    let mut address_layout = elements::LinearLayout::vertical();
    let mut address_columns = elements::TableLayout::new(vec![1, 1]);

    // FROM section with clear visual separation
    let mut from_section = elements::LinearLayout::vertical();
    from_section.push(
        elements::Paragraph::new(translate(lang, "invoice", "from"))
            .styled(style::Style::new().bold().with_font_size(14)),
    );
    from_section.push(elements::Paragraph::new(&invoice.user_profile.name));

    // Split address by newline and add each line as separate paragraph
    for line in invoice.user_profile.address.split('\n') {
        from_section.push(elements::Paragraph::new(line));
    }

    if let Some(tax_id) = &invoice.user_profile.tax_id {
        from_section.push(elements::Paragraph::new(format!(
            "{}: {}",
            translate(lang, "invoice", "tax_id"),
            tax_id
        )));
    }

    // TO section
    let mut to_section = elements::LinearLayout::vertical();
    to_section.push(
        elements::Paragraph::new(translate(lang, "invoice", "to"))
            .styled(style::Style::new().bold().with_font_size(14)),
    );
    to_section.push(elements::Paragraph::new(&invoice.client.name));

    // Split address by newline and add each line as separate paragraph
    for line in invoice.client.address.split('\n') {
        to_section.push(elements::Paragraph::new(line));
    }

    if let Some(contact) = &invoice.client.contact_person {
        to_section.push(elements::Paragraph::new(format!(
            "{}: {}",
            translate(lang, "invoice", "contact"),
            contact
        )));
    }

    // Add from and to sections to the columns
    address_columns.push_row(vec![Box::new(from_section), Box::new(to_section)])?;

    address_layout.push(address_columns);
    doc.push(address_layout);
    doc.push(elements::Break::new(2.0));

    // Create table for sessions with clear borders and padding
    let mut table = elements::TableLayout::new(vec![
        2, // Name
        1, // Date
        1, // Start Time
        1, // End Time
        1, // Duration
        1, // Amount
    ]);

    // Add table header with background color and borders
    let header_decorator = elements::FrameCellDecorator::new(true, true, false);
    table.set_cell_decorator(header_decorator);

    table.push_row(vec![
        Box::new(
            elements::Paragraph::new(translate(lang, "invoice", "service"))
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new(translate(lang, "invoice", "date"))
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new(translate(lang, "invoice", "start"))
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new(translate(lang, "invoice", "end"))
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new(translate(lang, "invoice", "hours"))
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new(translate(lang, "invoice", "amount"))
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
    ])?;

    // Set cell decorator with borders for all data rows
    table.set_cell_decorator(elements::FrameCellDecorator::new(true, true, false));

    // Add session rows with alternating background colors
    for item in invoice.sessions.iter() {
        table.push_row(vec![
            Box::new(elements::Paragraph::new(&item.name).padded(Margins::all(1))),
            Box::new(
                elements::Paragraph::new(format_date_for_language(&item.date, lang))
                    .padded(Margins::all(1)),
            ),
            Box::new(elements::Paragraph::new(&item.start_time).padded(Margins::all(1))),
            Box::new(elements::Paragraph::new(&item.end_time).padded(Margins::all(1))),
            Box::new(
                elements::Paragraph::new(format!("{:.2}", item.duration_hours))
                    .padded(Margins::all(1)),
            ),
            Box::new(
                elements::Paragraph::new(format!("€{:.2}", item.amount)).padded(Margins::all(1)),
            ),
        ])?;
    }

    doc.push(table);
    doc.push(elements::Break::new(1.0));

    // Add totals in a visually distinct table
    let mut totals_table = elements::TableLayout::new(vec![4, 2]);
    totals_table.set_cell_decorator(elements::FrameCellDecorator::new(true, true, false));

    // Set total hours row
    totals_table.push_row(vec![
        Box::new(
            elements::Paragraph::new(format!("{}:", translate(lang, "invoice", "total_hours")))
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new(format!("{:.2}", invoice.total_hours)).padded(Margins::all(1)),
        ),
    ])?;

    totals_table.push_row(vec![
        Box::new(
            elements::Paragraph::new(format!("{}:", translate(lang, "invoice", "total_amount")))
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new(format!("€{:.2}", invoice.total_amount))
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
    ])?;

    doc.push(totals_table);
    doc.push(elements::Break::new(1.5));

    doc.push(
        elements::Paragraph::new(format!(
            "{}:",
            translate(lang, "invoice", "payment_details")
        ))
        .styled(style::Style::new().bold().with_font_size(14)),
    );

    if let Some(bank_details) = &invoice.user_profile.bank_details {
        // Replace placeholders in bank details
        let processed_bank_details = replace_placeholders(bank_details, invoice);

        // Split bank details by newline and add each line as separate paragraph
        for line in processed_bank_details.split('\n') {
            doc.push(elements::Paragraph::new(line));
        }
    } else {
        doc.push(elements::Paragraph::new(translate(
            lang,
            "invoice",
            "no_payment_details",
        )));
    }

    // Render the document to a byte buffer
    let mut buffer = Cursor::new(Vec::new());
    doc.render(&mut buffer)?;

    Ok(buffer.into_inner())
}

use crate::models::invoice::InvoiceResponse;
use anyhow::Result;
use genpdf::{
    elements::{self},
    fonts, style, Element, Margins,
};
use std::io::Cursor;

const FONT_DIR: &str = "/usr/share/fonts/truetype/liberation";
const DEFAULT_FONT_NAME: &str = "LiberationSans";

pub fn generate_invoice_pdf(invoice: &InvoiceResponse) -> Result<Vec<u8>> {
    // Load system font
    let default_font = fonts::from_files(FONT_DIR, DEFAULT_FONT_NAME, None)?;

    // Create a document
    let mut doc = genpdf::Document::new(default_font);
    let mut decorator = genpdf::SimplePageDecorator::new();
    decorator.set_margins(20);
    doc.set_page_decorator(decorator);

    // Set document properties
    doc.set_title(&format!("Invoice #{}", &invoice.invoice_number));
    doc.set_minimal_conformance();
    doc.set_line_spacing(1.5);

    // Add invoice header with larger font and bold styling
    let header = elements::Paragraph::new(&format!("INVOICE #{}", &invoice.invoice_number))
        .styled(style::Style::new().bold().with_font_size(22));
    doc.push(header);

    // Add date with some space below
    doc.push(elements::Paragraph::new(&format!(
        "Date: {}",
        &invoice.date
    )));
    doc.push(elements::Break::new(1.5));

    // Create a two-column layout for FROM and TO sections
    let mut address_layout = elements::LinearLayout::vertical();
    let mut address_columns = elements::TableLayout::new(vec![1, 1]);

    // FROM section with clear visual separation
    let mut from_section = elements::LinearLayout::vertical();
    from_section.push(
        elements::Paragraph::new("FROM:").styled(style::Style::new().bold().with_font_size(14)),
    );
    from_section.push(elements::Paragraph::new(&invoice.user_profile.name));

    // Split address by newline and add each line as separate paragraph
    for line in invoice.user_profile.address.split('\n') {
        from_section.push(elements::Paragraph::new(line));
    }

    if let Some(tax_id) = &invoice.user_profile.tax_id {
        from_section.push(elements::Paragraph::new(&format!("Tax ID: {}", tax_id)));
    }

    // TO section
    let mut to_section = elements::LinearLayout::vertical();
    to_section.push(
        elements::Paragraph::new("TO:").styled(style::Style::new().bold().with_font_size(14)),
    );
    to_section.push(elements::Paragraph::new(&invoice.client.name));

    // Split address by newline and add each line as separate paragraph
    for line in invoice.client.address.split('\n') {
        to_section.push(elements::Paragraph::new(line));
    }

    if let Some(contact) = &invoice.client.contact_person {
        to_section.push(elements::Paragraph::new(&format!("Contact: {}", contact)));
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
            elements::Paragraph::new("Service")
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new("Date")
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new("Start")
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new("End")
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new("Hours")
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new("Amount")
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
            Box::new(elements::Paragraph::new(&item.date).padded(Margins::all(1))),
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
            elements::Paragraph::new("Total Hours:")
                .styled(style::Style::new().bold())
                .padded(Margins::all(1)),
        ),
        Box::new(
            elements::Paragraph::new(format!("{:.2}", invoice.total_hours)).padded(Margins::all(1)),
        ),
    ])?;

    totals_table.push_row(vec![
        Box::new(
            elements::Paragraph::new("Total Amount:")
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
        elements::Paragraph::new("Payment Details:")
            .styled(style::Style::new().bold().with_font_size(14)),
    );

    if let Some(bank_details) = &invoice.user_profile.bank_details {
        // Split bank details by newline and add each line as separate paragraph
        for line in bank_details.split('\n') {
            doc.push(elements::Paragraph::new(line));
        }
    } else {
        doc.push(elements::Paragraph::new(
            "Please contact for payment details.",
        ));
    }

    // Render the document to a byte buffer
    let mut buffer = Cursor::new(Vec::new());
    doc.render(&mut buffer)?;

    Ok(buffer.into_inner())
}

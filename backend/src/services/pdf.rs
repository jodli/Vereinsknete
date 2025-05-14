use anyhow::Result;
use genpdf::{elements, fonts, style};
use crate::models::invoice::InvoiceResponse;
use std::io::Cursor;

pub fn generate_invoice_pdf(invoice: &InvoiceResponse) -> Result<Vec<u8>> {
    // Load the default font
    let font_family = fonts::from_files("./fonts", "DejaVuSans", None)?;

    // Create a document
    let mut doc = genpdf::Document::new(font_family);
    let mut decorator = genpdf::SimplePageDecorator::new();
    decorator.set_margins(20);
    doc.set_page_decorator(decorator);

    // Set document properties
    doc.set_title(&format!("Invoice #{}", &invoice.invoice_number));
    doc.set_minimal_conformance();
    doc.set_line_spacing(1.5);

    // Add invoice header
    let mut header = elements::Paragraph::new(&format!("INVOICE #{}", &invoice.invoice_number));
    header.set_font_size(20);
    header.set_bold(true);
    doc.push(header);

    doc.push(elements::Paragraph::new(&format!("Date: {}", &invoice.date)));
    doc.push(elements::Break::new(1.0));

    // Add user profile details
    let mut from_title = elements::Paragraph::new("FROM:");
    from_title.set_bold(true);
    doc.push(from_title);

    doc.push(elements::Paragraph::new(&invoice.user_profile.name));
    doc.push(elements::Paragraph::new(&invoice.user_profile.address));

    if let Some(tax_id) = &invoice.user_profile.tax_id {
        doc.push(elements::Paragraph::new(&format!("Tax ID: {}", tax_id)));
    }

    if let Some(bank_details) = &invoice.user_profile.bank_details {
        doc.push(elements::Paragraph::new(&format!("Bank details: {}", bank_details)));
    }

    doc.push(elements::Break::new(1.0));

    // Add client details
    let mut to_title = elements::Paragraph::new("TO:");
    to_title.set_bold(true);
    doc.push(to_title);

    doc.push(elements::Paragraph::new(&invoice.client.name));
    doc.push(elements::Paragraph::new(&invoice.client.address));

    if let Some(contact) = &invoice.client.contact_person {
        doc.push(elements::Paragraph::new(&format!("Contact: {}", contact)));
    }

    doc.push(elements::Break::new(1.5));

    // Create table for sessions
    let mut table = elements::TableLayout::new(vec![
        1.5, // Name
        1.0, // Date
        0.8, // Start Time
        0.8, // End Time
        0.8, // Duration
        1.0, // Amount
    ]);

    // Add table header
    table.set_cell_decorator(style::CellDecorator::new().set_background_color(style::Color::Rgb(220, 220, 220)));
    table.push_row(vec![
        elements::Paragraph::new("Service").with_style(style::Style::new().bold()),
        elements::Paragraph::new("Date").with_style(style::Style::new().bold()),
        elements::Paragraph::new("Start").with_style(style::Style::new().bold()),
        elements::Paragraph::new("End").with_style(style::Style::new().bold()),
        elements::Paragraph::new("Hours").with_style(style::Style::new().bold()),
        elements::Paragraph::new("Amount").with_style(style::Style::new().bold()),
    ]);

    // Reset decorator for data rows
    table.set_cell_decorator(style::CellDecorator::new());

    // Add session rows
    for item in &invoice.sessions {
        table.push_row(vec![
            elements::Paragraph::new(&item.name),
            elements::Paragraph::new(&item.date),
            elements::Paragraph::new(&item.start_time),
            elements::Paragraph::new(&item.end_time),
            elements::Paragraph::new(&format!("{:.2}", item.duration_hours)),
            elements::Paragraph::new(&format!("€{:.2}", item.amount)),
        ]);
    }

    doc.push(table);
    doc.push(elements::Break::new(1.0));

    // Add totals
    let mut totals_table = elements::TableLayout::new(vec![4.0, 2.0]);

    totals_table.push_row(vec![
        elements::Paragraph::new("Total Hours:").with_style(style::Style::new().bold()),
        elements::Paragraph::new(&format!("{:.2}", invoice.total_hours)),
    ]);

    totals_table.push_row(vec![
        elements::Paragraph::new("Total Amount:").with_style(style::Style::new().bold()),
        elements::Paragraph::new(&format!("€{:.2}", invoice.total_amount)).with_style(style::Style::new().bold()),
    ]);

    doc.push(totals_table);
    doc.push(elements::Break::new(1.0));

    // Add payment details
    let mut payment_title = elements::Paragraph::new("Payment Details:");
    payment_title.set_bold(true);
    doc.push(payment_title);

    if let Some(bank_details) = &invoice.user_profile.bank_details {
        doc.push(elements::Paragraph::new(bank_details));
    } else {
        doc.push(elements::Paragraph::new("Please contact for payment details."));
    }

    // Render the document to a byte buffer
    let mut buffer = Cursor::new(Vec::new());
    doc.render(&mut buffer)?;

    Ok(buffer.into_inner())
}

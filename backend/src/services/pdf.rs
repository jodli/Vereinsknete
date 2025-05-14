use crate::models::invoice::InvoiceResponse;
use anyhow::Result;
use genpdf::{elements, fonts, style, Element};
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
    let header = elements::Paragraph::new(&format!("INVOICE #{}", &invoice.invoice_number))
        .styled(style::Style::new().bold().with_font_size(20));
    doc.push(header);

    doc.push(elements::Paragraph::new(&format!(
        "Date: {}",
        &invoice.date
    )));
    doc.push(elements::Break::new(1.0));

    // Add user profile details
    let from_title = elements::Paragraph::new("FROM:").styled(style::Style::new().bold());
    doc.push(from_title);

    doc.push(elements::Paragraph::new(&invoice.user_profile.name));
    doc.push(elements::Paragraph::new(&invoice.user_profile.address));

    if let Some(tax_id) = &invoice.user_profile.tax_id {
        doc.push(elements::Paragraph::new(&format!("Tax ID: {}", tax_id)));
    }

    if let Some(bank_details) = &invoice.user_profile.bank_details {
        doc.push(elements::Paragraph::new(&format!(
            "Bank details: {}",
            bank_details
        )));
    }

    doc.push(elements::Break::new(1.0));

    // Add client details
    let to_title = elements::Paragraph::new("TO:").styled(style::Style::new().bold());
    doc.push(to_title);

    doc.push(elements::Paragraph::new(&invoice.client.name));
    doc.push(elements::Paragraph::new(&invoice.client.address));

    if let Some(contact) = &invoice.client.contact_person {
        doc.push(elements::Paragraph::new(&format!("Contact: {}", contact)));
    }

    doc.push(elements::Break::new(1.5));

    // Create table for sessions
    let mut table = elements::TableLayout::new(vec![
        2, // Name
        1, // Date
        1, // Start Time
        1, // End Time
        1, // Duration
        1, // Amount
    ]);

    // Add table header
    table.set_cell_decorator(elements::FrameCellDecorator::new(false, true, false));
    table.push_row(vec![
        Box::new(elements::Paragraph::new("Service").styled(style::Style::new().bold())),
        Box::new(elements::Paragraph::new("Date").styled(style::Style::new().bold())),
        Box::new(elements::Paragraph::new("Start").styled(style::Style::new().bold())),
        Box::new(elements::Paragraph::new("End").styled(style::Style::new().bold())),
        Box::new(elements::Paragraph::new("Hours").styled(style::Style::new().bold())),
        Box::new(elements::Paragraph::new("Amount").styled(style::Style::new().bold())),
    ])?;

    // Reset decorator for data rows
    table.set_cell_decorator(elements::FrameCellDecorator::new(false, false, false));

    // Add session rows
    for item in &invoice.sessions {
        table.push_row(vec![
            Box::new(elements::Paragraph::new(&item.name)),
            Box::new(elements::Paragraph::new(&item.date)),
            Box::new(elements::Paragraph::new(&item.start_time)),
            Box::new(elements::Paragraph::new(&item.end_time)),
            Box::new(elements::Paragraph::new(&format!(
                "{:.2}",
                item.duration_hours
            ))),
            Box::new(elements::Paragraph::new(&format!("€{:.2}", item.amount))),
        ])?;
    }

    doc.push(table);
    doc.push(elements::Break::new(1.0));

    // Add totals
    let mut totals_table = elements::TableLayout::new(vec![4, 2]);

    totals_table.push_row(vec![
        Box::new(elements::Paragraph::new("Total Hours:").styled(style::Style::new().bold())),
        Box::new(elements::Paragraph::new(&format!(
            "{:.2}",
            invoice.total_hours
        ))),
    ])?;

    totals_table.push_row(vec![
        Box::new(elements::Paragraph::new("Total Amount:").styled(style::Style::new().bold())),
        Box::new(
            elements::Paragraph::new(&format!("€{:.2}", invoice.total_amount))
                .styled(style::Style::new().bold()),
        ),
    ])?;

    doc.push(totals_table);
    doc.push(elements::Break::new(1.0));

    // Add payment details
    let payment_title =
        elements::Paragraph::new("Payment Details:").styled(style::Style::new().bold());
    doc.push(payment_title);

    if let Some(bank_details) = &invoice.user_profile.bank_details {
        doc.push(elements::Paragraph::new(bank_details));
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

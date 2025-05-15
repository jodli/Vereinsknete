use super::{Language, TranslationMap};
use lazy_static::lazy_static;
use std::collections::HashMap;

// Define invoice translations for PDF export
lazy_static! {
    // English translations
    static ref INVOICE_EN: TranslationMap = {
        let mut map = HashMap::new();
        // Invoice header
        map.insert("invoice", "INVOICE");
        map.insert("date", "Date");
        // From/To sections
        map.insert("from", "FROM");
        map.insert("to", "TO");
        map.insert("contact", "Contact");
        map.insert("tax_id", "Tax ID");
        // Table headers
        map.insert("service", "Service");
        map.insert("start", "Start");
        map.insert("end", "End");
        map.insert("hours", "Hours");
        map.insert("amount", "Amount");
        // Totals
        map.insert("total_hours", "Total Hours");
        map.insert("total_amount", "Total Amount");
        // Payment details
        map.insert("payment_details", "Payment Details");
        map.insert("no_payment_details", "Please contact for payment details.");
        map
    };

    // German translations
    static ref INVOICE_DE: TranslationMap = {
        let mut map = HashMap::new();
        // Invoice header
        map.insert("invoice", "RECHNUNG");
        map.insert("date", "Datum");
        // From/To sections
        map.insert("from", "VON");
        map.insert("to", "AN");
        map.insert("contact", "Ansprechpartner");
        map.insert("tax_id", "Steuernummer");
        // Table headers
        map.insert("service", "Leistung");
        map.insert("start", "Beginn");
        map.insert("end", "Ende");
        map.insert("hours", "Stunden");
        map.insert("amount", "Betrag");
        // Totals
        map.insert("total_hours", "Gesamtstunden");
        map.insert("total_amount", "Gesamtbetrag");
        // Payment details
        map.insert("payment_details", "Zahlungsinformationen");
        map.insert("no_payment_details", "Bitte kontaktieren Sie uns für Zahlungsdetails.");
        map
    };

    // Map of all translations by category and language
    static ref TRANSLATIONS: HashMap<Language, HashMap<&'static str, &'static TranslationMap>> = {
        let mut map = HashMap::new();

        // English translations by category
        let mut en_map = HashMap::new();
        en_map.insert("invoice", &*INVOICE_EN);
        map.insert(Language::English, en_map);

        // German translations by category
        let mut de_map = HashMap::new();
        de_map.insert("invoice", &*INVOICE_DE);
        map.insert(Language::German, de_map);

        map
    };
}

// Get translations for a specific language and category
pub fn get_translations(lang: Language, category: &str) -> &'static TranslationMap {
    match TRANSLATIONS
        .get(&lang)
        .and_then(|categories| categories.get(category))
    {
        Some(translations) => translations,
        None => match lang {
            Language::English => &INVOICE_EN,
            Language::German => &INVOICE_DE,
        },
    }
}

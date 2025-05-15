pub mod translations;

use std::collections::HashMap;

pub type TranslationMap = HashMap<&'static str, &'static str>;

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Default)]
pub enum Language {
    English,
    #[default]
    German,
}

impl Language {
    pub fn from_str(s: &str) -> Self {
        match s.to_lowercase().as_str() {
            "en" => Language::English,
            _ => Language::German,
        }
    }
}

// Get translations for a specific language and category
pub fn get_translations(lang: Language, category: &str) -> &'static TranslationMap {
    translations::get_translations(lang, category)
}

// Translation keys for fallback
const KEY_NOT_FOUND: &str = "TRANSLATION_MISSING";

// Translate a key based on the language
pub fn translate(lang: Language, category: &str, key: &str) -> &'static str {
    match get_translations(lang, category).get(key) {
        Some(value) => value,
        None => {
            eprintln!(
                "Translation missing for key: '{}' in category: '{}'",
                key, category
            );
            KEY_NOT_FOUND
        }
    }
}

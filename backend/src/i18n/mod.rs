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
    /// Convenience helper returning a Language from &str using same rules as `FromStr`.
    /// Accepts "en" for English; any other value defaults to German.
    pub fn parse_lang(s: &str) -> Self {
        s.parse().unwrap_or_default()
    }
}

impl std::str::FromStr for Language {
    type Err = std::convert::Infallible; // Parsing never fails; unknown maps to default (German)

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        // Only two supported short codes for now; unknown defaults to German.
        Ok(match s.to_ascii_lowercase().as_str() {
            "en" => Language::English,
            _ => Language::German,
        })
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

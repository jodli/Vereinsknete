// This script initializes our i18n system and ensures the correct language is loaded on startup
// By placing it directly in the public folder, it will load before React

// Set the application default language as German
document.documentElement.lang = 'de';

// Function to initialize language settings
function initLanguage() {
    // Get stored language preference, if any
    const storedLang = localStorage.getItem('preferredLanguage');

    // If a preference exists, respect it, otherwise default to German
    const language = storedLang || 'de';

    // Set the html lang attribute to help with accessibility
    document.documentElement.lang = language;

    // We can add more initialization if needed
    console.log(`VereinsKnete language initialized to: ${language}`);
}

// Run the initialization
initLanguage();

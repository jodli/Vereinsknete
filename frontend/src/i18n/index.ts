import en from './en';
import de from './de';

// The types for our translations
export type TranslationsType = typeof en;

// Language dictionary
const languages = {
    en,
    de,
};

// Default language is German
export const defaultLanguage = 'de';

// Type for supported languages
export type SupportedLanguage = keyof typeof languages;

// Get translations for a specific language
export const getTranslations = (lang: SupportedLanguage): TranslationsType => {
    return languages[lang] || languages[defaultLanguage];
};

// Language context
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';

type LanguageContextType = {
    language: SupportedLanguage;
    translations: TranslationsType;
    setLanguage: (lang: SupportedLanguage) => void;
};

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export const LanguageProvider = ({ children }: { children: ReactNode }) => {
    const [language, setLanguageState] = useState<SupportedLanguage>(defaultLanguage);
    const [translations, setTranslations] = useState<TranslationsType>(getTranslations(language));

    // Check for stored preference on component mount
    useEffect(() => {
        const storedLang = localStorage.getItem('preferredLanguage') as SupportedLanguage | null;
        if (storedLang && languages[storedLang]) {
            setLanguageState(storedLang);
            setTranslations(getTranslations(storedLang));
        }
    }, []);

    const setLanguage = (lang: SupportedLanguage) => {
        setLanguageState(lang);
        setTranslations(getTranslations(lang));
        // Persist language preference
        localStorage.setItem('preferredLanguage', lang);
    };

    const value = { language, translations, setLanguage };

    return React.createElement(
        LanguageContext.Provider,
        { value },
        children
    );
};

// Hook to use language context
export const useLanguage = (): LanguageContextType => {
    const context = useContext(LanguageContext);
    if (context === undefined) {
        throw new Error('useLanguage must be used within a LanguageProvider');
    }
    return context;
};

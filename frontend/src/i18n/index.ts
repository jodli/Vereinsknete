import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import en from './en';
import de from './de';

// The types for our translations
export type TranslationsType = typeof en;

// Language dictionary
const languages = {
    en,
    de,
};

// Default language is German (product default). Tests that need English should
// explicitly set localStorage('preferredLanguage','en') before rendering.
export const defaultLanguage: keyof typeof languages = 'de';

// Type for supported languages
export type SupportedLanguage = keyof typeof languages;

// Get translations for a specific language
export const getTranslations = (lang: SupportedLanguage): TranslationsType => {
    return languages[lang] || languages[defaultLanguage];
};

// Language context

type LanguageContextType = {
    language: SupportedLanguage;
    translations: TranslationsType;
    setLanguage: (lang: SupportedLanguage) => void;
};

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export const LanguageProvider = ({ children }: { children: ReactNode }) => {
    // Initialize with stored preference or default
    const getInitialLanguage = (): SupportedLanguage => {
        try {
            const storedLang = localStorage.getItem('preferredLanguage') as SupportedLanguage | null;
            if (storedLang && languages[storedLang]) {
                return storedLang;
            }
        } catch {
            // ignore
        }
        return defaultLanguage;
    };

    const initialLanguage = getInitialLanguage();
    const [language, setLanguageState] = useState<SupportedLanguage>(initialLanguage);
    const [translations, setTranslations] = useState<TranslationsType>(getTranslations(initialLanguage));

    // Check for stored preference on component mount (for cases where initial state couldn't access localStorage)
    useEffect(() => {
        const storedLang = localStorage.getItem('preferredLanguage') as SupportedLanguage | null;
        if (storedLang && languages[storedLang] && storedLang !== language) {
            setLanguageState(storedLang);
            setTranslations(getTranslations(storedLang));
        }
    }, [language]);

    const setLanguage = (lang: SupportedLanguage) => {
        setLanguageState(lang);
        setTranslations(getTranslations(lang));
        try {
            localStorage.setItem('preferredLanguage', lang);
        } catch {
            // ignore persistence errors (e.g., in private mode)
        }
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

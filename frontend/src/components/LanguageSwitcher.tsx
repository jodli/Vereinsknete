import React from 'react';
import { useLanguage, SupportedLanguage } from '../i18n';
import { GlobeAltIcon } from '@heroicons/react/24/outline';

interface LanguageSwitcherProps {
    compact?: boolean;
}

const LanguageSwitcher: React.FC<LanguageSwitcherProps> = ({ compact = false }) => {
    const { language, setLanguage } = useLanguage();

    const languages: { code: SupportedLanguage; name: string; flag: string }[] = [
        { code: 'de', name: 'Deutsch', flag: '🇩🇪' },
        { code: 'en', name: 'English', flag: '🇺🇸' },
    ];

    const handleLanguageChange = (lang: SupportedLanguage) => {
        setLanguage(lang);
    };

    // Removed unused currentLanguage lookup (was only used for a previous implementation)

    // Unified implementation: always show two accessible buttons (tests rely on querying by role & name)
    return (
        <div className={`flex ${compact ? 'flex-col space-y-2 items-center' : 'space-x-2'} `} aria-label="Language Switcher">
            {languages.map((lang) => {
                const isActive = language === lang.code;
                return (
                    <button
                        key={lang.code}
                        onClick={() => handleLanguageChange(lang.code)}
                        aria-pressed={isActive}
                        // Provide accessible name when in compact mode (flag alone isn't descriptive)
                        {...(compact ? { 'aria-label': lang.name } : {})}
                        className={`flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500
                            ${isActive
                                ? 'bg-blue-600 text-white shadow-sm hover:bg-blue-700'
                                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}
                            ${compact ? 'w-10 h-10 justify-center p-0' : ''}
                        `}
                    >
                        {compact ? (
                            <span className="text-base" aria-hidden>{lang.flag}</span>
                        ) : (
                            <span className="flex items-center space-x-1">
                                <GlobeAltIcon className="w-4 h-4 text-gray-500" aria-hidden />
                                <span>{lang.name}</span>
                            </span>
                        )}
                    </button>
                );
            })}
        </div>
    );
};

export default LanguageSwitcher;

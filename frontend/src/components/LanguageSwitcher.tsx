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

    const currentLanguage = languages.find(lang => lang.code === language);

    if (compact) {
        // Compact version for collapsed sidebar - just show flag
        return (
            <div className="relative group">
                <div className="flex items-center justify-center w-10 h-10 rounded-lg bg-gray-50 hover:bg-gray-100 transition-all duration-200 cursor-pointer transform hover:scale-105">
                    <span className="text-lg">{currentLanguage?.flag}</span>
                </div>

                {/* Dropdown menu positioned to the right */}
                <div className="absolute bottom-0 left-full ml-2 bg-white border border-gray-200 rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50 animate-slide-up min-w-32">
                    {languages.map((lang) => (
                        <button
                            key={lang.code}
                            onClick={() => handleLanguageChange(lang.code)}
                            className={`w-full text-left px-3 py-2 text-sm transition-all duration-200 first:rounded-t-lg last:rounded-b-lg ${language === lang.code
                                    ? 'bg-blue-50 text-blue-700 font-medium shadow-sm'
                                    : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                                }`}
                        >
                            <span className="flex items-center space-x-2">
                                <span>{lang.flag}</span>
                                <span>{lang.name}</span>
                            </span>
                        </button>
                    ))}
                </div>
            </div>
        );
    }

    // Full version for expanded sidebar
    return (
        <div className="relative group">
            <div className="flex items-center space-x-2 px-3 py-2 rounded-lg bg-gray-50 hover:bg-gray-100 transition-all duration-200 cursor-pointer transform hover:scale-105">
                <GlobeAltIcon className="w-4 h-4 text-gray-500" />
                <span className="text-sm text-gray-700 font-medium">
                    {currentLanguage?.flag} {currentLanguage?.name}
                </span>
            </div>

            {/* Dropdown menu */}
            <div className="absolute bottom-full left-0 mb-1 w-full bg-white border border-gray-200 rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50 animate-slide-up">
                {languages.map((lang) => (
                    <button
                        key={lang.code}
                        onClick={() => handleLanguageChange(lang.code)}
                        className={`w-full text-left px-3 py-2 text-sm transition-all duration-200 first:rounded-t-lg last:rounded-b-lg ${language === lang.code
                                ? 'bg-blue-50 text-blue-700 font-medium shadow-sm'
                                : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                            }`}
                    >
                        <span className="flex items-center space-x-2">
                            <span>{lang.flag}</span>
                            <span>{lang.name}</span>
                        </span>
                    </button>
                ))}
            </div>
        </div>
    );
};

export default LanguageSwitcher;

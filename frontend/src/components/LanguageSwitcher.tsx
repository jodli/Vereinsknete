import React from 'react';
import { useLanguage, SupportedLanguage } from '../i18n';
import { GlobeAltIcon } from '@heroicons/react/24/outline';

const LanguageSwitcher: React.FC = () => {
    const { language, setLanguage } = useLanguage();

    const languages: { code: SupportedLanguage; name: string; flag: string }[] = [
        { code: 'de', name: 'Deutsch', flag: '🇩🇪' },
        { code: 'en', name: 'English', flag: '🇺🇸' },
    ];

    const handleLanguageChange = (lang: SupportedLanguage) => {
        setLanguage(lang);
    };

    const currentLanguage = languages.find(lang => lang.code === language);

    return (
        <div className="relative group">
            <div className="flex items-center space-x-2 px-3 py-2 rounded-lg bg-gray-50 hover:bg-gray-100 transition-colors cursor-pointer">
                <GlobeAltIcon className="w-4 h-4 text-gray-500" />
                <span className="text-sm text-gray-700 font-medium">
                    {currentLanguage?.flag} {currentLanguage?.name}
                </span>
            </div>

            {/* Dropdown menu */}
            <div className="absolute top-full left-0 mt-1 w-full bg-white border border-gray-200 rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50">
                {languages.map((lang) => (
                    <button
                        key={lang.code}
                        onClick={() => handleLanguageChange(lang.code)}
                        className={`w-full text-left px-3 py-2 text-sm hover:bg-gray-50 transition-colors first:rounded-t-lg last:rounded-b-lg ${language === lang.code
                                ? 'bg-blue-50 text-blue-700 font-medium'
                                : 'text-gray-700'
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

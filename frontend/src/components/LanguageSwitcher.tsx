import React from 'react';
import { useLanguage, SupportedLanguage } from '../i18n';

const LanguageSwitcher: React.FC = () => {
    const { language, setLanguage } = useLanguage();

    const languages: { code: SupportedLanguage; name: string }[] = [
        { code: 'de', name: 'Deutsch' },
        { code: 'en', name: 'English' },
    ];

    const handleLanguageChange = (lang: SupportedLanguage) => {
        setLanguage(lang);
    };

    return (
        <div className="flex items-center space-x-2">
            {languages.map((lang) => (
                <button
                    key={lang.code}
                    onClick={() => handleLanguageChange(lang.code)}
                    className={`text-sm px-2 py-1 rounded ${language === lang.code ? 'bg-blue-100 text-blue-700' : 'text-gray-600 hover:bg-gray-100'
                        }`}
                >
                    {lang.name}
                </button>
            ))}
        </div>
    );
};

export default LanguageSwitcher;

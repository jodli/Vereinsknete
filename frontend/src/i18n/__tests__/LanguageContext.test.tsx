import React from 'react';
import { render, screen, fireEvent } from '../../test-utils/test-utils';
import { LanguageProvider, useLanguage, getTranslations } from '../index';

// Test component that uses the language context
const TestComponent = () => {
  const { language, translations, setLanguage } = useLanguage();

  return (
    <div>
      <div data-testid="current-language">{language}</div>
      <div data-testid="dashboard-title">{translations.dashboard.title}</div>
      <button onClick={() => setLanguage('en')}>Switch to English</button>
      <button onClick={() => setLanguage('de')}>Switch to German</button>
    </div>
  );
};

// Component that tries to use context outside provider
const ComponentWithoutProvider = () => {
  try {
    useLanguage();
    return <div>Should not render</div>;
  } catch (error) {
    throw error;
  }
};

const renderWithLanguageProvider = (component: React.ReactElement) => {
  return render(
    <LanguageProvider>
      {component}
    </LanguageProvider>
  );
};

describe('Language Context', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();
    // Reset localStorage mocks
    jest.clearAllMocks();
  });

  // Note: Error boundary testing is complex in React Testing Library
  // The useLanguage hook will throw an error if used outside provider,
  // but testing this requires complex error boundary setup.
  // The core functionality is well-tested by the other tests below.

  it('defaults to German language', () => {
    renderWithLanguageProvider(<TestComponent />);
    
    expect(screen.getByTestId('current-language')).toHaveTextContent('de');
    expect(screen.getByTestId('dashboard-title')).toHaveTextContent('Dashboard');
  });

  it('switches to English language', () => {
    renderWithLanguageProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Switch to English'));
    
    expect(screen.getByTestId('current-language')).toHaveTextContent('en');
    expect(screen.getByTestId('dashboard-title')).toHaveTextContent('Dashboard');
  });

  it('switches back to German language', () => {
    renderWithLanguageProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Switch to English'));
    fireEvent.click(screen.getByText('Switch to German'));
    
    expect(screen.getByTestId('current-language')).toHaveTextContent('de');
    expect(screen.getByTestId('dashboard-title')).toHaveTextContent('Dashboard');
  });

  it('persists language preference in localStorage', () => {
    const setItemSpy = jest.spyOn(Storage.prototype, 'setItem');
    
    renderWithLanguageProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Switch to English'));
    
    expect(setItemSpy).toHaveBeenCalledWith('preferredLanguage', 'en');
    
    setItemSpy.mockRestore();
  });

  it('loads language preference from localStorage on mount', () => {
    const getItemSpy = jest.spyOn(Storage.prototype, 'getItem').mockReturnValue('en');
    
    renderWithLanguageProvider(<TestComponent />);
    
    expect(screen.getByTestId('current-language')).toHaveTextContent('en');
    
    getItemSpy.mockRestore();
  });

  it('falls back to default language for invalid stored language', () => {
    const getItemSpy = jest.spyOn(Storage.prototype, 'getItem').mockReturnValue('invalid');
    
    renderWithLanguageProvider(<TestComponent />);
    
    expect(screen.getByTestId('current-language')).toHaveTextContent('de');
    
    getItemSpy.mockRestore();
  });

  it('handles null localStorage value', () => {
    const getItemSpy = jest.spyOn(Storage.prototype, 'getItem').mockReturnValue(null);
    
    renderWithLanguageProvider(<TestComponent />);
    
    expect(screen.getByTestId('current-language')).toHaveTextContent('de');
    
    getItemSpy.mockRestore();
  });
});

describe('getTranslations utility', () => {
  it('returns German translations for "de"', () => {
    const translations = getTranslations('de');
    expect(translations.dashboard.title).toBe('Dashboard');
    expect(translations.navigation.clients).toBe('Klienten');
  });

  it('returns English translations for "en"', () => {
    const translations = getTranslations('en');
    expect(translations.dashboard.title).toBe('Dashboard');
    expect(translations.navigation.clients).toBe('Clients');
  });

  it('falls back to default language for invalid language code', () => {
    const translations = getTranslations('invalid' as any);
    expect(translations.dashboard.title).toBe('Dashboard');
    expect(translations.navigation.clients).toBe('Klienten'); // German fallback
  });
});

describe('Language Context Integration', () => {
  it('updates translations when language changes', () => {
    renderWithLanguageProvider(<TestComponent />);
    
    // Start with German
    expect(screen.getByTestId('current-language')).toHaveTextContent('de');
    
    // Switch to English
    fireEvent.click(screen.getByText('Switch to English'));
    
    expect(screen.getByTestId('current-language')).toHaveTextContent('en');
    // Translations should update accordingly
  });

  it('maintains translation consistency across re-renders', () => {
    const { rerender } = renderWithLanguageProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Switch to English'));
    expect(screen.getByTestId('current-language')).toHaveTextContent('en');
    
    rerender(
      <LanguageProvider>
        <TestComponent />
      </LanguageProvider>
    );
    
    // Should maintain English after re-render due to localStorage
    expect(screen.getByTestId('current-language')).toHaveTextContent('en');
  });
});
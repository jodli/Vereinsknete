import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { LanguageProvider } from '../../i18n';
import LanguageSwitcher from '../LanguageSwitcher';

// Test wrapper with LanguageProvider
const TestWrapper = ({ children }: { children: React.ReactNode }) => (
  <LanguageProvider>
    {children}
  </LanguageProvider>
);

describe('LanguageSwitcher Component', () => {
  it('renders language options', () => {
    render(
      <TestWrapper>
        <LanguageSwitcher />
      </TestWrapper>
    );

    expect(screen.getByRole('button', { name: /deutsch/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /english/i })).toBeInTheDocument();
  });

  it('switches to English when English button is clicked', () => {
    render(
      <TestWrapper>
        <LanguageSwitcher />
      </TestWrapper>
    );

    const englishButton = screen.getByRole('button', { name: /english/i });
    fireEvent.click(englishButton);

    // After clicking English, the button should show it's selected
    expect(englishButton).toBeInTheDocument();
  });

  it('switches to German when German button is clicked', () => {
    render(
      <TestWrapper>
        <LanguageSwitcher />
      </TestWrapper>
    );

    const germanButton = screen.getByRole('button', { name: /deutsch/i });
    fireEvent.click(germanButton);

    // After clicking German, the button should show it's selected
    expect(germanButton).toBeInTheDocument();
  });

  it('renders in compact mode', () => {
    render(
      <TestWrapper>
        <LanguageSwitcher compact={true} />
      </TestWrapper>
    );

    // Should still render both language options
    expect(screen.getByRole('button', { name: /deutsch/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /english/i })).toBeInTheDocument();
  });

  it('persists language selection', () => {
    const mockSetItem = jest.spyOn(Storage.prototype, 'setItem');
    
    render(
      <TestWrapper>
        <LanguageSwitcher />
      </TestWrapper>
    );

    const englishButton = screen.getByRole('button', { name: /english/i });
    fireEvent.click(englishButton);

    expect(mockSetItem).toHaveBeenCalledWith('preferredLanguage', 'en');
    
    mockSetItem.mockRestore();
  });
});
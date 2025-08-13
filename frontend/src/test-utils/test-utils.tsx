import React, { ReactElement } from 'react';
import { render as rtlRender, RenderOptions } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { LanguageProvider } from '../i18n';
import { ToastProvider } from '../components/Toast';
import { ConfirmProvider } from '../components/ConfirmDialog';

// We do not mock navigation hooks here to allow per-test mocking; wrap with MemoryRouter

// Custom render function that includes all providers
const AllTheProviders = ({ children }: { children: React.ReactNode }) => {
  return (
    <MemoryRouter>
      <LanguageProvider>
        <ToastProvider>
          <ConfirmProvider>
            {children}
          </ConfirmProvider>
        </ToastProvider>
      </LanguageProvider>
    </MemoryRouter>
  );
};

const customRender = (
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) => rtlRender(ui, { wrapper: AllTheProviders, ...options });

// Re-export everything
export * from '@testing-library/react';
export { customRender as render };

// Custom matchers and utilities
export const waitForLoadingToFinish = () => 
  new Promise(resolve => setTimeout(resolve, 0));

export const mockConsoleError = () => {
  const originalError = console.error;
  const mockError = jest.fn();
  console.error = mockError;
  
  return {
    mockError,
    restore: () => {
      console.error = originalError;
    }
  };
};

export const mockConsoleWarn = () => {
  const originalWarn = console.warn;
  const mockWarn = jest.fn();
  console.warn = mockWarn;
  
  return {
    mockWarn,
    restore: () => {
      console.warn = originalWarn;
    }
  };
};
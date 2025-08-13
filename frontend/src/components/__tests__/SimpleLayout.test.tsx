import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import Layout from '../Layout';
import { LanguageProvider } from '../../i18n';
import { ToastProvider } from '../Toast';
import { ConfirmProvider } from '../ConfirmDialog';

// Mock react-router-dom
const mockLocation = { pathname: '/' };
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useLocation: () => mockLocation,
  Link: ({ children, to, ...props }: any) => (
    <a href={to} {...props}>{children}</a>
  ),
}));

// Test wrapper with all providers
const TestWrapper = ({ children }: { children: React.ReactNode }) => (
  <LanguageProvider>
    <ToastProvider>
      <ConfirmProvider>
        {children}
      </ConfirmProvider>
    </ToastProvider>
  </LanguageProvider>
);

describe('Layout Component - Basic Tests', () => {
  const TestContent = () => <div>Test Content</div>;

  it('renders without crashing', () => {
    render(
      <TestWrapper>
        <Layout>
          <TestContent />
        </Layout>
      </TestWrapper>
    );

    expect(screen.getByText('Test Content')).toBeInTheDocument();
  });

  it('displays app title', () => {
    render(
      <TestWrapper>
        <Layout>
          <TestContent />
        </Layout>
      </TestWrapper>
    );

    expect(screen.getByText('VereinsKnete')).toBeInTheDocument();
  });

  it('displays navigation links', () => {
    render(
      <TestWrapper>
        <Layout>
          <TestContent />
        </Layout>
      </TestWrapper>
    );

    // Check that navigation links exist (using German text as that's the default)
    expect(screen.getByRole('link', { name: /dashboard/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /klienten/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /sitzungen/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /rechnungen/i })).toBeInTheDocument();
  });

  it('has correct navigation hrefs', () => {
    render(
      <TestWrapper>
        <Layout>
          <TestContent />
        </Layout>
      </TestWrapper>
    );

    expect(screen.getByRole('link', { name: /dashboard/i })).toHaveAttribute('href', '/');
    expect(screen.getByRole('link', { name: /klienten/i })).toHaveAttribute('href', '/clients');
    expect(screen.getByRole('link', { name: /sitzungen/i })).toHaveAttribute('href', '/sessions');
    expect(screen.getByRole('link', { name: /rechnungen/i })).toHaveAttribute('href', '/invoices');
  });

  it('displays secondary navigation', () => {
    render(
      <TestWrapper>
        <Layout>
          <TestContent />
        </Layout>
      </TestWrapper>
    );

    expect(screen.getByRole('link', { name: /meine daten/i })).toHaveAttribute('href', '/profile');
    expect(screen.getByRole('link', { name: /einstellungen/i })).toHaveAttribute('href', '/settings');
  });

  it('has language switcher buttons', () => {
    render(
      <TestWrapper>
        <Layout>
          <TestContent />
        </Layout>
      </TestWrapper>
    );

    // Language switcher should have German and English options
    expect(screen.getByRole('button', { name: /deutsch/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /english/i })).toBeInTheDocument();
  });

  it('renders main content area', () => {
    render(
      <TestWrapper>
        <Layout>
          <TestContent />
        </Layout>
      </TestWrapper>
    );

    const mainElement = screen.getByRole('main');
    expect(mainElement).toBeInTheDocument();
    expect(mainElement).toContainElement(screen.getByText('Test Content'));
  });
});
// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';

// Global MSW (Mock Service Worker) test server setup so all tests have mocked API endpoints available.
// Individual test files currently import the server but do not start it, which caused network
// requests to fail and pages to remain in loading or error states. Starting it here aligns with
// typical MSW usage and fixes the failing page tests that rely on mock data.
import { server } from './test-utils/mocks/server';

// Establish API mocking before all tests.
beforeAll(() => server.listen());

// Reset any runtime request handlers we may add during the tests.
afterEach(() => server.resetHandlers());

// Clean up after the tests are finished.
afterAll(() => server.close());

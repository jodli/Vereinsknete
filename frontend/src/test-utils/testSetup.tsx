// Global test setup for all tests (JSX allowed)
import '@testing-library/jest-dom';
import { server } from './mocks/server';

// Mock react-router-dom globally with lightweight replacements
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  BrowserRouter: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="mock-router">{children}</div>
  ),
  Link: ({ children, to, ...props }: any) => (
    <a href={to} {...props}>{children}</a>
  ),
  useLocation: () => ({ pathname: '/' }),
  useNavigate: () => jest.fn(),
  useParams: () => ({}),
}));

// Mock the useLanguage hook to always return English
jest.mock('../i18n', () => {
  const actual = jest.requireActual('../i18n');
  return {
    ...actual,
    useLanguage: () => ({
      language: 'en',
      translations: actual.getTranslations('en'),
      setLanguage: jest.fn(),
    }),
  };
});

// Mock react-datepicker globally with a simple HTML date input
jest.mock('react-datepicker', () => {
  return {
    __esModule: true,
    default: ({ selected, onChange, className, placeholderText, ...props }: any) => (
      <input
        type="date"
        value={selected ? selected.toISOString().split('T')[0] : ''}
        onChange={(e) => onChange(e.target.value ? new Date(e.target.value) : null)}
        className={className}
        placeholder={placeholderText}
        {...props}
      />
    ),
  };
});

// Global mocks for browser APIs
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

// Mock IntersectionObserver (minimal noop implementation)
class MockIntersectionObserver implements IntersectionObserver {
  readonly root: Element | Document | null = null;
  readonly rootMargin: string = '';
  readonly thresholds: ReadonlyArray<number> = [];
  constructor(callback: IntersectionObserverCallback, options?: IntersectionObserverInit) { 
    // Store callback and options for mock implementation
    this.callback = callback;
    this.options = options;
  }
  
  private callback: IntersectionObserverCallback;
  private options?: IntersectionObserverInit;
  disconnect(): void { }
  observe(_target: Element): void { }
  unobserve(_target: Element): void { }
  takeRecords(): IntersectionObserverEntry[] { return []; }
}
Object.defineProperty(globalThis, 'IntersectionObserver', { value: MockIntersectionObserver, writable: true });

// Mock ResizeObserver
class MockResizeObserver implements ResizeObserver {
  constructor(callback?: ResizeObserverCallback) { 
    // Store callback for mock implementation
    this.callback = callback;
  }
  
  private callback?: ResizeObserverCallback;
  observe(_target: Element, _options?: ResizeObserverOptions): void { }
  unobserve(_target: Element): void { }
  disconnect(): void { }
}
Object.defineProperty(globalThis, 'ResizeObserver', { value: MockResizeObserver, writable: true });

// Mock URL methods
global.URL.createObjectURL = jest.fn(() => 'mocked-url');
global.URL.revokeObjectURL = jest.fn();

// Enhanced localStorage mock
const createLocalStorageMock = () => {
  let store: Record<string, string> = { preferredLanguage: 'en' };
  return {
    getItem: jest.fn((key: string) => store[key] || null),
    setItem: jest.fn((key: string, value: string) => { store[key] = value; }),
    removeItem: jest.fn((key: string) => { delete store[key]; }),
    clear: jest.fn(() => { store = { preferredLanguage: 'en' }; }),
    length: 0,
    key: jest.fn(),
  };
};
const localStorageMock = createLocalStorageMock();
Object.defineProperty(window, 'localStorage', { value: localStorageMock, writable: true });
(global as any).localStorage = localStorageMock;

// Start MSW server before all tests
beforeAll(() => {
  server.listen({ onUnhandledRequest: 'error' });
});

// Reset handlers after each test
afterEach(() => {
  server.resetHandlers();
});

// Clean up after all tests
afterAll(() => {
  server.close();
});

// Suppress specific, noisy console warnings/errors
const originalError = console.error;
const originalWarn = console.warn;
console.error = (...args: any[]) => {
  if (
    typeof args[0] === 'string' && (
      args[0].includes('Warning: ReactDOM.render is no longer supported') ||
      args[0].includes('Warning: React.createFactory() is deprecated') ||
      args[0].includes('Error fetching') ||
      args[0].includes('Error getting') ||
      args[0].includes('React does not recognize the `dateFormat` prop')
    )
  ) { return; }
  originalError.call(console, ...args);
};
console.warn = (...args: any[]) => {
  if (
    typeof args[0] === 'string' && (
      args[0].includes('componentWillReceiveProps has been renamed') ||
      args[0].includes('React Router Future Flag Warning') ||
      args[0].includes('Found null or undefined session item')
    )
  ) { return; }
  originalWarn.call(console, ...args);
};

// Reset mocks between tests
afterEach(() => {
  jest.clearAllMocks();
  localStorageMock.clear();
});

beforeEach(() => {
  localStorageMock.getItem.mockClear();
  localStorageMock.setItem.mockClear();
  localStorageMock.removeItem.mockClear();
  localStorageMock.clear.mockClear();
});

# Frontend Architecture Guidelines

This document outlines the architectural patterns, project structure, and code organization standards for the VereinsKnete frontend application.

## Tech Stack & Core Technologies

### Core Technologies
- **React 19** with TypeScript for type safety
- **React Router DOM 7** for client-side routing
- **Tailwind CSS 3** for styling with utility-first approach
- **Heroicons** for consistent iconography
- **React DatePicker** for date inputs

### Build Status
- ✅ TypeScript compilation: No errors
- ✅ Dependencies: React 19, @heroicons/react 2.2.0, TypeScript 4.9.5
- ✅ Build process: Successful with only minor ESLint warnings
- ✅ Bundle size: 137.56 kB (optimized)

## Project Structure

```
src/
├── components/         # Reusable UI components
│   ├── UI.tsx         # Core UI component library
│   ├── Layout.tsx     # App layout with sidebar
│   ├── LanguageSwitcher.tsx  # Language selection
│   ├── Toast.tsx      # Toast notification system
│   └── ConfirmDialog.tsx     # Confirmation dialogs
├── pages/              # Page-level components
│   ├── DashboardPage.tsx     # Dashboard with metrics
│   ├── ClientsPage.tsx       # Client list view
│   ├── ClientFormPage.tsx    # Client create/edit
│   ├── SessionsPage.tsx      # Session list view
│   ├── SessionFormPage.tsx   # Session create/edit
│   └── ProfilePage.tsx       # User profile settings
├── services/           # API service layer
│   └── api.ts         # Centralized API calls
├── types/              # TypeScript type definitions
│   └── index.ts       # Shared type definitions
├── i18n/               # Internationalization
│   ├── en.ts          # English translations
│   ├── de.ts          # German translations
│   └── LanguageContext.tsx   # Language context
├── utils/              # Utility functions
│   └── hooks.ts       # Custom React hooks
└── App.tsx            # Main application component
```

## Page Structure Pattern

Every page should follow this standardized structure with consistent layout and state management:

```tsx
const PageName: React.FC = () => {
  const { translations } = useLanguage();
  const { data, loading, error, refetch } = useAsyncData(fetchFunction);
  const [showToast, setShowToast] = useState(false);

  // Handle loading state
  if (loading) {
    return <LoadingState message={translations.common.loading} />;
  }

  // Handle error state
  if (error) {
    return (
      <ErrorState 
        message={error} 
        onRetry={refetch}
        retryLabel={translations.common.tryAgain}
      />
    );
  }

  return (
    <div className="space-y-6">
      {/* Page header with consistent styling */}
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">
          {translations.pageName.title}
        </h1>
        <div className="flex items-center space-x-3">
          {/* Action buttons */}
        </div>
      </div>
      
      {/* Page content with proper spacing */}
      <div className="space-y-6">
        {/* Content sections */}
      </div>

      {/* Toast notifications */}
      {showToast && (
        <Toast
          type="success|error|warning|info"
          message={translations.common.success}
          onClose={() => setShowToast(false)}
        />
      )}
    </div>
  );
};
```

## Navigation & Routing

### Navigation Structure
The app uses a sidebar navigation with:
- **Main Navigation**: Dashboard, Clients, Sessions, Invoices
- **Secondary Navigation**: Profile, Settings
- **Color-coded icons**: Each section has a distinct color for visual hierarchy

### Route Patterns
- List pages: `/clients`, `/sessions`, `/invoices`
- Form pages: `/clients/new`, `/clients/:id`, `/sessions/new`, `/sessions/:id`
- Always redirect unknown routes to dashboard: `<Route path="*" element={<Navigate to="/" replace />} />`

### Router Configuration
```tsx
// App.tsx routing structure
const App: React.FC = () => {
  return (
    <BrowserRouter>
      <LanguageProvider>
        <Layout>
          <Routes>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/clients" element={<ClientsPage />} />
            <Route path="/clients/new" element={<ClientFormPage />} />
            <Route path="/clients/:id" element={<ClientFormPage />} />
            <Route path="/sessions" element={<SessionsPage />} />
            <Route path="/sessions/new" element={<SessionFormPage />} />
            <Route path="/sessions/:id" element={<SessionFormPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Layout>
      </LanguageProvider>
    </BrowserRouter>
  );
};
```

## Component Organization

### Component Hierarchy
- **Layout Components**: Handle app structure and navigation
- **Page Components**: Top-level route components
- **Feature Components**: Business logic components
- **UI Components**: Reusable interface elements
- **Utility Components**: Helper and wrapper components

### Component Composition Patterns
```tsx
// Container/Presenter pattern
const ClientsPageContainer: React.FC = () => {
  const { data: clients, loading, error } = useAsyncData(getClients);
  
  return (
    <ClientsPagePresenter 
      clients={clients}
      loading={loading}
      error={error}
    />
  );
};

// Higher-Order Component pattern
const withErrorBoundary = <P extends object>(
  Component: React.ComponentType<P>
) => {
  return (props: P) => (
    <ErrorBoundary>
      <Component {...props} />
    </ErrorBoundary>
  );
};
```

### Code Splitting
```tsx
// Lazy load pages for better performance
const DashboardPage = lazy(() => import('./pages/DashboardPage'));
const ClientsPage = lazy(() => import('./pages/ClientsPage'));

// Wrap with Suspense
<Suspense fallback={<LoadingState />}>
  <Routes>
    <Route path="/" element={<DashboardPage />} />
    <Route path="/clients" element={<ClientsPage />} />
  </Routes>
</Suspense>
```

## TypeScript Architecture

### Type Organization
```typescript
// src/types/index.ts - Centralized type definitions
export interface Client {
  id: number;
  name: string;
  address: string;
  contact_person?: string;
  default_hourly_rate: number;
  created_at: string;
  updated_at: string;
}

export interface NewClient {
  name: string;
  address: string;
  contact_person?: string;
  default_hourly_rate: number;
}

export interface Session {
  id: number;
  client_id: number;
  name: string;
  date: string;
  start_time: string;
  end_time: string;
  created_at: string;
  updated_at: string;
}

// API Response types
export interface ApiResponse<T> {
  data: T;
  status: string;
  message?: string;
}

// Form types
export interface FormState<T> {
  data: T;
  errors: Record<keyof T, string>;
  touched: Record<keyof T, boolean>;
  isValid: boolean;
}
```

### Component Typing Patterns
```tsx
// Proper component interface definitions
interface ButtonProps {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  onClick?: () => void;
  disabled?: boolean;
  loading?: boolean;
}

// Generic component types
interface TableProps<T> {
  data: T[];
  columns: TableColumn<T>[];
  onRowClick?: (row: T) => void;
  loading?: boolean;
}

// Event handler types
interface FormProps {
  onSubmit: (data: FormData) => void;
  onChange: (field: string, value: any) => void;
  onBlur: (field: string) => void;
}
```

## Error Boundaries

### Global Error Boundary
```tsx
class ErrorBoundary extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
    // Log to error reporting service
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-gray-900 mb-4">
              Something went wrong
            </h1>
            <button 
              onClick={() => window.location.reload()}
              className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
            >
              Reload page
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
```

## Code Quality Standards

### Naming Conventions
- **Components**: PascalCase (`DashboardPage`, `ClientFormPage`)
- **Files**: PascalCase for components, camelCase for utilities
- **Variables**: camelCase (`clientData`, `isLoading`)
- **Constants**: UPPER_SNAKE_CASE (`API_URL`)
- **Interfaces**: PascalCase with descriptive names (`ButtonProps`, `ClientFormData`)

### Component Organization Rules
- Keep components focused and single-purpose
- Extract reusable logic into custom hooks (see `src/utils/hooks.ts`)
- Use proper prop typing with interfaces for all components
- All UI components include proper accessibility attributes
- Components handle loading, error, and empty states consistently

### File Organization
```
// Component file structure
ComponentName/
├── index.ts          # Export barrel
├── ComponentName.tsx # Main component
├── ComponentName.test.tsx # Tests
└── ComponentName.stories.tsx # Storybook stories (if applicable)

// Or for simple components
ComponentName.tsx
```

## Environment Configuration

### Environment-Specific Configs
```typescript
// Environment-specific configs
const config = {
  development: {
    API_URL: 'http://localhost:8080/api',
    DEBUG: true,
    LOG_LEVEL: 'debug'
  },
  production: {
    API_URL: 'https://api.vereinsknete.com',
    DEBUG: false,
    LOG_LEVEL: 'error'
  }
};

export default config[process.env.NODE_ENV || 'development'];
```

## Security Best Practices

### Input Validation
- Validate all user inputs client-side and server-side
- Sanitize data before rendering
- Use TypeScript for compile-time type checking

### XSS Prevention
- Avoid `dangerouslySetInnerHTML` unless absolutely necessary
- Sanitize user-generated content
- Use Content Security Policy headers

### Authentication Patterns
```tsx
// Protected route component
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
};
```

This document should be referenced when working on application architecture, routing, component organization, or TypeScript patterns in the VereinsKnete application.
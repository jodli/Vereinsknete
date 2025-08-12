# Frontend State Management Guidelines

This document outlines state management patterns, custom hooks, and data handling strategies for the VereinsKnete frontend application.

## Custom Hooks (src/utils/hooks.ts)

Use the provided custom hooks for common patterns and consistent state management.

### Data Fetching Hook
```tsx
// useAsyncData - Standardized data fetching with loading/error states
const { data, loading, error, refetch } = useAsyncData(fetchClients);

// Usage example
const ClientsPage: React.FC = () => {
  const { data: clients, loading, error, refetch } = useAsyncData(getClients);
  
  if (loading) return <LoadingState />;
  if (error) return <ErrorState message={error} onRetry={refetch} />;
  
  return <ClientList clients={clients} />;
};
```

### Form State Management
```tsx
// useFormState - Form management with validation and error handling
const { 
  formData, 
  errors, 
  touched, 
  handleChange, 
  handleBlur, 
  setFieldValue, 
  resetForm, 
  isValid 
} = useFormState(initialData, validationRules);

// Usage example
const ClientForm: React.FC = () => {
  const { formData, errors, handleChange, isValid } = useFormState({
    name: '',
    email: '',
    phone: ''
  }, {
    name: (value) => value.length < 2 ? 'Name too short' : null,
    email: (value) => !/\S+@\S+\.\S+/.test(value) ? 'Invalid email' : null
  });

  return (
    <form>
      <Input
        id="name"
        label="Name"
        value={formData.name}
        onChange={handleChange}
        error={errors.name}
      />
    </form>
  );
};
```

### Local Storage Persistence
```tsx
// useLocalStorage - Persistent local storage with TypeScript support
const [settings, setSettings] = useLocalStorage('userSettings', defaultSettings);

// Usage example
const SettingsPage: React.FC = () => {
  const [userPrefs, setUserPrefs] = useLocalStorage('preferences', {
    theme: 'light',
    language: 'en',
    notifications: true
  });

  const updateTheme = (theme: string) => {
    setUserPrefs(prev => ({ ...prev, theme }));
  };

  return (
    <div>
      <Button onClick={() => updateTheme('dark')}>
        Switch to Dark Mode
      </Button>
    </div>
  );
};
```

### Debounced Input
```tsx
// useDebounce - Optimized search input handling
const debouncedSearchTerm = useDebounce(searchTerm, 300);

// Usage example
const SearchableList: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const debouncedSearch = useDebounce(searchTerm, 300);
  
  const { data: results } = useAsyncData(() => 
    searchClients(debouncedSearch), [debouncedSearch]
  );

  return (
    <div>
      <Input
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        placeholder="Search clients..."
      />
      <ClientList clients={results} />
    </div>
  );
};
```

## Context-Based State Management

### Language Context
```tsx
// Language context usage
const { translations, language, setLanguage } = useLanguage();

// Usage in components
const Header: React.FC = () => {
  const { translations, setLanguage } = useLanguage();
  
  return (
    <div>
      <h1>{translations.dashboard.title}</h1>
      <button onClick={() => setLanguage('de')}>
        Deutsch
      </button>
    </div>
  );
};
```

### Toast Notifications
```tsx
// Toast notifications context
const { showToast } = useToast();

// Usage examples
const handleSave = async () => {
  try {
    await saveClient(clientData);
    showToast('success', 'Client saved successfully');
  } catch (error) {
    showToast('error', 'Failed to save client');
  }
};

// Different toast types
showToast('success', 'Operation completed successfully');
showToast('error', 'Something went wrong');
showToast('warning', 'Please check your input');
showToast('info', 'Data has been updated');
```

## Advanced State Patterns

### Reducer Pattern for Complex State
```tsx
// Form reducer for complex form state
interface FormState {
  data: Record<string, any>;
  errors: Record<string, string>;
  touched: Record<string, boolean>;
  isSubmitting: boolean;
}

type FormAction = 
  | { type: 'SET_FIELD'; field: string; value: any }
  | { type: 'SET_ERROR'; field: string; error: string }
  | { type: 'SET_TOUCHED'; field: string }
  | { type: 'SET_SUBMITTING'; isSubmitting: boolean }
  | { type: 'RESET' };

const formReducer = (state: FormState, action: FormAction): FormState => {
  switch (action.type) {
    case 'SET_FIELD':
      return {
        ...state,
        data: { ...state.data, [action.field]: action.value },
        errors: { ...state.errors, [action.field]: '' }
      };
    case 'SET_ERROR':
      return {
        ...state,
        errors: { ...state.errors, [action.field]: action.error }
      };
    case 'SET_TOUCHED':
      return {
        ...state,
        touched: { ...state.touched, [action.field]: true }
      };
    case 'SET_SUBMITTING':
      return { ...state, isSubmitting: action.isSubmitting };
    case 'RESET':
      return initialFormState;
    default:
      return state;
  }
};
```

### Custom Context Hook Pattern
```tsx
// Create custom context for app-wide state
interface AppContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const useAppContext = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useAppContext must be used within AppProvider');
  }
  return context;
};

// Provider component
export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  
  const login = async (credentials: LoginCredentials) => {
    const userData = await authenticateUser(credentials);
    setUser(userData);
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('authToken');
  };

  return (
    <AppContext.Provider value={{
      user,
      isAuthenticated: !!user,
      login,
      logout
    }}>
      {children}
    </AppContext.Provider>
  );
};
```

## Form Validation Patterns

### Real-time Validation
```tsx
// Real-time validation with debouncing
const ClientForm: React.FC = () => {
  const [formData, setFormData] = useState(initialData);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});

  // Debounced validation
  const debouncedFormData = useDebounce(formData, 300);

  useEffect(() => {
    validateForm(debouncedFormData).then(setErrors);
  }, [debouncedFormData]);

  const handleChange = (field: string, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleBlur = (field: string) => {
    setTouched(prev => ({ ...prev, [field]: true }));
  };

  return (
    <form>
      <Input
        id="email"
        label="Email"
        value={formData.email}
        onChange={(e) => handleChange('email', e.target.value)}
        onBlur={() => handleBlur('email')}
        error={touched.email ? errors.email : undefined}
      />
    </form>
  );
};
```

### Validation Rules
```tsx
// Validation rule patterns
const validationRules = {
  required: (value: any) => 
    !value || value.toString().trim() === '' ? 'This field is required' : null,
  
  email: (value: string) => 
    !/\S+@\S+\.\S+/.test(value) ? 'Invalid email format' : null,
  
  minLength: (min: number) => (value: string) =>
    value.length < min ? `Minimum ${min} characters required` : null,
  
  maxLength: (max: number) => (value: string) =>
    value.length > max ? `Maximum ${max} characters allowed` : null,
  
  positive: (value: number) =>
    value <= 0 ? 'Value must be positive' : null,
  
  phone: (value: string) =>
    !/^\+?[1-9]\d{1,14}$/.test(value) ? 'Invalid phone number' : null
};

// Compose validation rules
const clientValidation = {
  name: [validationRules.required, validationRules.minLength(2)],
  email: [validationRules.required, validationRules.email],
  hourlyRate: [validationRules.required, validationRules.positive]
};
```

## API Integration Patterns

### Centralized API Service
```tsx
// API service structure (src/services/api.ts)
export const api = {
  // Client operations
  clients: {
    getAll: () => fetch('/api/clients').then(res => res.json()),
    getById: (id: number) => fetch(`/api/clients/${id}`).then(res => res.json()),
    create: (data: NewClient) => fetch('/api/clients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    }).then(res => res.json()),
    update: (id: number, data: UpdateClient) => fetch(`/api/clients/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    }).then(res => res.json()),
    delete: (id: number) => fetch(`/api/clients/${id}`, {
      method: 'DELETE'
    }).then(res => res.json())
  },
  
  // Session operations
  sessions: {
    getAll: () => fetch('/api/sessions').then(res => res.json()),
    // ... other session methods
  }
};
```

### Error Handling in API Calls
```tsx
// Standardized error handling
const handleApiCall = async <T>(
  apiCall: () => Promise<T>,
  successMessage?: string,
  errorMessage?: string
): Promise<T | null> => {
  try {
    const result = await apiCall();
    if (successMessage) {
      showToast('success', successMessage);
    }
    return result;
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Unknown error';
    showToast('error', errorMessage || `Operation failed: ${message}`);
    return null;
  }
};

// Usage
const handleSaveClient = async (clientData: NewClient) => {
  const result = await handleApiCall(
    () => api.clients.create(clientData),
    'Client created successfully',
    'Failed to create client'
  );
  
  if (result) {
    navigate('/clients');
  }
};
```

## Performance Optimization

### Memoization Patterns
```tsx
// Memoize expensive calculations
const ExpensiveComponent: React.FC<{ data: any[] }> = ({ data }) => {
  const processedData = useMemo(() => {
    return data.map(item => ({
      ...item,
      calculated: expensiveCalculation(item)
    }));
  }, [data]);

  return <div>{/* Render processed data */}</div>;
};

// Memoize callback functions
const ParentComponent: React.FC = () => {
  const [count, setCount] = useState(0);
  
  const handleIncrement = useCallback(() => {
    setCount(prev => prev + 1);
  }, []);

  return <ChildComponent onIncrement={handleIncrement} />;
};
```

### State Updates Optimization
```tsx
// Batch state updates
const handleMultipleUpdates = () => {
  // React automatically batches these in React 18
  setLoading(true);
  setError(null);
  setData([]);
};

// Use functional updates for state based on previous state
const handleIncrement = () => {
  setCount(prevCount => prevCount + 1);
};
```

This document should be referenced when working on state management, form handling, data fetching, or complex component interactions in the VereinsKnete application.
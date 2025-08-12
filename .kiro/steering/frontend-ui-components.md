# Frontend UI Components Guidelines

This document outlines the standardized UI component library for the VereinsKnete frontend application.

## UI Components (src/components/UI.tsx)

All components are fully typed with TypeScript and follow accessibility best practices.

### Button Component
```tsx
<Button 
  variant="primary|secondary|danger|success|warning" 
  size="sm|md|lg"
  loading={isLoading}
  disabled={isDisabled}
  onClick={handleClick}
>
  Action Text
</Button>
```

**Variants:**
- `primary`: Blue background for main actions
- `secondary`: White background with border for secondary actions
- `danger`: Red background for destructive actions
- `success`: Green background for positive actions
- `warning`: Amber background for warning actions

**Features:**
- Loading state with spinner
- Hover animations (scale transform)
- Focus ring for accessibility
- Disabled state handling

### Form Components

#### Input Component
```tsx
<Input 
  id="field-id" 
  label="Field Label" 
  type="text|email|number|password"
  value={value} 
  onChange={handleChange}
  onBlur={handleBlur}
  required={true}
  error={errorMessage}
  success={isValid}
  helpText="Optional help text"
  leftIcon={<EnvelopeIcon />}
  rightAddon="€"
/>
```

**Features:**
- Built-in validation states (error/success)
- Icon support (left icon, right addon)
- Accessibility labels and ARIA attributes
- Focus states with ring styling
- Help text and error message display

#### Textarea Component
```tsx
<Textarea
  id="description"
  label="Description"
  value={description}
  onChange={handleChange}
  rows={4}
  error={errorMessage}
  helpText="Enter detailed description"
/>
```

#### Select Component
```tsx
<Select 
  id="select-id"
  label="Select Label"
  options={[{value: 'key', label: 'Display', disabled?: boolean}]}
  value={selectedValue}
  onChange={handleChange}
  placeholder="Choose an option"
  error={errorMessage}
/>
```

### Layout Components

#### Card Component
```tsx
<Card 
  title="Optional Title"
  subtitle="Optional subtitle"
  variant="default|elevated|outlined"
  padding="none|sm|md|lg"
  actions={<Button>Action</Button>}
>
  Content goes here
</Card>
```

**Variants:**
- `default`: Subtle shadow with border
- `elevated`: Larger shadow for prominence
- `outlined`: Border-only styling

#### Table Component
```tsx
<Table 
  columns={[{
    key: 'field', 
    label: 'Display Name',
    sortable?: boolean,
    render?: (value, row) => <CustomComponent />,
    className?: 'text-right'
  }]}
  data={tableData}
  onRowClick={handleRowClick}
  loading={isLoading}
  emptyMessage="No data found"
/>
```

**Features:**
- Built-in loading skeleton
- Empty state handling
- Row click handlers
- Custom cell rendering
- Responsive horizontal scroll

### State Components

#### Loading Components
```tsx
// Loading states
<LoadingState message="Loading data..." />
<Spinner size="sm|md|lg" />
```

#### Error Components
```tsx
<ErrorState 
  title="Something went wrong"
  message={errorMessage}
  onRetry={handleRetry}
  retryLabel="Try Again"
/>
```

#### Empty State
```tsx
<EmptyState
  icon="📋"
  title="No items found"
  description="Get started by creating your first item"
  action={<Button>Create Item</Button>}
/>
```

#### Status Badge
```tsx
<StatusBadge status="paid|sent|created|overdue|draft">
  Status Text
</StatusBadge>
```

**Status Colors:**
- `paid`: Green (emerald-50/700)
- `sent`: Amber (amber-50/700)
- `created/draft`: Gray (gray-50/700)
- `overdue`: Red (red-50/700)

## Component Composition Patterns

### Compound Components
```tsx
// Flexible component composition
const Modal = ({ children, isOpen, onClose }) => {
  if (!isOpen) return null;
  
  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen px-4">
        {children}
      </div>
    </div>
  );
};

Modal.Header = ({ children }) => (
  <div className="px-6 py-4 border-b border-gray-200">{children}</div>
);

Modal.Body = ({ children }) => (
  <div className="px-6 py-4">{children}</div>
);

Modal.Footer = ({ children }) => (
  <div className="px-6 py-4 border-t border-gray-200 flex justify-end space-x-2">
    {children}
  </div>
);
```

### Render Props Pattern
```tsx
const DataFetcher = ({ url, children }) => {
  const { data, loading, error } = useFetch(url);
  return children({ data, loading, error });
};

// Usage
<DataFetcher url="/api/clients">
  {({ data, loading, error }) => (
    loading ? <Spinner /> : 
    error ? <ErrorMessage error={error} /> :
    <ClientList clients={data} />
  )}
</DataFetcher>
```

## Accessibility Standards (WCAG 2.1 AA)

### Form Accessibility
```tsx
// Proper form labeling
<label htmlFor="email" className="sr-only">Email address</label>
<input id="email" aria-describedby="email-help" />
<div id="email-help" className="text-sm text-gray-500">We'll never share your email</div>
```

### Focus Management
```tsx
<button 
  aria-expanded={isOpen}
  aria-controls="dropdown-menu"
  onClick={toggleDropdown}
>
```

### Skip Links
```tsx
<a href="#main-content" className="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4">
  Skip to main content
</a>
```

## Component Testing

### Component Testing Patterns
```tsx
// Test user interactions
test('should submit form with valid data', async () => {
  render(<ClientForm onSubmit={mockSubmit} />);
  
  await user.type(screen.getByLabelText(/name/i), 'Test Client');
  await user.type(screen.getByLabelText(/email/i), 'test@example.com');
  await user.click(screen.getByRole('button', { name: /save/i }));
  
  expect(mockSubmit).toHaveBeenCalledWith({
    name: 'Test Client',
    email: 'test@example.com'
  });
});
```

## TypeScript Component Patterns

### Proper Component Typing
```tsx
interface ButtonProps {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'danger';
  onClick?: () => void;
  disabled?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  children,
  variant = 'primary',
  onClick,
  disabled = false
}) => {
  // Component implementation
};
```

This document should be referenced when working on UI components, form elements, or interactive elements in the VereinsKnete application.
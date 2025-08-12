# Frontend UX Patterns Guidelines

This document outlines user experience patterns, interaction design, and feedback mechanisms for the VereinsKnete frontend application.

## Toast Notifications (src/components/Toast.tsx)

Use the centralized toast system for user feedback with consistent styling and behavior.

### Toast Usage Patterns
```tsx
// Import and use the toast hook
const { showToast } = useToast();

// Show different types of toasts
showToast('success', 'Client created successfully');
showToast('error', 'Failed to save changes');
showToast('warning', 'Please check your input');
showToast('info', 'Data has been updated');

// Toast component usage (automatic positioning and styling)
<Toast
  type="success|error|warning|info"
  message="Your message here"
  onClose={() => setShowToast(false)}
  autoClose={true}
  duration={5000}
/>
```

### Toast Design Patterns
- **Success**: Green background with checkmark icon
- **Error**: Red background with exclamation icon
- **Warning**: Amber background with warning icon
- **Info**: Blue background with info icon
- **Auto-dismiss**: 5 seconds for success/info, manual dismiss for errors
- **Positioning**: Top-right corner with slide-in animation

## Confirmation Dialogs (src/components/ConfirmDialog.tsx)

Use for destructive actions to prevent accidental data loss.

### Confirmation Dialog Patterns
```tsx
const [showConfirm, setShowConfirm] = useState(false);

const handleDelete = () => {
  setShowConfirm(true);
};

const confirmDelete = async () => {
  try {
    await deleteClient(clientId);
    showToast('success', 'Client deleted successfully');
    navigate('/clients');
  } catch (error) {
    showToast('error', 'Failed to delete client');
  }
  setShowConfirm(false);
};

// In render
<ConfirmDialog
  isOpen={showConfirm}
  onClose={() => setShowConfirm(false)}
  onConfirm={confirmDelete}
  title="Delete Client"
  message="Are you sure you want to delete this client? This action cannot be undone."
  confirmLabel="Delete"
  cancelLabel="Cancel"
  type="danger"
/>
```

### Confirmation Dialog Types
- **Danger**: Red confirm button for destructive actions
- **Warning**: Amber confirm button for potentially harmful actions
- **Info**: Blue confirm button for informational confirmations

## Loading States

### Page-Level Loading
```tsx
// Use standardized loading components
if (loading) {
  return <LoadingState message={translations.common.loading} />;
}

// Custom loading messages
<LoadingState message="Generating invoice..." />
<LoadingState message="Saving changes..." />
```

### Component-Level Loading
```tsx
// Inline loading spinner
<Spinner size="md" className="mx-auto" />

// Table loading state (built-in)
<Table columns={columns} data={data} loading={loading} />

// Button loading state
<Button loading={isSubmitting} disabled={isSubmitting}>
  {isSubmitting ? 'Saving...' : 'Save'}
</Button>
```

### Skeleton Loading
```tsx
// Card skeleton
<div className="animate-pulse">
  <div className="bg-gray-200 rounded-lg h-32 mb-4"></div>
  <div className="bg-gray-200 rounded h-4 mb-2"></div>
  <div className="bg-gray-200 rounded h-4 w-3/4"></div>
</div>

// List skeleton
{[...Array(5)].map((_, i) => (
  <div key={i} className="animate-pulse">
    <div className="bg-gray-200 rounded h-16 mb-2"></div>
  </div>
))}
```

## Error Handling

### Page-Level Errors
```tsx
// Use standardized error components
if (error) {
  return (
    <ErrorState 
      message={error} 
      onRetry={refetch}
      retryLabel={translations.common.tryAgain}
    />
  );
}

// Custom error states
<ErrorState
  title="Failed to load data"
  message="Unable to connect to the server. Please check your connection."
  onRetry={handleRetry}
  retryLabel="Try Again"
/>
```

### Form Field Errors
```tsx
// Built into Input components
<Input 
  id="email"
  label="Email"
  value={email}
  onChange={handleChange}
  error={errors.email}
  success={isEmailValid}
/>

// Error styling automatically applied
// - Red border and focus ring
// - Error icon in input
// - Error message below field
```

### Error Recovery Patterns
```tsx
// Graceful degradation
const ClientList: React.FC = () => {
  const { data: clients, error } = useAsyncData(getClients);
  
  if (error) {
    return (
      <div className="text-center py-8">
        <p className="text-gray-500 mb-4">Unable to load clients</p>
        <Button onClick={() => window.location.reload()}>
          Refresh Page
        </Button>
      </div>
    );
  }
  
  return <Table data={clients} columns={columns} />;
};
```

## Empty States

### Data Empty States
```tsx
<EmptyState
  icon="👥"
  title="No clients yet"
  description="Get started by adding your first client"
  action={
    <Button onClick={() => navigate('/clients/new')}>
      Add Client
    </Button>
  }
/>

// Different empty state contexts
<EmptyState
  icon="📋"
  title="No sessions found"
  description="No sessions match your current filters"
  action={
    <Button variant="secondary" onClick={clearFilters}>
      Clear Filters
    </Button>
  }
/>
```

### Search Empty States
```tsx
// When search returns no results
<EmptyState
  icon="🔍"
  title="No results found"
  description={`No clients match "${searchTerm}"`}
  action={
    <Button variant="secondary" onClick={() => setSearchTerm('')}>
      Clear Search
    </Button>
  }
/>
```

## Form UX Patterns

### Real-time Validation
```tsx
// Show validation as user types (debounced)
const ClientForm: React.FC = () => {
  const { formData, errors, touched, handleChange, handleBlur } = useFormState(
    initialData,
    validationRules
  );

  return (
    <form className="space-y-6">
      <Input
        id="email"
        label="Email"
        value={formData.email}
        onChange={handleChange}
        onBlur={handleBlur}
        error={touched.email ? errors.email : undefined}
        success={touched.email && !errors.email && formData.email}
      />
    </form>
  );
};
```

### Form Submission States
```tsx
const [isSubmitting, setIsSubmitting] = useState(false);

const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setIsSubmitting(true);
  
  try {
    await saveClient(formData);
    showToast('success', 'Client saved successfully');
    navigate('/clients');
  } catch (error) {
    showToast('error', 'Failed to save client');
  } finally {
    setIsSubmitting(false);
  }
};

// Submit button with loading state
<Button 
  type="submit" 
  loading={isSubmitting}
  disabled={isSubmitting || !isFormValid}
>
  {isSubmitting ? 'Saving...' : 'Save Client'}
</Button>
```

### Form Navigation Protection
```tsx
// Warn user about unsaved changes
const useUnsavedChanges = (hasUnsavedChanges: boolean) => {
  useEffect(() => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      if (hasUnsavedChanges) {
        e.preventDefault();
        e.returnValue = '';
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, [hasUnsavedChanges]);
};
```

## Progressive Disclosure

### Expandable Sections
```tsx
const [isExpanded, setIsExpanded] = useState(false);

return (
  <Card>
    <div className="flex items-center justify-between">
      <h3>Advanced Settings</h3>
      <Button
        variant="secondary"
        size="sm"
        onClick={() => setIsExpanded(!isExpanded)}
      >
        {isExpanded ? 'Hide' : 'Show'} Details
      </Button>
    </div>
    
    {isExpanded && (
      <div className="mt-4 space-y-4">
        {/* Advanced form fields */}
      </div>
    )}
  </Card>
);
```

### Step-by-Step Flows
```tsx
// Multi-step form pattern
const [currentStep, setCurrentStep] = useState(1);
const totalSteps = 3;

const nextStep = () => setCurrentStep(prev => Math.min(prev + 1, totalSteps));
const prevStep = () => setCurrentStep(prev => Math.max(prev - 1, 1));

return (
  <div>
    {/* Progress indicator */}
    <div className="mb-8">
      <div className="flex items-center">
        {Array.from({ length: totalSteps }, (_, i) => (
          <div
            key={i}
            className={`flex items-center ${i < totalSteps - 1 ? 'flex-1' : ''}`}
          >
            <div
              className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium
                ${i + 1 <= currentStep 
                  ? 'bg-blue-600 text-white' 
                  : 'bg-gray-200 text-gray-600'
                }`}
            >
              {i + 1}
            </div>
            {i < totalSteps - 1 && (
              <div
                className={`flex-1 h-1 mx-4 ${
                  i + 1 < currentStep ? 'bg-blue-600' : 'bg-gray-200'
                }`}
              />
            )}
          </div>
        ))}
      </div>
    </div>

    {/* Step content */}
    {currentStep === 1 && <Step1Component />}
    {currentStep === 2 && <Step2Component />}
    {currentStep === 3 && <Step3Component />}

    {/* Navigation */}
    <div className="flex justify-between mt-8">
      <Button
        variant="secondary"
        onClick={prevStep}
        disabled={currentStep === 1}
      >
        Previous
      </Button>
      <Button
        onClick={currentStep === totalSteps ? handleSubmit : nextStep}
      >
        {currentStep === totalSteps ? 'Complete' : 'Next'}
      </Button>
    </div>
  </div>
);
```

## Accessibility UX Patterns

### Focus Management
```tsx
// Focus first input on page load
useEffect(() => {
  const firstInput = document.querySelector('input, select, textarea');
  if (firstInput instanceof HTMLElement) {
    firstInput.focus();
  }
}, []);

// Focus management in modals
const ModalComponent: React.FC = ({ isOpen }) => {
  const modalRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isOpen && modalRef.current) {
      const focusableElements = modalRef.current.querySelectorAll(
        'button, input, select, textarea, [tabindex]:not([tabindex="-1"])'
      );
      if (focusableElements.length > 0) {
        (focusableElements[0] as HTMLElement).focus();
      }
    }
  }, [isOpen]);

  return (
    <div ref={modalRef} role="dialog" aria-modal="true">
      {/* Modal content */}
    </div>
  );
};
```

### Keyboard Navigation
```tsx
// Keyboard shortcuts
useEffect(() => {
  const handleKeyDown = (e: KeyboardEvent) => {
    // Ctrl/Cmd + S to save
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault();
      handleSave();
    }
    
    // Escape to close modal
    if (e.key === 'Escape' && isModalOpen) {
      closeModal();
    }
  };

  document.addEventListener('keydown', handleKeyDown);
  return () => document.removeEventListener('keydown', handleKeyDown);
}, [isModalOpen]);
```

### Screen Reader Support
```tsx
// Announce dynamic content changes
const [announcement, setAnnouncement] = useState('');

const announceToScreenReader = (message: string) => {
  setAnnouncement(message);
  setTimeout(() => setAnnouncement(''), 1000);
};

// Usage
const handleSave = async () => {
  try {
    await saveData();
    announceToScreenReader('Data saved successfully');
  } catch (error) {
    announceToScreenReader('Error saving data');
  }
};

// Screen reader announcement area
<div
  aria-live="polite"
  aria-atomic="true"
  className="sr-only"
>
  {announcement}
</div>
```

## Mobile UX Considerations

### Touch-Friendly Interactions
- Minimum 44px touch targets
- Adequate spacing between interactive elements
- Swipe gestures for navigation where appropriate
- Pull-to-refresh for data lists

### Mobile-Specific Patterns
```tsx
// Mobile-optimized table
<div className="md:hidden">
  {/* Card-based layout for mobile */}
  {data.map(item => (
    <Card key={item.id} className="mb-4">
      <div className="space-y-2">
        <h3 className="font-medium">{item.name}</h3>
        <p className="text-sm text-gray-600">{item.details}</p>
      </div>
    </Card>
  ))}
</div>

<div className="hidden md:block">
  {/* Table layout for desktop */}
  <Table data={data} columns={columns} />
</div>
```

This document should be referenced when working on user interactions, feedback mechanisms, loading states, or accessibility features in the VereinsKnete application.
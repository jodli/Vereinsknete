import React from 'react';
import { render, screen, fireEvent, waitFor } from '../../test-utils/test-utils';
import { 
  Button, 
  Input, 
  Select, 
  Table, 
  Card, 
  LoadingState, 
  ErrorState, 
  StatusBadge, 
  EmptyState,
  Spinner,
  Textarea
} from '../UI';

describe('Button Component', () => {
  it('renders with default props', () => {
    render(<Button>Click me</Button>);
    const button = screen.getByRole('button', { name: /click me/i });
    expect(button).toBeInTheDocument();
    expect(button).toHaveClass('bg-blue-600'); // primary variant
  });

  it('renders different variants correctly', () => {
    const { rerender } = render(<Button variant="secondary">Secondary</Button>);
    expect(screen.getByRole('button')).toHaveClass('bg-white');

    rerender(<Button variant="danger">Danger</Button>);
    expect(screen.getByRole('button')).toHaveClass('bg-red-600');

    rerender(<Button variant="success">Success</Button>);
    expect(screen.getByRole('button')).toHaveClass('bg-emerald-600');

    rerender(<Button variant="warning">Warning</Button>);
    expect(screen.getByRole('button')).toHaveClass('bg-amber-600');
  });

  it('renders different sizes correctly', () => {
    const { rerender } = render(<Button size="sm">Small</Button>);
    expect(screen.getByRole('button')).toHaveClass('px-3 py-1.5 text-sm');

    rerender(<Button size="lg">Large</Button>);
    expect(screen.getByRole('button')).toHaveClass('px-6 py-3 text-base');
  });

  it('handles loading state', () => {
    render(<Button loading>Loading</Button>);
    const button = screen.getByRole('button');
    expect(button).toBeDisabled();
    expect(screen.getByText('Loading')).toBeInTheDocument();
    // Check for loading spinner
    expect(button.querySelector('.animate-spin')).toBeInTheDocument();
  });

  it('handles disabled state', () => {
    render(<Button disabled>Disabled</Button>);
    const button = screen.getByRole('button');
    expect(button).toBeDisabled();
    expect(button).toHaveClass('disabled:opacity-50');
  });

  it('calls onClick handler', () => {
    const handleClick = jest.fn();
    render(<Button onClick={handleClick}>Click me</Button>);
    
    fireEvent.click(screen.getByRole('button'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('does not call onClick when disabled', () => {
    const handleClick = jest.fn();
    render(<Button onClick={handleClick} disabled>Disabled</Button>);
    
    fireEvent.click(screen.getByRole('button'));
    expect(handleClick).not.toHaveBeenCalled();
  });
});

describe('Input Component', () => {
  it('renders with required props', () => {
    render(<Input id="test-input" label="Test Label" />);
    
    expect(screen.getByLabelText(/test label/i)).toBeInTheDocument();
    expect(screen.getByRole('textbox')).toHaveAttribute('id', 'test-input');
  });

  it('shows required indicator', () => {
    render(<Input id="test-input" label="Required Field" required />);
    
    expect(screen.getByText('*')).toBeInTheDocument();
    expect(screen.getByRole('textbox')).toHaveAttribute('required');
  });

  it('displays error state', () => {
    render(<Input id="test-input" label="Test" error="This field is required" />);
    
    expect(screen.getByText('This field is required')).toBeInTheDocument();
    expect(screen.getByRole('textbox')).toHaveClass('border-red-500');
  });

  it('displays success state', () => {
    render(<Input id="test-input" label="Test" success />);
    
    expect(screen.getByRole('textbox')).toHaveClass('border-emerald-500');
  });

  it('handles value changes', () => {
    const handleChange = jest.fn();
    render(<Input id="test-input" label="Test" onChange={handleChange} />);
    
    const input = screen.getByRole('textbox');
    fireEvent.change(input, { target: { value: 'new value' } });
    
    expect(handleChange).toHaveBeenCalledWith(
      expect.objectContaining({
        target: expect.objectContaining({ value: 'new value' })
      })
    );
  });

  it('displays help text', () => {
    render(<Input id="test-input" label="Test" helpText="This is help text" />);
    
    expect(screen.getByText('This is help text')).toBeInTheDocument();
  });

  it('renders with right addon', () => {
    render(<Input id="test-input" label="Amount" rightAddon="€" />);
    
    expect(screen.getByText('€')).toBeInTheDocument();
  });

  it('handles disabled state', () => {
    render(<Input id="test-input" label="Test" disabled />);
    
    const input = screen.getByRole('textbox');
    expect(input).toBeDisabled();
    expect(input).toHaveClass('bg-gray-50');
  });
});

describe('Textarea Component', () => {
  it('renders with required props', () => {
    render(<Textarea id="test-textarea" label="Description" />);
    
    expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
    expect(screen.getByRole('textbox')).toHaveAttribute('id', 'test-textarea');
  });

  it('handles value changes', () => {
    const handleChange = jest.fn();
    render(<Textarea id="test-textarea" label="Test" onChange={handleChange} />);
    
    const textarea = screen.getByRole('textbox');
    fireEvent.change(textarea, { target: { value: 'new content' } });
    
    expect(handleChange).toHaveBeenCalled();
  });

  it('displays error state', () => {
    render(<Textarea id="test-textarea" label="Test" error="Error message" />);
    
    expect(screen.getByText('Error message')).toBeInTheDocument();
    expect(screen.getByRole('textbox')).toHaveClass('border-red-500');
  });
});

describe('Select Component', () => {
  const options = [
    { value: '1', label: 'Option 1' },
    { value: '2', label: 'Option 2' },
    { value: '3', label: 'Option 3', disabled: true },
  ];

  it('renders with options', () => {
    render(<Select id="test-select" label="Choose Option" options={options} />);
    
    expect(screen.getByLabelText(/choose option/i)).toBeInTheDocument();
    expect(screen.getByRole('combobox')).toBeInTheDocument();
    
    // Check that options are rendered
    expect(screen.getByRole('option', { name: 'Option 1' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Option 2' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Option 3' })).toBeInTheDocument();
  });

  it('handles value changes', () => {
    const handleChange = jest.fn();
    render(<Select id="test-select" label="Test" options={options} onChange={handleChange} />);
    
    const select = screen.getByRole('combobox');
    fireEvent.change(select, { target: { value: '2' } });
    
    expect(handleChange).toHaveBeenCalled();
  });

  it('shows placeholder', () => {
    render(<Select id="test-select" label="Test" options={options} placeholder="Select an option" />);
    
    expect(screen.getByRole('option', { name: 'Select an option' })).toBeInTheDocument();
  });

  it('handles disabled options', () => {
    render(<Select id="test-select" label="Test" options={options} />);
    
    const disabledOption = screen.getByRole('option', { name: 'Option 3' });
    expect(disabledOption).toBeDisabled();
  });
});

describe('Table Component', () => {
  const columns = [
    { key: 'name', label: 'Name' },
    { key: 'email', label: 'Email' },
    { key: 'role', label: 'Role', render: (value: string) => <span className="badge">{value}</span> },
  ];

  const data = [
    { id: 1, name: 'John Doe', email: 'john@example.com', role: 'Admin' },
    { id: 2, name: 'Jane Smith', email: 'jane@example.com', role: 'User' },
  ];

  it('renders table with data', () => {
    render(<Table columns={columns} data={data} />);
    
    // Check headers
    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Email')).toBeInTheDocument();
    expect(screen.getByText('Role')).toBeInTheDocument();
    
    // Check data
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
  });

  it('renders custom cell content', () => {
    render(<Table columns={columns} data={data} />);
    
    // Check that custom render function is used
    expect(screen.getAllByText('Admin')[0]).toHaveClass('badge');
  });

  it('handles row clicks', () => {
    const handleRowClick = jest.fn();
    render(<Table columns={columns} data={data} onRowClick={handleRowClick} />);
    
    const firstRow = screen.getByText('John Doe').closest('tr');
    fireEvent.click(firstRow!);
    
    expect(handleRowClick).toHaveBeenCalledWith(data[0]);
  });

  it('shows loading state', () => {
    render(<Table columns={columns} data={[]} loading />);
    
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
    // Should show skeleton loading
    expect(document.querySelector('.animate-pulse')).toBeInTheDocument();
  });

  it('shows empty state', () => {
    render(<Table columns={columns} data={[]} emptyMessage="No data found" />);
    
    expect(screen.getByText('No data found')).toBeInTheDocument();
  });
});

describe('Card Component', () => {
  it('renders with children', () => {
    render(<Card>Card content</Card>);
    
    expect(screen.getByText('Card content')).toBeInTheDocument();
  });

  it('renders with title and subtitle', () => {
    render(
      <Card title="Card Title" subtitle="Card subtitle">
        Content
      </Card>
    );
    
    expect(screen.getByText('Card Title')).toBeInTheDocument();
    expect(screen.getByText('Card subtitle')).toBeInTheDocument();
  });

  it('renders with actions', () => {
    render(
      <Card actions={<button>Action</button>}>
        Content
      </Card>
    );
    
    expect(screen.getByRole('button', { name: 'Action' })).toBeInTheDocument();
  });

  it('applies different variants', () => {
    const { rerender } = render(<Card variant="elevated">Content</Card>);
    expect(screen.getByText('Content').parentElement).toHaveClass('shadow-lg');

    rerender(<Card variant="outlined">Content</Card>);
    expect(screen.getByText('Content').parentElement).toHaveClass('border-2');
  });
});

describe('LoadingState Component', () => {
  it('renders with default message', () => {
    render(<LoadingState />);
    
    expect(screen.getByText('Loading...')).toBeInTheDocument();
    expect(document.querySelector('.animate-spin')).toBeInTheDocument();
  });

  it('renders with custom message', () => {
    render(<LoadingState message="Loading data..." />);
    
    expect(screen.getByText('Loading data...')).toBeInTheDocument();
  });
});

describe('ErrorState Component', () => {
  it('renders with error message', () => {
    render(<ErrorState message="Something went wrong" />);
    
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
  });

  it('renders with custom title', () => {
    render(<ErrorState title="Custom Error" message="Error details" />);
    
    expect(screen.getByText('Custom Error')).toBeInTheDocument();
    expect(screen.getByText('Error details')).toBeInTheDocument();
  });

  it('handles retry action', () => {
    const handleRetry = jest.fn();
    render(<ErrorState message="Error" onRetry={handleRetry} retryLabel="Retry Now" />);
    
    const retryButton = screen.getByRole('button', { name: 'Retry Now' });
    fireEvent.click(retryButton);
    
    expect(handleRetry).toHaveBeenCalledTimes(1);
  });
});

describe('StatusBadge Component', () => {
  it('renders with different status styles', () => {
    const { rerender } = render(<StatusBadge status="paid">Paid</StatusBadge>);
    expect(screen.getByText('Paid')).toHaveClass('bg-emerald-50', 'text-emerald-700');

    rerender(<StatusBadge status="sent">Sent</StatusBadge>);
    expect(screen.getByText('Sent')).toHaveClass('bg-amber-50', 'text-amber-700');

    rerender(<StatusBadge status="overdue">Overdue</StatusBadge>);
    expect(screen.getByText('Overdue')).toHaveClass('bg-red-50', 'text-red-700');
  });
});

describe('EmptyState Component', () => {
  it('renders with title and description', () => {
    render(<EmptyState title="No data" description="Add some data to get started" />);
    
    expect(screen.getByText('No data')).toBeInTheDocument();
    expect(screen.getByText('Add some data to get started')).toBeInTheDocument();
  });

  it('renders with custom icon', () => {
    render(<EmptyState icon="🎉" title="Success" />);
    
    expect(screen.getByText('🎉')).toBeInTheDocument();
  });

  it('renders with action button', () => {
    render(
      <EmptyState 
        title="No data" 
        action={<button>Add Data</button>}
      />
    );
    
    expect(screen.getByRole('button', { name: 'Add Data' })).toBeInTheDocument();
  });
});

describe('Spinner Component', () => {
  it('renders with different sizes', () => {
    const { rerender } = render(<Spinner size="sm" />);
    expect(document.querySelector('.animate-spin')).toHaveClass('h-4 w-4');

    rerender(<Spinner size="lg" />);
    expect(document.querySelector('.animate-spin')).toHaveClass('h-12 w-12');
  });
});
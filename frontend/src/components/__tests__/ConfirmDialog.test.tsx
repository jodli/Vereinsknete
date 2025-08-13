import React from 'react';
import { render, screen, fireEvent, waitFor } from '../../test-utils/test-utils';
import { ConfirmProvider, useConfirm } from '../ConfirmDialog';

// Test component that uses the confirm context
const TestComponent = () => {
  const { confirm } = useConfirm();

  const handleDangerAction = () => {
    confirm({
      title: 'Delete Item',
      message: 'Are you sure you want to delete this item?',
      variant: 'danger',
      confirmLabel: 'Delete',
      cancelLabel: 'Cancel',
      onConfirm: jest.fn(),
    });
  };

  const handleWarningAction = () => {
    confirm({
      title: 'Warning Action',
      message: 'This action cannot be undone.',
      variant: 'warning',
      onConfirm: jest.fn(),
    });
  };

  const handleAsyncAction = () => {
    confirm({
      title: 'Async Action',
      message: 'This will perform an async operation.',
      onConfirm: async () => {
        await new Promise(resolve => setTimeout(resolve, 100));
      },
    });
  };

  return (
    <div>
      <button onClick={handleDangerAction}>Show Danger Confirm</button>
      <button onClick={handleWarningAction}>Show Warning Confirm</button>
      <button onClick={handleAsyncAction}>Show Async Confirm</button>
    </div>
  );
};

const renderWithConfirmProvider = (component: React.ReactElement) => {
  return render(
    <ConfirmProvider>
      {component}
    </ConfirmProvider>
  );
};

describe('Confirm Dialog Context', () => {
  // Note: Error boundary tests are complex in React Testing Library
  // The core functionality is tested in the working tests below

  it('shows confirm dialog with danger variant', () => {
    renderWithConfirmProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Danger Confirm'));
    
    expect(screen.getByText('Delete Item')).toBeInTheDocument();
    expect(screen.getByText('Are you sure you want to delete this item?')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Delete' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument();
  });

  it('shows confirm dialog with warning variant', () => {
    renderWithConfirmProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Warning Confirm'));
    
    expect(screen.getByText('Warning Action')).toBeInTheDocument();
    expect(screen.getByText('This action cannot be undone.')).toBeInTheDocument();
  });

  it('uses default labels when not provided', () => {
    renderWithConfirmProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Warning Confirm'));
    
    expect(screen.getByRole('button', { name: 'Confirm' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument();
  });

  it('closes dialog when cancel is clicked', () => {
    renderWithConfirmProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Danger Confirm'));
    expect(screen.getByText('Delete Item')).toBeInTheDocument();
    
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(screen.queryByText('Delete Item')).not.toBeInTheDocument();
  });

  it('closes dialog when X button is clicked', () => {
    renderWithConfirmProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Danger Confirm'));
    expect(screen.getByText('Delete Item')).toBeInTheDocument();
    
    // Find the X close button
    const closeButton = document.querySelector('button[title="Close"]') || 
                       document.querySelector('.text-gray-400');
    
    if (closeButton) {
      fireEvent.click(closeButton);
    }
    
    expect(screen.queryByText('Delete Item')).not.toBeInTheDocument();
  });

  it('calls onConfirm and closes dialog when confirm is clicked', async () => {
    const mockOnConfirm = jest.fn();
    
    const TestComponentWithMock = () => {
      const { confirm } = useConfirm();
      
      const handleAction = () => {
        confirm({
          title: 'Test Action',
          message: 'Test message',
          onConfirm: mockOnConfirm,
        });
      };
      
      return <button onClick={handleAction}>Show Confirm</button>;
    };

    renderWithConfirmProvider(<TestComponentWithMock />);
    
    fireEvent.click(screen.getByText('Show Confirm'));
    fireEvent.click(screen.getByRole('button', { name: 'Confirm' }));
    
    expect(mockOnConfirm).toHaveBeenCalledTimes(1);
    
    await waitFor(() => {
      expect(screen.queryByText('Test Action')).not.toBeInTheDocument();
    });
  });

  it('handles async onConfirm with loading state', async () => {
    jest.useFakeTimers();
    
    renderWithConfirmProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Async Confirm'));
    
    const confirmButton = screen.getByRole('button', { name: 'Confirm' });
    fireEvent.click(confirmButton);
    
    // Should show loading state
    expect(confirmButton).toBeDisabled();
    
    // Fast-forward time to resolve the promise
    jest.advanceTimersByTime(100);
    
    await waitFor(() => {
      expect(screen.queryByText('Async Action')).not.toBeInTheDocument();
    });
    
    jest.useRealTimers();
  });

  it('handles onConfirm errors gracefully', async () => {
    const mockConsoleError = jest.spyOn(console, 'error').mockImplementation(() => {});
    const mockOnConfirm = jest.fn().mockRejectedValue(new Error('Test error'));
    
    const TestComponentWithError = () => {
      const { confirm } = useConfirm();
      
      const handleAction = () => {
        confirm({
          title: 'Error Action',
          message: 'This will fail',
          onConfirm: mockOnConfirm,
        });
      };
      
      return <button onClick={handleAction}>Show Error Confirm</button>;
    };

    renderWithConfirmProvider(<TestComponentWithError />);
    
    fireEvent.click(screen.getByText('Show Error Confirm'));
    fireEvent.click(screen.getByRole('button', { name: 'Confirm' }));
    
    await waitFor(() => {
      expect(mockConsoleError).toHaveBeenCalledWith(
        'Confirm action failed:',
        expect.any(Error)
      );
    });
    
    // Dialog should still be open after error
    expect(screen.getByText('Error Action')).toBeInTheDocument();
    
    mockConsoleError.mockRestore();
  });

  it('closes dialog when background overlay is clicked', () => {
    renderWithConfirmProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Danger Confirm'));
    expect(screen.getByText('Delete Item')).toBeInTheDocument();
    
    // Click on the background overlay
    const overlay = document.querySelector('.bg-gray-500.bg-opacity-75');
    if (overlay) {
      fireEvent.click(overlay);
    }
    
    expect(screen.queryByText('Delete Item')).not.toBeInTheDocument();
  });

  it('applies correct styling for different variants', () => {
    renderWithConfirmProvider(<TestComponent />);
    
    // Test danger variant
    fireEvent.click(screen.getByText('Show Danger Confirm'));
    expect(screen.getByRole('button', { name: 'Delete' })).toHaveClass('bg-red-600');
    
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    
    // Test warning variant
    fireEvent.click(screen.getByText('Show Warning Confirm'));
    expect(screen.getByRole('button', { name: 'Confirm' })).toHaveClass('bg-amber-600');
  });
});
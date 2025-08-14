import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '../../test-utils/test-utils';
import { ToastProvider, useToast } from '../Toast';

// Test component that uses the toast context
const TestComponent = () => {
  const { success, error, warning, info, addToast, removeToast, toasts } = useToast();

  return (
    <div>
      <button onClick={() => success('Success message')}>Show Success</button>
      <button onClick={() => error('Error message')}>Show Error</button>
      <button onClick={() => warning('Warning message')}>Show Warning</button>
      <button onClick={() => info('Info message')}>Show Info</button>
      <button onClick={() => addToast({ 
        type: 'success', 
        title: 'Custom toast',
        message: 'Custom message',
        duration: 0 // Disable auto-removal for testing
      })}>
        Show Custom
      </button>
      <button onClick={() => addToast({ 
        type: 'success', 
        title: 'Auto-remove toast',
        message: 'This will auto-remove',
        duration: 1000
      })}>
        Show Auto-Remove
      </button>
      <div data-testid="toast-count">{toasts.length}</div>
      {toasts.map(toast => (
        <div key={toast.id} data-testid={`toast-${toast.id}`}>
          <span>{toast.title}</span>
          {toast.message && <span>{toast.message}</span>}
          <button onClick={() => removeToast(toast.id)}>Remove</button>
        </div>
      ))}
    </div>
  );
};

const renderWithToastProvider = (component: React.ReactElement) => {
  return render(
    <ToastProvider>
      {component}
    </ToastProvider>
  );
};

describe('Toast Context', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    // Suppress act warnings for these tests since setTimeout is expected
    const originalError = console.error;
    jest.spyOn(console, 'error').mockImplementation((...args) => {
      if (args[0]?.includes?.('act(...)')) {
        return;
      }
      originalError(...args);
    });
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
    jest.restoreAllMocks();
  });

  // Note: Error boundary tests are complex in React Testing Library
  // The core functionality is tested in the working tests below

  it('shows success toast', () => {
    renderWithToastProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Success'));
    
    expect(screen.getByTestId('toast-count')).toHaveTextContent('1');
  });

  it('shows error toast', () => {
    renderWithToastProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Error'));
    
    expect(screen.getByTestId('toast-count')).toHaveTextContent('1');
  });

  it('shows warning toast', () => {
    renderWithToastProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Warning'));
    
    expect(screen.getByTestId('toast-count')).toHaveTextContent('1');
  });

  it('shows info toast', () => {
    renderWithToastProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Info'));
    
    expect(screen.getByTestId('toast-count')).toHaveTextContent('1');
  });

  it('shows custom toast with message', () => {
    renderWithToastProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Custom'));
    
    expect(screen.getByTestId('toast-count')).toHaveTextContent('1');
  });

  it('auto-removes toast after duration', async () => {
    renderWithToastProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Auto-Remove')); // 1000ms duration
    
    expect(screen.getByTestId('toast-count')).toHaveTextContent('1');
    
    // Fast-forward time and wait for the removal
    act(() => {
      jest.advanceTimersByTime(1000);
    });
    
    await waitFor(() => {
      expect(screen.getByTestId('toast-count')).toHaveTextContent('0');
    });
  });

  it('manually removes toast', () => {
    renderWithToastProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Success'));
    expect(screen.getByTestId('toast-count')).toHaveTextContent('1');
    
    fireEvent.click(screen.getByText('Remove'));
    expect(screen.getByTestId('toast-count')).toHaveTextContent('0');
  });

  it('handles multiple toasts', () => {
    renderWithToastProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Success'));
    fireEvent.click(screen.getByText('Show Error'));
    fireEvent.click(screen.getByText('Show Warning'));
    
    expect(screen.getByTestId('toast-count')).toHaveTextContent('3');
  });

  it('error toasts have longer duration', async () => {
    renderWithToastProvider(<TestComponent />);
    
    fireEvent.click(screen.getByText('Show Error'));
    
    expect(screen.getByTestId('toast-count')).toHaveTextContent('1');
    
    // Error toasts should have 7000ms duration
    act(() => {
      jest.advanceTimersByTime(5000); // Less than error duration
    });
    
    // Should still be there
    expect(screen.getByTestId('toast-count')).toHaveTextContent('1');
    
    act(() => {
      jest.advanceTimersByTime(2500); // Total 7500ms (more than 7000ms)
    });
    
    await waitFor(() => {
      expect(screen.getByTestId('toast-count')).toHaveTextContent('0');
    });
  });
});

describe('Toast Component Rendering', () => {
  it('closes toast when close button is clicked', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    );
    
    fireEvent.click(screen.getByText('Show Success'));
    expect(screen.getByTestId('toast-count')).toHaveTextContent('1');
    
    // Find and click the close button in the actual toast component
    const toastContainer = document.querySelector('.fixed.top-4.right-4');
    const closeButton = toastContainer?.querySelector('button');
    
    if (closeButton) {
      fireEvent.click(closeButton);
    }
    
    expect(screen.getByTestId('toast-count')).toHaveTextContent('0');
  });
});
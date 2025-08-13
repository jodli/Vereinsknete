import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { Button, Input, Card, LoadingState, ErrorState } from '../UI';

describe('Basic UI Components', () => {
  describe('Button', () => {
    it('renders and handles clicks', () => {
      const handleClick = jest.fn();
      render(<Button onClick={handleClick}>Click me</Button>);
      
      const button = screen.getByRole('button', { name: /click me/i });
      expect(button).toBeInTheDocument();
      
      fireEvent.click(button);
      expect(handleClick).toHaveBeenCalledTimes(1);
    });

    it('shows loading state', () => {
      render(<Button loading>Loading</Button>);
      
      const button = screen.getByRole('button');
      expect(button).toBeDisabled();
      expect(button.querySelector('.animate-spin')).toBeInTheDocument();
    });
  });

  describe('Input', () => {
    it('renders with label', () => {
      render(<Input id="test" label="Test Input" />);
      
      expect(screen.getByLabelText(/test input/i)).toBeInTheDocument();
    });

    it('shows error state', () => {
      render(<Input id="test" label="Test" error="This is an error" />);
      
      expect(screen.getByText('This is an error')).toBeInTheDocument();
      expect(screen.getByRole('textbox')).toHaveClass('border-red-500');
    });
  });

  describe('Card', () => {
    it('renders with title and content', () => {
      render(
        <Card title="Test Card">
          <p>Card content</p>
        </Card>
      );
      
      expect(screen.getByText('Test Card')).toBeInTheDocument();
      expect(screen.getByText('Card content')).toBeInTheDocument();
    });
  });

  describe('LoadingState', () => {
    it('renders with default message', () => {
      render(<LoadingState />);
      
      expect(screen.getByText('Loading...')).toBeInTheDocument();
      expect(document.querySelector('.animate-spin')).toBeInTheDocument();
    });

    it('renders with custom message', () => {
      render(<LoadingState message="Custom loading message" />);
      
      expect(screen.getByText('Custom loading message')).toBeInTheDocument();
    });
  });

  describe('ErrorState', () => {
    it('renders error message', () => {
      render(<ErrorState message="Custom error message" />);
      
      // Check for the default title and custom message
      expect(screen.getByText('Something went wrong')).toBeInTheDocument(); // Default title
      expect(screen.getByText('Custom error message')).toBeInTheDocument(); // Custom message
    });

    it('handles retry action', () => {
      const handleRetry = jest.fn();
      render(
        <ErrorState 
          message="Error occurred" 
          onRetry={handleRetry}
          retryLabel="Try Again"
        />
      );
      
      const retryButton = screen.getByRole('button', { name: /try again/i });
      fireEvent.click(retryButton);
      
      expect(handleRetry).toHaveBeenCalledTimes(1);
    });
  });
});
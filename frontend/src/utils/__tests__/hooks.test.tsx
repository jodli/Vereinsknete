import React from 'react';
import { renderHook, act, waitFor } from '@testing-library/react';
import { useAsyncData, useFormState, useLocalStorage, useDebounce } from '../hooks';

describe('useAsyncData', () => {
  it('handles successful data fetching', async () => {
    const mockData = { id: 1, name: 'Test' };
    const mockFetch = jest.fn().mockResolvedValue(mockData);

    const { result } = renderHook(() => useAsyncData(mockFetch));

    // Initially loading
    expect(result.current.loading).toBe(true);
    expect(result.current.data).toBe(null);
    expect(result.current.error).toBe(null);

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.data).toEqual(mockData);
    expect(result.current.error).toBe(null);
    expect(mockFetch).toHaveBeenCalledTimes(1);
  });

  it('handles fetch errors', async () => {
    const mockError = new Error('Fetch failed');
    const mockFetch = jest.fn().mockRejectedValue(mockError);

    const { result } = renderHook(() => useAsyncData(mockFetch));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.data).toBe(null);
    expect(result.current.error).toBe('Fetch failed');
  });

  it('handles non-Error rejections', async () => {
    const mockFetch = jest.fn().mockRejectedValue('String error');

    const { result } = renderHook(() => useAsyncData(mockFetch));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe('Unknown error occurred');
  });

  it('refetches data when refetch is called', async () => {
    const mockData1 = { id: 1, name: 'First' };
    const mockData2 = { id: 2, name: 'Second' };
    const mockFetch = jest.fn()
      .mockResolvedValueOnce(mockData1)
      .mockResolvedValueOnce(mockData2);

    const { result } = renderHook(() => useAsyncData(mockFetch));

    await waitFor(() => {
      expect(result.current.data).toEqual(mockData1);
    });

    act(() => {
      result.current.refetch();
    });

    await waitFor(() => {
      expect(result.current.data).toEqual(mockData2);
    });

    expect(mockFetch).toHaveBeenCalledTimes(2);
  });

  it('refetches when dependencies change', async () => {
    const mockFetch = jest.fn().mockResolvedValue({ data: 'test' });
    let dependency = 'initial';

    const { result, rerender } = renderHook(() => 
      useAsyncData(mockFetch, [dependency])
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(mockFetch).toHaveBeenCalledTimes(1);

    // Change dependency
    dependency = 'changed';
    rerender();

    await waitFor(() => {
      expect(mockFetch).toHaveBeenCalledTimes(2);
    });
  });
});

describe('useFormState', () => {
  const initialState = {
    name: '',
    email: '',
    age: 0,
  };

  const validationRules = {
    name: (value: string) => value.length < 2 ? 'Name too short' : null,
    email: (value: string) => !value.includes('@') ? 'Invalid email' : null,
    age: (value: number) => value < 18 ? 'Must be 18 or older' : null,
  };

  it('initializes with correct state', () => {
    const { result } = renderHook(() => useFormState(initialState));

    expect(result.current.formData).toEqual(initialState);
    expect(result.current.errors).toEqual({});
    expect(result.current.touched).toEqual({});
  });

  it('updates field values', () => {
    const { result } = renderHook(() => useFormState(initialState));

    act(() => {
      result.current.setFieldValue('name', 'John');
    });

    expect(result.current.formData.name).toBe('John');
  });

  it('clears errors when field value changes', () => {
    const { result } = renderHook(() => useFormState(initialState, validationRules));

    // Set an error first
    act(() => {
      result.current.validateField('name');
    });

    expect(result.current.errors.name).toBe('Name too short');

    // Update field value should clear error
    act(() => {
      result.current.setFieldValue('name', 'John');
    });

    expect(result.current.errors.name).toBeUndefined();
  });

  it('sets field as touched', () => {
    const { result } = renderHook(() => useFormState(initialState));

    act(() => {
      result.current.setFieldTouched('name');
    });

    expect(result.current.touched.name).toBe(true);
  });

  it('validates individual fields', () => {
    const { result } = renderHook(() => useFormState(initialState, validationRules));

    act(() => {
      const error = result.current.validateField('name');
      expect(error).toBe('Name too short');
    });

    expect(result.current.errors.name).toBe('Name too short');
  });

  it('validates entire form', () => {
    const { result } = renderHook(() => useFormState(initialState, validationRules));

    act(() => {
      const isValid = result.current.validateForm();
      expect(isValid).toBe(false);
    });

    expect(result.current.errors.name).toBe('Name too short');
    expect(result.current.errors.email).toBe('Invalid email');
    expect(result.current.errors.age).toBe('Must be 18 or older');
  });

  it('returns true for valid form', () => {
    const validData = {
      name: 'John Doe',
      email: 'john@example.com',
      age: 25,
    };

    const { result } = renderHook(() => useFormState(validData, validationRules));

    act(() => {
      const isValid = result.current.validateForm();
      expect(isValid).toBe(true);
    });

    expect(result.current.errors).toEqual({});
  });

  it('resets form to initial state', () => {
    const { result } = renderHook(() => useFormState(initialState));

    act(() => {
      result.current.setFieldValue('name', 'John');
      result.current.setFieldTouched('name');
    });

    expect(result.current.formData.name).toBe('John');
    expect(result.current.touched.name).toBe(true);

    act(() => {
      result.current.resetForm();
    });

    expect(result.current.formData).toEqual(initialState);
    expect(result.current.touched).toEqual({});
    expect(result.current.errors).toEqual({});
  });

  it('works without validation rules', () => {
    const { result } = renderHook(() => useFormState(initialState));

    act(() => {
      result.current.setFieldValue('name', 'John');
      const isValid = result.current.validateForm();
      expect(isValid).toBe(true);
    });

    expect(result.current.errors).toEqual({});
  });
});

describe('useLocalStorage', () => {
  beforeEach(() => {
    localStorage.clear();
    // Clear any existing spies
    jest.restoreAllMocks();
  });

  it('initializes with default value when localStorage is empty', () => {
    const { result } = renderHook(() => useLocalStorage('test-key', 'default'));

    expect(result.current[0]).toBe('default');
  });

  it('initializes with stored value when localStorage has data', () => {
    localStorage.setItem('test-key', JSON.stringify('stored-value'));

    const { result } = renderHook(() => useLocalStorage('test-key', 'default'));

    expect(result.current[0]).toBe('stored-value');
  });

  it('updates localStorage when value changes', () => {
    // Create a spy on the actual localStorage method
    const setItemSpy = jest.spyOn(Storage.prototype, 'setItem');
    
    const { result } = renderHook(() => useLocalStorage('test-key', 'initial'));

    act(() => {
      result.current[1]('updated');
    });

    expect(result.current[0]).toBe('updated');
    expect(setItemSpy).toHaveBeenCalledWith('test-key', '"updated"');
    
    setItemSpy.mockRestore();
  });

  it('handles function updates', () => {
    const { result } = renderHook(() => useLocalStorage('test-key', 0));

    act(() => {
      result.current[1](prev => prev + 1);
    });

    expect(result.current[0]).toBe(1);
  });



  it('handles complex objects', () => {
    const complexObject = { name: 'John', age: 30, hobbies: ['reading', 'coding'] };

    const { result } = renderHook(() => useLocalStorage('complex-key', complexObject));

    expect(result.current[0]).toEqual(complexObject);

    const updatedObject = { ...complexObject, age: 31 };

    act(() => {
      result.current[1](updatedObject);
    });

    expect(result.current[0]).toEqual(updatedObject);
  });
});

describe('useDebounce', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('returns initial value immediately', () => {
    const { result } = renderHook(() => useDebounce('initial', 500));

    expect(result.current).toBe('initial');
  });

  it('debounces value changes', () => {
    const { result, rerender } = renderHook(
      ({ value, delay }) => useDebounce(value, delay),
      { initialProps: { value: 'initial', delay: 500 } }
    );

    expect(result.current).toBe('initial');

    // Change value
    rerender({ value: 'updated', delay: 500 });

    // Value should not change immediately
    expect(result.current).toBe('initial');

    // Fast-forward time
    act(() => {
      jest.advanceTimersByTime(500);
    });

    // Now value should be updated
    expect(result.current).toBe('updated');
  });

  it('cancels previous timeout when value changes quickly', () => {
    const { result, rerender } = renderHook(
      ({ value, delay }) => useDebounce(value, delay),
      { initialProps: { value: 'initial', delay: 500 } }
    );

    // Change value multiple times quickly
    rerender({ value: 'first', delay: 500 });
    rerender({ value: 'second', delay: 500 });
    rerender({ value: 'final', delay: 500 });

    // Fast-forward time
    act(() => {
      jest.advanceTimersByTime(500);
    });

    // Should only have the final value
    expect(result.current).toBe('final');
  });

  it('handles delay changes', () => {
    const { result, rerender } = renderHook(
      ({ value, delay }) => useDebounce(value, delay),
      { initialProps: { value: 'initial', delay: 500 } }
    );

    rerender({ value: 'updated', delay: 1000 });

    // Fast-forward by original delay
    act(() => {
      jest.advanceTimersByTime(500);
    });

    // Should still be initial value
    expect(result.current).toBe('initial');

    // Fast-forward by remaining time
    act(() => {
      jest.advanceTimersByTime(500);
    });

    // Now should be updated
    expect(result.current).toBe('updated');
  });

  it('cleans up timeout on unmount', () => {
    const { unmount } = renderHook(() => useDebounce('test', 500));

    // Should not throw any errors
    expect(() => unmount()).not.toThrow();
  });
});
import { useState, useEffect, useCallback } from 'react';

// Custom hook for data fetching with loading, error, and refetch functionality
export const useAsyncData = <T>(
    fetchFunction: () => Promise<T>,
    dependencies: any[] = []
) => {
    const [data, setData] = useState<T | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchData = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            const result = await fetchFunction();
            setData(result);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Unknown error occurred');
        } finally {
            setLoading(false);
        }
    }, [fetchFunction]);

    // Serialize dependencies to trigger re-fetch when their values (not just identity) change without using a spread in deps array.
    const depsKey = JSON.stringify(dependencies);

    useEffect(() => {
        fetchData();
        // eslint-disable-next-line react-hooks/exhaustive-deps -- depsKey captures external dependencies changes
    }, [fetchData, depsKey]);

    const refetch = useCallback(() => {
        fetchData();
    }, [fetchData]);

    return { data, loading, error, refetch };
};

// Custom hook for form state management
export const useFormState = <T extends Record<string, any>>(
    initialState: T,
    validationRules?: Partial<Record<keyof T, (value: any) => string | null>>
) => {
    const [formData, setFormData] = useState<T>(initialState);
    const [errors, setErrors] = useState<Partial<Record<keyof T, string>>>({});
    const [touched, setTouched] = useState<Partial<Record<keyof T, boolean>>>({});

    const setFieldValue = useCallback((field: keyof T, value: any) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        
        // Clear error when user starts typing
        if (errors[field]) {
            setErrors(prev => ({ ...prev, [field]: undefined }));
        }
    }, [errors]);

    const setFieldTouched = useCallback((field: keyof T, isTouched: boolean = true) => {
        setTouched(prev => ({ ...prev, [field]: isTouched }));
    }, []);

    const validateField = useCallback((field: keyof T) => {
        if (!validationRules?.[field]) return null;
        
        const error = validationRules[field]!(formData[field]);
        setErrors(prev => ({ ...prev, [field]: error || undefined }));
        return error;
    }, [formData, validationRules]);

    const validateForm = useCallback(() => {
        if (!validationRules) return true;
        
        const newErrors: Partial<Record<keyof T, string>> = {};
        let isValid = true;
        const newTouched: Partial<Record<keyof T, boolean>> = {};

        Object.keys(validationRules).forEach(key => {
            const field = key as keyof T;
            const error = validationRules[field]!(formData[field]);
            if (error) {
                newErrors[field] = error;
                isValid = false;
            }
            // Mark every validated field as touched so UI shows errors after submit
            newTouched[field] = true;
        });

        setErrors(newErrors);
        if (Object.keys(newTouched).length) {
            setTouched(prev => ({ ...prev, ...newTouched }));
        }
        return isValid;
    }, [formData, validationRules]);

    const resetForm = useCallback(() => {
        setFormData(initialState);
        setErrors({});
        setTouched({});
    }, [initialState]);

    return {
        formData,
        errors,
        touched,
        setFieldValue,
        setFieldTouched,
        validateField,
        validateForm,
        resetForm,
    };
};

// Custom hook for local storage
export const useLocalStorage = <T>(key: string, initialValue: T) => {
    const [storedValue, setStoredValue] = useState<T>(() => {
        try {
            const item = window.localStorage.getItem(key);
            return item ? JSON.parse(item) : initialValue;
        } catch (error) {
            console.error(`Error reading localStorage key "${key}":`, error);
            return initialValue;
        }
    });

    const setValue = useCallback((value: T | ((val: T) => T)) => {
        try {
            const valueToStore = value instanceof Function ? value(storedValue) : value;
            setStoredValue(valueToStore);
            window.localStorage.setItem(key, JSON.stringify(valueToStore));
        } catch (error) {
            console.error(`Error setting localStorage key "${key}":`, error);
        }
    }, [key, storedValue]);

    return [storedValue, setValue] as const;
};

// Custom hook for debounced values
export const useDebounce = <T>(value: T, delay: number) => {
    const [debouncedValue, setDebouncedValue] = useState<T>(value);

    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedValue(value);
        }, delay);

        return () => {
            clearTimeout(handler);
        };
    }, [value, delay]);

    return debouncedValue;
};
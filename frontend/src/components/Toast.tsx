import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { CheckCircleIcon, XCircleIcon, ExclamationTriangleIcon, InformationCircleIcon, XMarkIcon } from '@heroicons/react/24/outline';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
    id: string;
    type: ToastType;
    title: string;
    message?: string;
    duration?: number;
    action?: {
        label: string;
        onClick: () => void;
    };
}

interface ToastContextType {
    toasts: Toast[];
    addToast: (toast: Omit<Toast, 'id'>) => void;
    removeToast: (id: string) => void;
    success: (title: string, message?: string) => void;
    error: (title: string, message?: string) => void;
    warning: (title: string, message?: string) => void;
    info: (title: string, message?: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const useToast = () => {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error('useToast must be used within a ToastProvider');
    }
    return context;
};

interface ToastProviderProps {
    children: ReactNode;
}

export const ToastProvider: React.FC<ToastProviderProps> = ({ children }) => {
    const [toasts, setToasts] = useState<Toast[]>([]);

    const removeToast = useCallback((id: string) => {
        setToasts(prev => prev.filter(toast => toast.id !== id));
    }, []);

    const addToast = useCallback((toast: Omit<Toast, 'id'>) => {
        const id = Math.random().toString(36).substr(2, 9);
        const newToast: Toast = {
            ...toast,
            id,
            duration: toast.duration ?? 5000,
        };

        setToasts(prev => [...prev, newToast]);

        // Auto remove toast after duration
        if (newToast.duration && newToast.duration > 0) {
            setTimeout(() => {
                removeToast(id);
            }, newToast.duration);
        }
    }, [removeToast]);

    const success = useCallback((title: string, message?: string) => {
        addToast({ type: 'success', title, message });
    }, [addToast]);

    const error = useCallback((title: string, message?: string) => {
        addToast({ type: 'error', title, message, duration: 7000 });
    }, [addToast]);

    const warning = useCallback((title: string, message?: string) => {
        addToast({ type: 'warning', title, message });
    }, [addToast]);

    const info = useCallback((title: string, message?: string) => {
        addToast({ type: 'info', title, message });
    }, [addToast]);

    const value = {
        toasts,
        addToast,
        removeToast,
        success,
        error,
        warning,
        info,
    };

    return (
        <ToastContext.Provider value={value}>
            {children}
            <ToastContainer />
        </ToastContext.Provider>
    );
};

const ToastContainer: React.FC = () => {
    const { toasts, removeToast } = useToast();

    return (
        <div className="fixed top-4 right-4 z-50 space-y-2 max-w-sm">
            {toasts.map(toast => (
                <ToastItem key={toast.id} toast={toast} onRemove={removeToast} />
            ))}
        </div>
    );
};

interface ToastItemProps {
    toast: Toast;
    onRemove: (id: string) => void;
}

const ToastItem: React.FC<ToastItemProps> = ({ toast, onRemove }) => {
    const getToastStyles = (type: ToastType) => {
        switch (type) {
            case 'success':
                return {
                    container: 'bg-emerald-50 border-emerald-200 text-emerald-800',
                    icon: <CheckCircleIcon className="w-5 h-5 text-emerald-600" />,
                };
            case 'error':
                return {
                    container: 'bg-red-50 border-red-200 text-red-800',
                    icon: <XCircleIcon className="w-5 h-5 text-red-600" />,
                };
            case 'warning':
                return {
                    container: 'bg-amber-50 border-amber-200 text-amber-800',
                    icon: <ExclamationTriangleIcon className="w-5 h-5 text-amber-600" />,
                };
            case 'info':
                return {
                    container: 'bg-sky-50 border-sky-200 text-sky-800',
                    icon: <InformationCircleIcon className="w-5 h-5 text-sky-600" />,
                };
        }
    };

    const styles = getToastStyles(toast.type);

    return (
        <div className={`
            ${styles.container} 
            border rounded-lg p-4 shadow-lg animate-slide-up
            transition-all duration-300 ease-in-out
        `}>
            <div className="flex items-start">
                <div className="flex-shrink-0">
                    {styles.icon}
                </div>
                <div className="ml-3 flex-1">
                    <h4 className="text-sm font-medium">{toast.title}</h4>
                    {toast.message && (
                        <p className="text-sm mt-1 opacity-90">{toast.message}</p>
                    )}
                    {toast.action && (
                        <div className="mt-2">
                            <button
                                onClick={toast.action.onClick}
                                className="text-sm font-medium underline hover:no-underline transition-all"
                            >
                                {toast.action.label}
                            </button>
                        </div>
                    )}
                </div>
                <div className="ml-4 flex-shrink-0">
                    <button
                        onClick={() => onRemove(toast.id)}
                        className="inline-flex text-gray-400 hover:text-gray-600 transition-colors"
                    >
                        <XMarkIcon className="w-4 h-4" />
                    </button>
                </div>
            </div>
        </div>
    );
};
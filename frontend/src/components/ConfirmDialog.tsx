import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { ExclamationTriangleIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { Button } from './UI';

interface ConfirmOptions {
    title: string;
    message: string;
    confirmLabel?: string;
    cancelLabel?: string;
    variant?: 'danger' | 'warning' | 'info';
    onConfirm: () => void | Promise<void>;
}

interface ConfirmContextType {
    confirm: (options: ConfirmOptions) => void;
}

const ConfirmContext = createContext<ConfirmContextType | undefined>(undefined);

export const useConfirm = () => {
    const context = useContext(ConfirmContext);
    if (!context) {
        throw new Error('useConfirm must be used within a ConfirmProvider');
    }
    return context;
};

interface ConfirmProviderProps {
    children: ReactNode;
}

export const ConfirmProvider: React.FC<ConfirmProviderProps> = ({ children }) => {
    const [confirmState, setConfirmState] = useState<{
        isOpen: boolean;
        options: ConfirmOptions | null;
        isLoading: boolean;
    }>({
        isOpen: false,
        options: null,
        isLoading: false,
    });

    const confirm = useCallback((options: ConfirmOptions) => {
        setConfirmState({
            isOpen: true,
            options,
            isLoading: false,
        });
    }, []);

    const handleConfirm = async () => {
        if (!confirmState.options) return;

        setConfirmState(prev => ({ ...prev, isLoading: true }));

        try {
            await confirmState.options.onConfirm();
            setConfirmState({ isOpen: false, options: null, isLoading: false });
        } catch (error) {
            console.error('Confirm action failed:', error);
            setConfirmState(prev => ({ ...prev, isLoading: false }));
        }
    };

    const handleCancel = () => {
        setConfirmState({ isOpen: false, options: null, isLoading: false });
    };

    const value = { confirm };

    return (
        <ConfirmContext.Provider value={value}>
            {children}
            {confirmState.isOpen && confirmState.options && (
                <ConfirmDialog
                    options={confirmState.options}
                    isLoading={confirmState.isLoading}
                    onConfirm={handleConfirm}
                    onCancel={handleCancel}
                />
            )}
        </ConfirmContext.Provider>
    );
};

interface ConfirmDialogProps {
    options: ConfirmOptions;
    isLoading: boolean;
    onConfirm: () => void;
    onCancel: () => void;
}

const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
    options,
    isLoading,
    onConfirm,
    onCancel,
}) => {
    const getVariantStyles = (variant: string) => {
        switch (variant) {
            case 'danger':
                return {
                    icon: 'text-red-600',
                    iconBg: 'bg-red-100',
                    confirmButton: 'danger' as const,
                };
            case 'warning':
                return {
                    icon: 'text-amber-600',
                    iconBg: 'bg-amber-100',
                    confirmButton: 'warning' as const,
                };
            default:
                return {
                    icon: 'text-blue-600',
                    iconBg: 'bg-blue-100',
                    confirmButton: 'primary' as const,
                };
        }
    };

    const styles = getVariantStyles(options.variant || 'info');

    return (
        <div className="fixed inset-0 z-50 overflow-y-auto">
            <div className="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
                {/* Background overlay */}
                <div 
                    className="fixed inset-0 transition-opacity bg-gray-500 bg-opacity-75"
                    onClick={onCancel}
                />

                {/* Dialog */}
                <div className="inline-block w-full max-w-md p-6 my-8 overflow-hidden text-left align-middle transition-all transform bg-white shadow-xl rounded-2xl">
                    <div className="flex items-start">
                        <div className={`flex-shrink-0 w-10 h-10 rounded-full ${styles.iconBg} flex items-center justify-center`}>
                            <ExclamationTriangleIcon className={`w-6 h-6 ${styles.icon}`} />
                        </div>
                        
                        <div className="ml-4 flex-1">
                            <h3 className="text-lg font-medium text-gray-900 mb-2">
                                {options.title}
                            </h3>
                            <p className="text-sm text-gray-600 mb-6">
                                {options.message}
                            </p>
                            
                            <div className="flex flex-col sm:flex-row sm:justify-end gap-3">
                                <Button
                                    variant="secondary"
                                    onClick={onCancel}
                                    disabled={isLoading}
                                >
                                    {options.cancelLabel || 'Cancel'}
                                </Button>
                                <Button
                                    variant={styles.confirmButton}
                                    onClick={onConfirm}
                                    loading={isLoading}
                                    disabled={isLoading}
                                >
                                    {options.confirmLabel || 'Confirm'}
                                </Button>
                            </div>
                        </div>
                        
                        <button
                            onClick={onCancel}
                            disabled={isLoading}
                            className="ml-2 text-gray-400 hover:text-gray-600 transition-colors disabled:opacity-50"
                        >
                            <XMarkIcon className="w-5 h-5" />
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};
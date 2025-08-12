import React from 'react';
import { ExclamationCircleIcon, CheckCircleIcon } from '@heroicons/react/24/outline';

interface ButtonProps {
    children: React.ReactNode;
    type?: 'button' | 'submit' | 'reset';
    variant?: 'primary' | 'secondary' | 'danger' | 'success' | 'warning';
    size?: 'sm' | 'md' | 'lg';
    onClick?: () => void;
    disabled?: boolean;
    loading?: boolean;
    className?: string;
}

export const Button: React.FC<ButtonProps> = ({
    children,
    type = 'button',
    variant = 'primary',
    size = 'md',
    onClick,
    disabled = false,
    loading = false,
    className = '',
}) => {
    const baseStyle = 'inline-flex items-center justify-center font-medium rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed';

    const sizeStyles = {
        sm: 'px-3 py-1.5 text-sm',
        md: 'px-4 py-2 text-sm',
        lg: 'px-6 py-3 text-base',
    };

    const variantStyles = {
        primary: 'bg-blue-600 hover:bg-blue-700 text-white focus:ring-blue-500 shadow-sm hover:shadow-md transform hover:scale-105',
        secondary: 'bg-white hover:bg-gray-50 text-gray-700 border border-gray-300 focus:ring-blue-500 shadow-sm hover:shadow-md',
        danger: 'bg-red-600 hover:bg-red-700 text-white focus:ring-red-500 shadow-sm hover:shadow-md transform hover:scale-105',
        success: 'bg-emerald-600 hover:bg-emerald-700 text-white focus:ring-emerald-500 shadow-sm hover:shadow-md transform hover:scale-105',
        warning: 'bg-amber-600 hover:bg-amber-700 text-white focus:ring-amber-500 shadow-sm hover:shadow-md transform hover:scale-105',
    };

    return (
        <button
            type={type}
            onClick={onClick}
            disabled={disabled || loading}
            className={`${baseStyle} ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}
        >
            {loading && (
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current mr-2"></div>
            )}
            {children}
        </button>
    );
};

interface InputProps {
    id: string;
    name?: string;
    label: string;
    type?: string;
    value?: string | number;
    onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onBlur?: (e: React.FocusEvent<HTMLInputElement>) => void;
    placeholder?: string;
    required?: boolean;
    disabled?: boolean;
    error?: string;
    success?: boolean;
    helpText?: string;
    rightAddon?: string;
    leftIcon?: React.ReactNode;
    className?: string;
    min?: number;
    step?: number;
}

export const Input: React.FC<InputProps> = ({
    id,
    name,
    label,
    type = 'text',
    value,
    onChange,
    onBlur,
    placeholder,
    required = false,
    disabled = false,
    error,
    success = false,
    helpText,
    rightAddon,
    leftIcon,
    className = '',
    min,
    step,
}) => {
    const inputClasses = `
        w-full px-3 py-2 border rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-1
        ${leftIcon ? 'pl-10' : ''}
        ${rightAddon ? 'pr-12' : ''}
        ${error 
            ? 'border-red-500 focus:ring-red-500 focus:border-red-500' 
            : success 
                ? 'border-emerald-500 focus:ring-emerald-500 focus:border-emerald-500'
                : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500'
        }
        ${disabled ? 'bg-gray-50 text-gray-500 cursor-not-allowed' : 'bg-white'}
    `;

    return (
        <div className={`space-y-1 ${className}`}>
            <label htmlFor={id} className="block text-sm font-medium text-gray-700">
                {label}
                {required && <span className="text-red-500 ml-1">*</span>}
            </label>
            
            <div className="relative">
                {leftIcon && (
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <div className="h-5 w-5 text-gray-400">
                            {leftIcon}
                        </div>
                    </div>
                )}
                
                <input
                    id={id}
                    name={name || id}
                    type={type}
                    value={value}
                    onChange={onChange}
                    onBlur={onBlur}
                    placeholder={placeholder}
                    required={required}
                    disabled={disabled}
                    min={min}
                    step={step}
                    className={inputClasses}
                />
                
                {rightAddon && (
                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                        <span className="text-gray-500 text-sm">{rightAddon}</span>
                    </div>
                )}
                
                {error && (
                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                        <ExclamationCircleIcon className="h-5 w-5 text-red-500" />
                    </div>
                )}
                
                {success && !error && (
                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                        <CheckCircleIcon className="h-5 w-5 text-emerald-500" />
                    </div>
                )}
            </div>
            
            {error && (
                <p className="text-sm text-red-600 flex items-center">
                    <ExclamationCircleIcon className="w-4 h-4 mr-1" />
                    {error}
                </p>
            )}
            
            {helpText && !error && (
                <p className="text-sm text-gray-500">{helpText}</p>
            )}
        </div>
    );
};

interface TextareaProps {
    id: string;
    name?: string;
    label: string;
    value?: string;
    onChange?: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
    onBlur?: (e: React.FocusEvent<HTMLTextAreaElement>) => void;
    placeholder?: string;
    required?: boolean;
    disabled?: boolean;
    error?: string;
    helpText?: string;
    rows?: number;
    className?: string;
}

export const Textarea: React.FC<TextareaProps> = ({
    id,
    name,
    label,
    value,
    onChange,
    onBlur,
    placeholder,
    required = false,
    disabled = false,
    error,
    helpText,
    rows = 3,
    className = '',
}) => {
    const textareaClasses = `
        w-full px-3 py-2 border rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-1 resize-vertical
        ${error 
            ? 'border-red-500 focus:ring-red-500 focus:border-red-500' 
            : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500'
        }
        ${disabled ? 'bg-gray-50 text-gray-500 cursor-not-allowed' : 'bg-white'}
    `;

    return (
        <div className={`space-y-1 ${className}`}>
            <label htmlFor={id} className="block text-sm font-medium text-gray-700">
                {label}
                {required && <span className="text-red-500 ml-1">*</span>}
            </label>
            
            <textarea
                id={id}
                name={name || id}
                value={value}
                onChange={onChange}
                onBlur={onBlur}
                placeholder={placeholder}
                required={required}
                disabled={disabled}
                rows={rows}
                className={textareaClasses}
            />
            
            {error && (
                <p className="text-sm text-red-600 flex items-center">
                    <ExclamationCircleIcon className="w-4 h-4 mr-1" />
                    {error}
                </p>
            )}
            
            {helpText && !error && (
                <p className="text-sm text-gray-500">{helpText}</p>
            )}
        </div>
    );
};

interface SelectProps {
    id: string;
    name?: string;
    label: string;
    value?: string | number;
    onChange?: (e: React.ChangeEvent<HTMLSelectElement>) => void;
    options: { value: string | number; label: string; disabled?: boolean }[];
    placeholder?: string;
    required?: boolean;
    disabled?: boolean;
    error?: string;
    helpText?: string;
    className?: string;
}

export const Select: React.FC<SelectProps> = ({
    id,
    name,
    label,
    value,
    onChange,
    options,
    placeholder,
    required = false,
    disabled = false,
    error,
    helpText,
    className = '',
}) => {
    const selectClasses = `
        w-full px-3 py-2 border rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-1 bg-white
        ${error 
            ? 'border-red-500 focus:ring-red-500 focus:border-red-500' 
            : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500'
        }
        ${disabled ? 'bg-gray-50 text-gray-500 cursor-not-allowed' : ''}
    `;

    return (
        <div className={`space-y-1 ${className}`}>
            <label htmlFor={id} className="block text-sm font-medium text-gray-700">
                {label}
                {required && <span className="text-red-500 ml-1">*</span>}
            </label>
            
            <select
                id={id}
                name={name || id}
                value={value}
                onChange={onChange}
                required={required}
                disabled={disabled}
                className={selectClasses}
            >
                {placeholder && (
                    <option value="" disabled>
                        {placeholder}
                    </option>
                )}
                {options.map((option) => (
                    <option 
                        key={option.value} 
                        value={option.value}
                        disabled={option.disabled}
                    >
                        {option.label}
                    </option>
                ))}
            </select>
            
            {error && (
                <p className="text-sm text-red-600 flex items-center">
                    <ExclamationCircleIcon className="w-4 h-4 mr-1" />
                    {error}
                </p>
            )}
            
            {helpText && !error && (
                <p className="text-sm text-gray-500">{helpText}</p>
            )}
        </div>
    );
};

interface CardProps {
    title?: string;
    subtitle?: string;
    children: React.ReactNode;
    actions?: React.ReactNode;
    variant?: 'default' | 'elevated' | 'outlined';
    padding?: 'none' | 'sm' | 'md' | 'lg';
    className?: string;
}

export const Card: React.FC<CardProps> = ({ 
    title, 
    subtitle,
    children, 
    actions,
    variant = 'default',
    padding = 'md',
    className = '' 
}) => {
    const variantStyles = {
        default: 'bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow border border-gray-100',
        elevated: 'bg-white rounded-xl shadow-lg hover:shadow-xl transition-shadow',
        outlined: 'bg-white rounded-xl border-2 border-gray-200 hover:border-gray-300 transition-colors',
    };

    const paddingStyles = {
        none: '',
        sm: 'p-4',
        md: 'p-6',
        lg: 'p-8',
    };

    return (
        <div className={`${variantStyles[variant]} ${paddingStyles[padding]} ${className}`}>
            {(title || subtitle || actions) && (
                <div className="flex items-start justify-between mb-6">
                    <div>
                        {title && (
                            <h2 className="text-xl font-semibold text-gray-900">{title}</h2>
                        )}
                        {subtitle && (
                            <p className="text-sm text-gray-600 mt-1">{subtitle}</p>
                        )}
                    </div>
                    {actions && (
                        <div className="flex items-center space-x-2">
                            {actions}
                        </div>
                    )}
                </div>
            )}
            {children}
        </div>
    );
};

interface TableColumn {
    key: string;
    label: string;
    sortable?: boolean;
    render?: (value: any, row: any) => React.ReactNode;
    className?: string;
}

interface TableProps {
    columns: TableColumn[];
    data: any[];
    onRowClick?: (row: any) => void;
    loading?: boolean;
    emptyMessage?: string;
    className?: string;
}

export const Table: React.FC<TableProps> = ({ 
    columns, 
    data, 
    onRowClick, 
    loading = false,
    emptyMessage = 'No data available',
    className = '' 
}) => {
    if (loading) {
        return (
            <div className={`${className}`}>
                <div className="animate-pulse">
                    <div className="bg-gray-200 rounded-lg h-12 mb-4"></div>
                    {[...Array(5)].map((_, i) => (
                        <div key={i} className="bg-gray-100 rounded h-16 mb-2"></div>
                    ))}
                </div>
            </div>
        );
    }

    return (
        <div className={`overflow-hidden ${className}`}>
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            {columns.map((column) => (
                                <th 
                                    key={column.key} 
                                    className={`px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider ${column.className || ''}`}
                                >
                                    {column.label}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {data.map((row, index) => (
                            <tr
                                key={row.id || index}
                                onClick={() => onRowClick && onRowClick(row)}
                                className={`
                                    transition-colors duration-200
                                    ${onRowClick 
                                        ? 'hover:bg-gray-50 cursor-pointer hover:shadow-sm' 
                                        : ''
                                    }
                                `}
                            >
                                {columns.map((column) => (
                                    <td 
                                        key={`${index}-${column.key}`} 
                                        className={`px-6 py-4 whitespace-nowrap text-sm ${column.className || ''}`}
                                    >
                                        {column.render 
                                            ? column.render(row[column.key], row)
                                            : row[column.key]
                                        }
                                    </td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
            
            {data.length === 0 && (
                <div className="text-center py-12">
                    <div className="text-gray-400 text-lg mb-2">📋</div>
                    <p className="text-gray-500 text-sm">{emptyMessage}</p>
                </div>
            )}
        </div>
    );
};

// Loading Spinner Component
interface SpinnerProps {
    size?: 'sm' | 'md' | 'lg';
    className?: string;
}

export const Spinner: React.FC<SpinnerProps> = ({ size = 'md', className = '' }) => {
    const sizeStyles = {
        sm: 'h-4 w-4',
        md: 'h-8 w-8',
        lg: 'h-12 w-12',
    };

    return (
        <div className={`animate-spin rounded-full border-b-2 border-blue-600 ${sizeStyles[size]} ${className}`}></div>
    );
};

// Loading State Component
interface LoadingStateProps {
    message?: string;
    className?: string;
}

export const LoadingState: React.FC<LoadingStateProps> = ({ 
    message = 'Loading...', 
    className = '' 
}) => {
    return (
        <div className={`flex items-center justify-center min-h-64 ${className}`}>
            <div className="text-center">
                <Spinner size="lg" className="mx-auto mb-4" />
                <p className="text-lg text-gray-600">{message}</p>
            </div>
        </div>
    );
};

// Error State Component
interface ErrorStateProps {
    title?: string;
    message: string;
    onRetry?: () => void;
    retryLabel?: string;
    className?: string;
}

export const ErrorState: React.FC<ErrorStateProps> = ({ 
    title = 'Something went wrong',
    message, 
    onRetry,
    retryLabel = 'Try Again',
    className = '' 
}) => {
    return (
        <div className={`bg-red-50 border border-red-200 rounded-lg p-6 ${className}`}>
            <div className="flex items-center">
                <ExclamationCircleIcon className="w-6 h-6 text-red-600 mr-3" />
                <div className="flex-1">
                    <h3 className="text-lg font-medium text-red-800">{title}</h3>
                    <p className="text-red-700 mt-1">{message}</p>
                </div>
            </div>
            {onRetry && (
                <div className="mt-4">
                    <Button variant="danger" size="sm" onClick={onRetry}>
                        {retryLabel}
                    </Button>
                </div>
            )}
        </div>
    );
};

// Status Badge Component
interface StatusBadgeProps {
    status: 'paid' | 'sent' | 'created' | 'overdue' | 'draft' | string;
    children: React.ReactNode;
    className?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, children, className = '' }) => {
    const getStatusStyles = (status: string) => {
        switch (status.toLowerCase()) {
            case 'paid':
                return 'bg-emerald-50 text-emerald-700 border-emerald-200';
            case 'sent':
                return 'bg-amber-50 text-amber-700 border-amber-200';
            case 'created':
            case 'draft':
                return 'bg-gray-50 text-gray-700 border-gray-200';
            case 'overdue':
                return 'bg-red-50 text-red-700 border-red-200';
            default:
                return 'bg-gray-50 text-gray-700 border-gray-200';
        }
    };

    return (
        <span className={`
            inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border
            ${getStatusStyles(status)} ${className}
        `}>
            {children}
        </span>
    );
};

// Empty State Component
interface EmptyStateProps {
    icon?: string;
    title: string;
    description?: string;
    action?: React.ReactNode;
    className?: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({ 
    icon = '📋',
    title, 
    description,
    action,
    className = '' 
}) => {
    return (
        <div className={`text-center py-12 ${className}`}>
            <div className="text-6xl mb-4">{icon}</div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">{title}</h3>
            {description && (
                <p className="text-gray-500 mb-6 max-w-sm mx-auto">{description}</p>
            )}
            {action && action}
        </div>
    );
};

import React from 'react';

interface ButtonProps {
    children: React.ReactNode;
    type?: 'button' | 'submit' | 'reset';
    variant?: 'primary' | 'secondary' | 'danger';
    onClick?: () => void;
    disabled?: boolean;
    className?: string;
}

export const Button: React.FC<ButtonProps> = ({
    children,
    type = 'button',
    variant = 'primary',
    onClick,
    disabled = false,
    className = '',
}) => {
    const baseStyle = 'px-4 py-2 rounded-md font-medium focus:outline-none focus:ring-2 focus:ring-offset-2';

    const variantStyles = {
        primary: 'bg-blue-600 hover:bg-blue-700 text-white focus:ring-blue-500',
        secondary: 'bg-gray-200 hover:bg-gray-300 text-gray-800 focus:ring-gray-500',
        danger: 'bg-red-600 hover:bg-red-700 text-white focus:ring-red-500',
    };

    return (
        <button
            type={type}
            onClick={onClick}
            disabled={disabled}
            className={`${baseStyle} ${variantStyles[variant]} ${disabled ? 'opacity-50 cursor-not-allowed' : ''} ${className}`}
        >
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
    placeholder?: string;
    required?: boolean;
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
    placeholder,
    required = false,
    className = '',
    min,
    step,
}) => {
    return (
        <div className={`mb-4 ${className}`}>
            <label htmlFor={id} className="block text-sm font-medium text-gray-700 mb-1">
                {label}{required && <span className="text-red-500">*</span>}
            </label>
            <input
                id={id}
                name={name}
                type={type}
                value={value}
                onChange={onChange}
                placeholder={placeholder}
                required={required}
                min={min}
                step={step}
                className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
        </div>
    );
};

interface TextareaProps {
  id: string;
  name?: string;
  label: string;
  value?: string;
  onChange?: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
  placeholder?: string;
  required?: boolean;
  rows?: number;
  className?: string;
}

export const Textarea: React.FC<TextareaProps> = ({
  id,
  name,
  label,
  value,
  onChange,
  placeholder,
  required = false,
  rows = 3,
  className = '',
}) => {
  return (
    <div className={`mb-4 ${className}`}>
      <label htmlFor={id} className="block text-sm font-medium text-gray-700 mb-1">
        {label}{required && <span className="text-red-500">*</span>}
      </label>
      <textarea
        id={id}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        rows={rows}
        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
      />
    </div>
  );
};

interface SelectProps {
    id: string;
    name?: string;
    label: string;
    value?: string | number;
    onChange?: (e: React.ChangeEvent<HTMLSelectElement>) => void;
    options: { value: string | number; label: string }[];
    placeholder?: string;
    required?: boolean;
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
    className = '',
}) => {
    return (
        <div className={`mb-4 ${className}`}>
            <label htmlFor={id} className="block text-sm font-medium text-gray-700 mb-1">
                {label}{required && <span className="text-red-500">*</span>}
            </label>
            <select
                id={id}
                name={name}
                value={value}
                onChange={onChange}
                required={required}
                className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
                {placeholder && (
                    <option value="" disabled>
                        {placeholder}
                    </option>
                )}
                {options.map((option) => (
                    <option key={option.value} value={option.value}>
                        {option.label}
                    </option>
                ))}
            </select>
        </div>
    );
};

interface CardProps {
    title?: string;
    children: React.ReactNode;
    className?: string;
}

export const Card: React.FC<CardProps> = ({ title, children, className = '' }) => {
    return (
        <div className={`bg-white rounded-lg shadow p-6 ${className}`}>
            {title && <h2 className="text-xl font-semibold mb-4">{title}</h2>}
            {children}
        </div>
    );
};

interface TableProps {
    columns: { key: string; label: string }[];
    data: any[];
    onRowClick?: (row: any) => void;
    className?: string;
}

export const Table: React.FC<TableProps> = ({ columns, data, onRowClick, className = '' }) => {
    return (
        <div className={`overflow-x-auto ${className}`}>
            <table className="w-full">
                <thead>
                    <tr className="bg-gray-50">
                        {columns.map((column) => (
                            <th key={column.key} className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                {column.label}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                    {data.map((row, index) => (
                        <tr
                            key={index}
                            onClick={() => onRowClick && onRowClick(row)}
                            className={onRowClick ? 'hover:bg-gray-100 cursor-pointer' : ''}
                        >
                            {columns.map((column) => (
                                <td key={`${index}-${column.key}`} className="px-6 py-4 whitespace-nowrap">
                                    {row[column.key]}
                                </td>
                            ))}
                        </tr>
                    ))}
                    {data.length === 0 && (
                        <tr>
                            <td colSpan={columns.length} className="px-6 py-4 text-center text-sm text-gray-500">
                                No data available
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

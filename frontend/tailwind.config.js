/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/**/*.{js,jsx,ts,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                // Brand colors
                brand: {
                    50: '#eff6ff',
                    100: '#dbeafe',
                    200: '#bfdbfe',
                    300: '#93c5fd',
                    400: '#60a5fa',
                    500: '#3b82f6',
                    600: '#2563eb', // Primary brand color
                    700: '#1d4ed8',
                    800: '#1e40af',
                    900: '#1e3a8a',
                },
                // Status colors following the guidelines
                success: {
                    50: '#d1fae5',
                    100: '#a7f3d0',
                    200: '#6ee7b7',
                    300: '#34d399',
                    400: '#10b981',
                    500: '#059669',
                    600: '#047857', // Success color
                    700: '#065f46',
                    800: '#064e3b',
                    900: '#022c22',
                },
                warning: {
                    50: '#fef3c7',
                    100: '#fde68a',
                    200: '#fcd34d',
                    300: '#fbbf24',
                    400: '#f59e0b',
                    500: '#d97706',
                    600: '#b45309', // Warning color
                    700: '#92400e',
                    800: '#78350f',
                    900: '#451a03',
                },
                danger: {
                    50: '#fee2e2',
                    100: '#fecaca',
                    200: '#fca5a5',
                    300: '#f87171',
                    400: '#ef4444',
                    500: '#dc2626',
                    600: '#b91c1c', // Danger color
                    700: '#991b1b',
                    800: '#7f1d1d',
                    900: '#450a0a',
                },
            },
            fontFamily: {
                sans: [
                    '-apple-system',
                    'BlinkMacSystemFont',
                    'Segoe UI',
                    'Roboto',
                    'Oxygen',
                    'Ubuntu',
                    'Cantarell',
                    'Fira Sans',
                    'Droid Sans',
                    'Helvetica Neue',
                    'sans-serif',
                ],
            },
            fontSize: {
                'xs': ['0.75rem', { lineHeight: '1rem' }],
                'sm': ['0.875rem', { lineHeight: '1.25rem' }],
                'base': ['1rem', { lineHeight: '1.5rem' }],
                'lg': ['1.125rem', { lineHeight: '1.75rem' }],
                'xl': ['1.25rem', { lineHeight: '1.75rem' }],
                '2xl': ['1.5rem', { lineHeight: '2rem' }],
                '3xl': ['1.875rem', { lineHeight: '2.25rem' }],
                '4xl': ['2.25rem', { lineHeight: '2.5rem' }],
            },
            spacing: {
                '18': '4.5rem',
                '88': '22rem',
                '128': '32rem',
            },
            animation: {
                'fade-in': 'fadeIn 0.3s ease-in-out',
                'slide-up': 'slideUp 0.3s ease-out',
                'scale-in': 'scaleIn 0.2s ease-out',
                'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
            },
            boxShadow: {
                'brand': '0 4px 14px 0 rgba(37, 99, 235, 0.15)',
                'success': '0 4px 14px 0 rgba(5, 150, 105, 0.15)',
                'warning': '0 4px 14px 0 rgba(217, 119, 6, 0.15)',
                'danger': '0 4px 14px 0 rgba(220, 38, 38, 0.15)',
            },
            borderRadius: {
                'xl': '0.75rem',
                '2xl': '1rem',
            },
        },
    },
    plugins: [],
}

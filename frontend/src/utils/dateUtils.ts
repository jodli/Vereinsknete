/**
 * Utility functions for handling date parsing and formatting
 */

/**
 * Parse a date string in DD.MM.YYYY format to a JavaScript Date object
 * @param dateString - Date string in DD.MM.YYYY format (e.g., "14.05.2025")
 * @returns Date object or null if parsing fails
 */
export const parseGermanDateString = (dateString: string): Date | null => {
    if (!dateString || typeof dateString !== 'string') return null;

    try {
        let date: Date | null = null;

        // Accept already ISO formatted (YYYY-MM-DD) strings from backend
        if (/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
            date = new Date(dateString + 'T00:00:00');
        } else if (/^\d{2}\.\d{2}\.\d{4}$/.test(dateString)) {
            // Convert DD.MM.YYYY to YYYY-MM-DD
            const isoString = dateString.split('.').reverse().join('-');
            date = new Date(isoString + 'T00:00:00');
        } else {
            // Fallback attempt (let Date try to parse)
            date = new Date(dateString);
        }

        if (!date || isNaN(date.getTime())) return null;
        return date;
    } catch (error) {
        console.error('Error parsing date string:', dateString, error);
        return null;
    }
};

/**
 * Format a Date object to DD.MM.YYYY string
 * @param date - Date object to format
 * @returns Formatted date string in DD.MM.YYYY format
 */
export const formatDateToGerman = (date: Date): string => {
    // Core utility (and related tests) expect NON-zero-padded day & month (e.g., 5.1.2024)
    const day = date.getDate();
    const month = date.getMonth() + 1; // 1-based
    const year = date.getFullYear();
    return `${day}.${month}.${year}`;
};

/**
 * Padded variant (dd.MM.yyyy) for UI tables that need consistent alignment.
 */
export const formatDateToGermanPadded = (date: Date): string => {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    return `${day}.${month}.${date.getFullYear()}`;
};

/**
 * Safely parse and format a date string from the backend
 * @param dateString - Date string from backend (DD.MM.YYYY format)
 * @returns Formatted date string for display
 */
export const formatBackendDate = (dateString: string): string => {
    const date = parseGermanDateString(dateString);
    return date ? formatDateToGerman(date) : 'Invalid Date';
};

/**
 * Utility functions for handling date parsing and formatting
 */

/**
 * Parse a date string in DD.MM.YYYY format to a JavaScript Date object
 * @param dateString - Date string in DD.MM.YYYY format (e.g., "14.05.2025")
 * @returns Date object or null if parsing fails
 */
export const parseGermanDateString = (dateString: string): Date | null => {
    if (!dateString || typeof dateString !== 'string') {
        return null;
    }

    try {
        // Convert DD.MM.YYYY to YYYY-MM-DD for JavaScript Date parsing
        const isoString = dateString.split('.').reverse().join('-');
        const date = new Date(isoString);

        // Check if the date is valid
        if (isNaN(date.getTime())) {
            return null;
        }

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
    return date.toLocaleDateString('de-DE');
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

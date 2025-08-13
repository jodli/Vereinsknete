import { formatBackendDate } from '../dateUtils';

describe('dateUtils', () => {
  describe('formatBackendDate', () => {
    it('formats German date string correctly', () => {
      const germanDate = '15.01.2024';
      const formatted = formatBackendDate(germanDate);
      expect(formatted).toBe('15.1.2024'); // toLocaleDateString doesn't zero-pad
    });

    it('handles different date formats', () => {
      const germanDate = '15.01.2024';
      const formatted = formatBackendDate(germanDate);
      expect(formatted).toBe('15.1.2024');
    });

    it('handles single digit dates and months', () => {
      const germanDate = '05.01.2024';
      const formatted = formatBackendDate(germanDate);
      expect(formatted).toBe('5.1.2024');
    });

    it('handles different months', () => {
      const dates = [
        { input: '15.01.2024', expected: '15.1.2024' },
        { input: '28.02.2024', expected: '28.2.2024' },
        { input: '31.12.2024', expected: '31.12.2024' },
      ];

      dates.forEach(({ input, expected }) => {
        expect(formatBackendDate(input)).toBe(expected);
      });
    });

    it('handles leap year dates', () => {
      const leapYearDate = '29.02.2024';
      const formatted = formatBackendDate(leapYearDate);
      expect(formatted).toBe('29.2.2024');
    });

    it('handles invalid date strings gracefully', () => {
      const invalidDate = 'invalid-date';
      const formatted = formatBackendDate(invalidDate);
      expect(formatted).toBe('Invalid Date');
    });

    it('handles empty string', () => {
      const emptyDate = '';
      const formatted = formatBackendDate(emptyDate);
      expect(formatted).toBe('Invalid Date');
    });

    it('handles different years', () => {
      const dates = [
        { input: '15.06.2023', expected: '15.6.2023' },
        { input: '01.12.2025', expected: '1.12.2025' },
      ];

      dates.forEach(({ input, expected }) => {
        expect(formatBackendDate(input)).toBe(expected);
      });
    });

    it('handles null and undefined gracefully', () => {
      expect(formatBackendDate(null as any)).toBe('Invalid Date');
      expect(formatBackendDate(undefined as any)).toBe('Invalid Date');
    });
  });
});
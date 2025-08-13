module.exports = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: [
    '<rootDir>/src/test-utils/setup.ts',
    '<rootDir>/src/test-utils/testSetup.tsx'
  ],
  // We allow overriding reporters via npm script (e.g., test:pages:summary)
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/index.tsx',
    '!src/reportWebVitals.ts',
    '!src/test-utils/**',
  ],
  coverageThreshold: {
    global: {
      statements: 70,
      branches: 65,
      functions: 75,
      lines: 70,
    },
  },
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
  },
  transformIgnorePatterns: [
    'node_modules/(?!(msw|@mswjs|react-datepicker)/)',
  ],
  testTimeout: 10000,
  maxWorkers: 1, // Run tests serially to avoid conflicts
};
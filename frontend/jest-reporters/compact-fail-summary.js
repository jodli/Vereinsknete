/**
 * CompactFailSummaryReporter
 * A concise Jest reporter that summarizes only failing tests with a compact format.
 *
 * Options (configured in jest.config.js reporters array):
 *   label   : string  – A short label describing the test target (default: "Tests")
 *   command : string  – A command hint to re-run for full details (optional)
 *
 * Example configuration:
 *   reporters: [
 *     'default',
 *     ['<rootDir>/jest-reporters/compact-fail-summary.js', { label: 'Unit', command: 'npm test' }]
 *   ]
 */
class CompactFailSummaryReporter {
  constructor(globalConfig, options = {}) {
    this.label = options.label || 'Tests';
    this.command = options.command; // Optional command hint
  }

  onRunComplete(contexts, results) {
    const { numFailedTests, numFailedTestSuites, numPassedTests, numTotalTests, startTime } = results;
    const duration = ((Date.now() - startTime) / 1000).toFixed(2);
    const label = this.label;

    if (numFailedTests === 0) {
      console.log(`✅ ${label}: ${numPassedTests}/${numTotalTests} tests passed in ${duration}s`);
      return;
    }

    console.log(`\n❌ ${label}: ${numFailedTests} failed test(s) across ${numFailedTestSuites} suite(s) (total ${numTotalTests}) in ${duration}s`);
    console.log('─'.repeat(80));

    if (!Array.isArray(results.testResults)) {
      console.log('Unexpected Jest result shape: no testResults array.');
      return;
    }

    results.testResults.forEach(fileResult => {
      const assertions = Array.isArray(fileResult.testResults) ? fileResult.testResults : [];
      const failed = assertions.filter(a => a.status === 'failed');
      if (failed.length === 0) return;
      console.log(`File: ${fileResult.testFilePath}`);
      failed.forEach(a => {
        const titlePath = [...(a.ancestorTitles || []), a.title].join(' > ');
        const firstLine = (a.failureMessages && a.failureMessages[0]) ? a.failureMessages[0].split('\n').find(l => l.trim()) : '';
        const trimmed = (firstLine || '').trim();
        console.log(`  ✗ ${titlePath}`);
        if (trimmed) {
          const shortMsg = trimmed.substring(0, 180) + (trimmed.length > 180 ? '…' : '');
          console.log(`    → ${shortMsg}`);
        }
      });
      console.log('-'.repeat(80));
    });

    const hint = this.command ? ` (rerun with ${this.command} for full details)` : '';
    console.log(`Summary${hint}`);
  }
}

module.exports = CompactFailSummaryReporter;

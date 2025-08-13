// Aggregator to ensure folder-based integration tests are compiled.
// Having this file avoids name collision with the tests/integration/ dir (we removed integration.rs).
mod common;
mod integration;

// Optionally re-export for shorter paths in failure output.
pub use integration::*;

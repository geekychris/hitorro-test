<p align="center">
  <img src="assets/logo.png" alt="Hitorro Test logo" width="180">
</p>

# Hitorro Test Suite

Comprehensive unit tests for the Hitorro project, covering features, analysis, text processing, persistence, and utilities.

## Description

This module contains **79 JUnit test classes** covering various Hitorro subsystems:

- **Analysis Tests**: NLP analysis, link engine, sentence enumeration
- **Features Tests**: Feature extraction, indexing, serialization
- **Text Tests**: Text processing, inverters, classifiers
- **Base DMS Tests**: Persistence, versioning, type system, sessions
- **Network Tests**: RPC, clustering, node groups
- **Utilities Tests**: Core utilities, CSV processing, IO operations, SQL-like queries
- **JSON Type System Tests**: JSON type system and operators

## Test Coverage

### Analysis Module Tests (com.hitorro.analysis.*)
- Classifier model tests
- Link engine (full tests, RLE primitives)
- Sentence enumeration
- Text analysis and emitters

### Features Module Tests (com.hitorro.features.*)
- Feature extraction
- Feature indexing
- Feature serialization

### Base DMS Tests (com.hitorro.basedms.*)
- Authentication
- Category management
- Persistence (Hibernate, type system)
- RSS feed retrieval
- SSH utilities
- Transformer/workflow tests
- Versioning

### Text Tests (com.hitorro.basetext.*)
- Text inverters
- Classification tests

### JSON Type System Tests (com.hitorro.jsontypesystem.*)
- Type system operations
- JSON operators

### Network Tests (com.hitorro.network.*)
- RPC communication
- Clustering
- Node groups

### Utility Tests (com.hitorro.test.*)
- Core utilities
- CSV consumers
- Database operations
- File system operations
- String utilities
- URL parsing

### Log Digest Tests (com.hitorro.logdigest.*)
- Log file processing utilities

## Project Structure

```
hitorro-test/
├── pom.xml
├── README.md
├── DEPENDENCIES.md
└── src/
    └── main/
        └── java/
            └── com/hitorro/
                ├── analysis/       (8 test classes)
                ├── basedms/        (10 test classes)
                ├── basetext/       (2 test classes)
                ├── features/       (3 test classes)
                ├── jsontypesystem/ (1 test class)
                ├── logdigest/      (1 test class)
                ├── network/        (2 test classes)
                └── test/           (52 test classes)
                    ├── cglib/
                    ├── csvconsumer/
                    ├── db/
                    ├── dms/
                    ├── html/
                    ├── statemachine/
                    ├── transformer/
                    ├── typesystem/
                    └── util/
```

## Building

⚠️ **Important**: This module requires all Hitorro modules to be installed to your local Maven repository first.

```bash
# With dependencies installed
mvn clean compile

# Run tests (after uncommenting dependencies in pom.xml)
mvn test

# Package
mvn package
```

## Requirements

- Java 19 or higher
- Maven 3.6+
- JUnit 4.13.2
- All Hitorro modules installed locally (see DEPENDENCIES.md)

## Dependencies

This test module depends on **8 internal Hitorro modules**:

1. `hitorro-features` - Feature extraction system
2. `hitorro-basedms` - Base DMS/persistence layer
3. `hitorro-jsonsql` - JSON SQL query engine
4. `hitorro-logdigest` - Log processing
5. `hitorro-analysis` - NLP analysis tools
6. `hitorro-conversation` - Conversation tracking
7. `hitorro-text-core` - Text processing core
8. `hitorro-text-persistence` - Text storage layer

### Installing Dependencies

See `DEPENDENCIES.md` for detailed instructions on installing the required Hitorro modules.

Quick version:

```bash
# From the hitorro-parent directory
cd /path/to/hitorro-parent

# Install all modules
mvn clean install

# Or install specific modules needed by tests
mvn clean install -pl hitorro-features,hitorro-basedms,hitorro-jsonsql,hitorro-logdigest,hitorro-analysis,hitorro-conversation,hitorro-text-core,hitorro-text-persistence
```

Then uncomment the dependencies in `pom.xml` and run:

```bash
cd /path/to/hitorro-test
mvn clean test
```

## Running Specific Tests

```bash
# Run a specific test class
mvn test -Dtest=FeatureIndexTest

# Run tests matching a pattern
mvn test -Dtest=*Test

# Run with debug output
mvn test -X
```

## Test Organization

Tests are organized by the module/subsystem they test:

- **Analysis tests**: Test NLP, link analysis, conversation tracking
- **Base DMS tests**: Test persistence, type system, database operations
- **Feature tests**: Test feature extraction and indexing
- **Text tests**: Test text processing pipelines
- **Utility tests**: Test core utilities, IO, networking

## Notes

- Tests use JUnit 4 (not JUnit 5)
- Some tests may require external resources (models, data files)
- Tests are currently **skipped by default** (see pom.xml) until dependencies are resolved
- All test classes were consolidated under `com.hitorro` package (no more `ht.*` packages)

## License

[Add appropriate license information]

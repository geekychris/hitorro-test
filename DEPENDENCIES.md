# Dependencies Guide for Hitorro Test Suite

## Overview

The test suite depends on **8 internal Hitorro modules**. These dependencies are currently **commented out** in the `pom.xml` to allow the project to be created standalone, but must be resolved to run the tests.

## Required Internal Modules

### 1. hitorro-features (3.0.0)
**Purpose**: Feature extraction and indexing
**Tests that require it**:
- `com.hitorro.features.FeatureExtractorTest`
- `com.hitorro.features.FeatureIndexTest`
- `com.hitorro.features.FeatureSerializationTest`

### 2. hitorro-basedms (3.0.0)
**Purpose**: Base document management system, persistence, type system
**Tests that require it**:
- `com.hitorro.test.AuthenticationTest`
- `com.hitorro.test.CategoryTest`
- `com.hitorro.test.PersistenceTest`
- `com.hitorro.test.dms.*` (multiple tests)
- `com.hitorro.test.typesystem.*` (multiple tests)
- `com.hitorro.test.transformer.*`
- `com.hitorro.basedms.*` (RSS, nanny tests)

### 3. hitorro-jsonsql (3.0.0)
**Purpose**: JSON-based SQL query engine
**Tests that require it**:
- `com.hitorro.jsontypesystem.JsonTypesystemTest`

### 4. hitorro-logdigest (3.0.0)
**Purpose**: Log file processing and analysis
**Tests that require it**:
- `com.hitorro.logdigest.LogDigesterUtilTest`

### 5. hitorro-analysis (3.0.0)
**Purpose**: NLP analysis, text classification
**Tests that require it**:
- `com.hitorro.analysis.classifier.ModelTest`
- `com.hitorro.analysis.linkengine.*`
- `com.hitorro.analysis.sentenceenumeration.*`
- `com.hitorro.analysis.text.*`

### 6. hitorro-conversation (3.0.0)
**Purpose**: Conversation and link tracking
**Tests that require it**:
- Tests related to conversation analysis and link engines

### 7. hitorro-text-core (3.0.0)
**Purpose**: Core text processing functionality
**Tests that require it**:
- `com.hitorro.basetext.test.TestInverter`
- Text processing tests

### 8. hitorro-text-persistence (3.0.0)
**Purpose**: Text storage and retrieval
**Tests that require it**:
- Text persistence and indexing tests

## How to Resolve Dependencies

### Option 1: Install All Modules (Recommended)

If you have access to the full Hitorro project:

```bash
# Navigate to the hitorro-parent directory
cd /path/to/hitorro/hitorro-parent

# Install all modules to local Maven repository
mvn clean install

# Or install only the required modules
mvn clean install -pl hitorro-features,hitorro-basedms,hitorro-jsonsql,hitorro-logdigest,hitorro-analysis,hitorro-conversation,hitorro-text-core,hitorro-text-persistence
```

Then edit `pom.xml` and **uncomment** the dependency section:

```xml
<!-- Uncomment this entire block -->
<dependency>
    <groupId>com.hitorro</groupId>
    <artifactId>hitorro-features</artifactId>
    <version>3.0.0</version>
</dependency>
<!-- ... and all other hitorro dependencies -->
```

Finally, update the surefire plugin to enable tests:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M9</version>
    <configuration>
        <!-- Remove or set to false -->
        <skipTests>false</skipTests>
    </configuration>
</plugin>
```

Now you can run the tests:

```bash
cd /path/to/hitorro-test
mvn clean test
```

### Option 2: Run Tests From Parent Project

The simplest approach is to run tests from the parent project where all modules are available:

```bash
cd /path/to/hitorro-parent
mvn test -pl hitorro-test
```

### Option 3: Remove Tests for Missing Modules

If you don't need all tests, you can:

1. Keep dependencies commented out
2. Delete test classes for modules you don't have
3. Keep only the tests for modules you have access to

## Verification

After setting up dependencies, verify with:

```bash
# Should compile without errors
mvn clean compile

# Run a single test to verify
mvn test -Dtest=StringUtilTest

# Run all tests
mvn test
```

## Current Status

- ✅ Project structure created
- ✅ All 79 test files copied
- ✅ POM configured for standalone use
- ⚠️ Dependencies commented out (intentional)
- ⚠️ Tests skipped by default (until dependencies resolved)

## Test Execution Matrix

| Module | Test Count | Requires Dependencies |
|--------|------------|----------------------|
| analysis | 8 | hitorro-analysis, hitorro-conversation |
| basedms | 10 | hitorro-basedms |
| basetext | 2 | hitorro-text-core |
| features | 3 | hitorro-features |
| jsontypesystem | 1 | hitorro-jsonsql |
| logdigest | 1 | hitorro-logdigest |
| network | 2 | hitorro-basedms (for RPC tests) |
| test/util | 52 | Various (hitorro-basedms primarily) |

## Next Steps

1. Choose one of the options above
2. If using Option 1: Install dependencies and uncomment in POM
3. Update surefire plugin to enable tests
4. Run `mvn clean test`
5. Review test results and resolve any failures

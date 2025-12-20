# Test Inventory

Complete list of all 79 test classes in the Hitorro Test Suite.

## Summary by Package

| Package | Test Count | Description |
|---------|------------|-------------|
| analysis | 10 | NLP analysis, link engine, text processing |
| basedms | 3 | DMS, RSS feeds, nanny service |
| basetext | 1 | Text inversion and processing |
| features | 3 | Feature extraction and indexing |
| jsontypesystem | 1 | JSON type system operations |
| logdigest | 1 | Log file processing |
| network | 2 | RPC and node groups |
| test (root) | 14 | Core functionality tests |
| test.cglib | 11 | CGLib proxy and setter/getter tests |
| test.csvconsumer | 1 | CSV processing |
| test.db | 3 | Database operations |
| test.dms | 7 | Document management system |
| test.html | 1 | HTML parsing |
| test.statemachine | 1 | State machine |
| test.transformer | 1 | Document transformation |
| test.typesystem | 6 | Type system operations |
| test.util | 1 | Utility parameters |
| util | 11 | Core utilities, I/O, threading, URL parsing |

**Total: 79 test classes**

## Detailed Test List

### Analysis Tests (10)

#### NLP & Classification
- `com.hitorro.analysis.classifier.ModelTest` - Model classification tests

#### Link Engine
- `com.hitorro.analysis.linkengine.LinkEngineFullTest` - Full link engine tests
- `com.hitorro.analysis.linkengine.RLELinkEnginePrimitivesTest` - RLE primitives tests

#### Sentence Enumeration
- `com.hitorro.analysis.sentenceenumeration.SentenceEnumeratorTest` - Sentence enumeration

#### Text Analysis
- `com.hitorro.analysis.text.EmitterElem` - Emitter element support class
- `com.hitorro.analysis.text.EmitterElemMapper` - Emitter element mapper
- `com.hitorro.analysis.text.Emmitter2CSV` - Emitter to CSV converter
- `com.hitorro.analysis.text.HashBits` - Hash bit operations
- `com.hitorro.analysis.text.TestEmitter` - Emitter tests
- `com.hitorro.analysis.text.TextAnalysisTests` - Text analysis suite

### Base DMS Tests (3)

- `com.hitorro.basedms.nanny.NannyTest` - Service management tests
- `com.hitorro.basedms.rss.FeedRetrievalTest` - RSS feed retrieval
- `com.hitorro.basedms.rss.RssTest` - RSS processing tests

### Base Text Tests (1)

- `com.hitorro.basetext.test.TestInverter` - Text inversion tests

### Features Tests (3)

- `com.hitorro.features.FeatureExtractorTest` - Feature extraction
- `com.hitorro.features.FeatureIndexTest` - Feature indexing
- `com.hitorro.features.FeatureSerializationTest` - Feature serialization

### JSON Type System Tests (1)

- `com.hitorro.jsontypesystem.JsonTypesystemTest` - JSON type system operations

### Log Digest Tests (1)

- `com.hitorro.logdigest.LogDigesterUtilTest` - Log processing utilities

### Network Tests (2)

- `com.hitorro.network.rpc.RPCTest` - RPC communication tests
- `com.hitorro.network.rpc.test.NodeGroupTest` - Node group management

### Core Tests (14)

- `com.hitorro.test.AuthenticationTest` - Authentication system
- `com.hitorro.test.BasicFileUtilTest` - File utilities
- `com.hitorro.test.CategoryTest` - Category management
- `com.hitorro.test.ClusterTest` - Clustering functionality
- `com.hitorro.test.CoreUtilTest` - Core utilities
- `com.hitorro.test.PersistenceTest` - Persistence layer
- `com.hitorro.test.PropertiesTest` - Properties handling
- `com.hitorro.test.ResourceCacheTest` - Resource caching
- `com.hitorro.test.SelectTreeTest` - Select tree operations
- `com.hitorro.test.SshUtilTest` - SSH utilities
- `com.hitorro.test.StringUtilTest` - String utilities
- `com.hitorro.test.TestBooleanSetterGetter` - Boolean getter/setter
- `com.hitorro.test.TransactionDirTest` - Transaction directories

### CGLib Tests (11)

Dynamic proxy and getter/setter generation tests:
- `com.hitorro.test.cglib.TestCGLib` - Main CGLib tests
- `com.hitorro.test.cglib.TestDoubleValueSetterGetter` - Double setters/getters
- `com.hitorro.test.cglib.TestIntSetterGetter` - Integer setters/getters
- `com.hitorro.test.cglib.TestLongSetterGetter` - Long setters/getters
- `com.hitorro.test.cglib.TestSetterGetter` - Generic setters/getters
- `com.hitorro.test.cglib.V` - Test value class
- `com.hitorro.test.cglib.V2` - Test value class 2
- `com.hitorro.test.cglib.V3` - Test value class 3
- `com.hitorro.test.cglib.V4` - Test value class 4
- `com.hitorro.test.cglib.VLong` - Long value class

### CSV Tests (1)

- `com.hitorro.test.csvconsumer.CSVConsumerTest` - CSV processing

### Database Tests (3)

- `com.hitorro.test.db.ConnectionTest` - Database connections
- `com.hitorro.test.db.DatabaseUtilTest` - Database utilities
- `com.hitorro.test.db.ExecuteScriptTest` - SQL script execution

### DMS Tests (7)

- `com.hitorro.test.dms.ContainerTest` - Container management
- `com.hitorro.test.dms.ContentTest` - Content management
- `com.hitorro.test.dms.HTSerializableTest` - Serialization
- `com.hitorro.test.dms.NamedLongTest` - Named long values
- `com.hitorro.test.dms.NonPersistObject` - Non-persistent objects
- `com.hitorro.test.dms.TypeSystemTest` - Type system
- `com.hitorro.test.dms.VersioningTest` - Version control

### HTML Tests (1)

- `com.hitorro.test.html.HTMLParserTest` - HTML parsing

### State Machine Tests (1)

- `com.hitorro.test.statemachine.StateMachineTest` - State machine operations

### Transformer Tests (1)

- `com.hitorro.test.transformer.DocTransformerTest` - Document transformation

### Type System Tests (6)

- `com.hitorro.test.typesystem.DumpLoadTest` - Dump and load operations
- `com.hitorro.test.typesystem.DumpTest` - Dump operations
- `com.hitorro.test.typesystem.LoadFullSuiteDataTest` - Full suite loading
- `com.hitorro.test.typesystem.LoadLoadTest` - Load operations
- `com.hitorro.test.typesystem.LoadTest` - Basic loading
- `com.hitorro.test.typesystem.TypeSystemTestUtil` - Test utilities

### Utility Parameter Tests (1)

- `com.hitorro.test.util.params.ConfigChangeTest` - Configuration changes

### Utility Tests (11)

- `com.hitorro.util.basefile.TestHDFS` - HDFS file operations
- `com.hitorro.util.core.ComplexPropsTest` - Complex properties
- `com.hitorro.util.core.DateTests` - Date utilities
- `com.hitorro.util.core.longword.TestBits` - Bit manipulation
- `com.hitorro.util.core.queue.TestThreadedQueue` - Threaded queues
- `com.hitorro.util.core.thread.farm.jobfarm.JobFarmTest` - Job farm
- `com.hitorro.util.core.thread.farm.jobfarm.TestEnqueueJobAgent` - Job enqueueing
- `com.hitorro.util.core.thread.farm.jobfarm.TestFarmCommand` - Farm commands
- `com.hitorro.util.core.thread.farm.jobfarm.TestJob` - Job handling
- `com.hitorro.util.core.topologicalsort.TestNode` - Topological sort nodes
- `com.hitorro.util.core.topologicalsort.TopologicalSortTest` - Topological sorting
- `com.hitorro.util.sql.SqlTests` - SQL-like operations
- `com.hitorro.util.swig.SwigUtilTests` - SWIG utilities
- `com.hitorro.util.urlparser.UrlTests` - URL parsing

## Running Tests

See `DEPENDENCIES.md` for instructions on setting up dependencies.

```bash
# Run all tests (after dependencies are installed)
mvn test

# Run a specific test
mvn test -Dtest=StringUtilTest

# Run tests by pattern
mvn test -Dtest=*Test
```

# SnapshotTest

## Description

SnapsbotTest is a simple unit test library to allow tests to assert if new output is matching against previously recorded test output snapshot. The output is serilized as either text files or json files which are version-controlled together with the source code. The basic idea is from the popular frontend [Jest SnapshotTest](https://jestjs.io/docs/en/snapshot-testing). 

## Snapshot Test philosophy

- Prefer auto-generated test output snapshot over manually written assertion boilerplate code. While manual assertion is still necessary, it should be minimized to assert specific value unique to the specific test
- Snapshot test provides much better assertion coverage with minimum effort. A single statement will cover the whole output. 
- Recorded snapshots should be part of the source code to be version-controlled. 
- Recorded snapshots should be easily readable just as normal source code so that it can also be served as specification documentations.
- Unit test should always generate deterministic output to avoid flaky testing. For example, it should never output values based on system clock, system locale etc.

## Features

- Minimum configuration: to use this library, just need add maven dependency, everything is working out of the box.
- Options to customize JSON coder with [JSONCoderOption](../JSONCoder/src/main/java/org/jsonex/jsoncoder/JSONCoderOption.java), so that it's possible to:
  - Exclude/Mask certain fields
  - Provide custom coder for certain classes
  - Many other options to adjust the JSON output

## Usage
- Add dependency
    ```xml
     <dependency>
       <groupId>org.jsonex</groupId>
       <artifactId>SnapshotTest</artifactId>
       <scope>test</scope>
       <version>${SnapshotTestVersion}</version>
     </dependency>
    ````
  You can get current version by searching [maven central](https://search.maven.org/search?q=g:org.jsonex)

- In unit test
  ```java
  import static org.jsonex.snapshottest.Snapshot.assertMatchesSnapshot;   
  class SomeTest {
    @Test public static void someTest() {
      SomeClass result = businessFunction();
      assertMatchesSnapshot(result);
    }
  }
  ```
  **How it works**:
    - __Recording Mode__: when this test is first run, as there's no existing snapshot, it will record the output by serializing `result` with [JSONCoder](JSONCoder/src/main/java/org/jsonex/jsoncoder/JSONCoder.java), and store this JSON file under `resource/__snapshot__` folder.
    - __Verifying Mode__: for subsequent runs, as there's already snapshot recorded, it will serialize the new output and compare it with the recorded snapshot. Test passes if it matches. Otherwise test fails, and a tmp file will be generated with the new snapshot under the same folder. Developers can compare the tmp file with exsting snapshot file to determine if the delta is expected or not.
    - __Approve New Snapshot__: If the delta it's expected, just delete the previously recorded snapshot file. Rerun the test, a new version of the snapshot will be generated.
    
- Advanced usage
  - **Customize JSONCoder**: sometimes, we want to ignore certain fields in the snapshot or want to use some customized JSON encoder for certain class, we can pass an additional [SnapshotOption](src/main/java/org/jsonex/snapshottest/SnapshotOption.java) parameter to the `assertMatchesSnapshot()` method to customize the JSONCoder

  - **Named Snapshot**: by default the snapshot file name is based on the test class and test method. If there's multiple `assertMatchesSnapshot()` in the same test method, to avoid file name conflict, you can pass in an optional `name` parameter to the `assertMatchesSnapshot()` method.
  - **Examples**: SnapshotTest library itself is tested with SnapshotTest, so for the live working example, you can refer the [SnapshotTest](src/test/java/org/jsonex/snapshottest/SnapshotTest.java) of this library
    
## Contributions and Enhancement

Even this library provides useful function, it's still rudimentary. Lot of space of enhancement. If you find any issues or have any better idea, you are welcome to open github issues or submit PRs.
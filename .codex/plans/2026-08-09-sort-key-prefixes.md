# Sort Key Prefixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `TASK#` sort key naming convention과 `begins_with` Query를 실제 DynamoDB Local에서 확인하는 독립 Spring Boot theory 프로젝트를 만든다.

**Architecture:** non-web Spring Boot test가 blocking `DynamoDbClient` bean을 주입받는다. `ItemKeyFactory`가 key 문자열 생성 규칙을 담당하고, `SortKeyPrefixService`가 같은 owner partition에 task와 stats item을 저장한 뒤 task prefix만 Query한다.

**Tech Stack:** Kotlin 2.1.20, Java 21, Spring Boot 3.4.5, AWS SDK for Java 2.31.78, JUnit 5, AssertJ, Gradle Kotlin DSL, DynamoDB Local

## Global Constraints

- 프로젝트 경로는 `theory/05-sort-key-prefixes`다.
- controller와 runner를 만들지 않고 test로만 실행한다.
- blocking `DynamoDbClient`를 사용한다.
- DynamoDB 개념과 operation에 관련된 주요 코드만 간단한 한국어 주석으로 설명한다.
- README는 DynamoDB 입문자가 해당 주제 전체를 이해할 수 있는 학습 본문이어야 한다.
- full single-table design, GSI, transaction, pagination은 구현하지 않는다.
- Gradle wrapper를 프로젝트와 함께 커밋한다.
- `main`에서 검증 후 기존 형식으로 커밋하고 즉시 push한다.

---

### Task 1: Spring Boot와 DynamoDbClient 프로젝트 골격

**Files:**
- Create: `theory/05-sort-key-prefixes/build.gradle.kts`
- Create: `theory/05-sort-key-prefixes/settings.gradle.kts`
- Create: `theory/05-sort-key-prefixes/docker-compose.yml`
- Create: `theory/05-sort-key-prefixes/gradlew`
- Create: `theory/05-sort-key-prefixes/gradlew.bat`
- Create: `theory/05-sort-key-prefixes/gradle/wrapper/gradle-wrapper.jar`
- Create: `theory/05-sort-key-prefixes/gradle/wrapper/gradle-wrapper.properties`
- Create: `theory/05-sort-key-prefixes/src/main/resources/application.yml`
- Create: `theory/05-sort-key-prefixes/src/main/resources/application-local.yml`
- Create: `theory/05-sort-key-prefixes/src/test/kotlin/com/example/dynamodb/theory/sortkeyprefixes/SortKeyPrefixesApplicationTest.kt`
- Create: `theory/05-sort-key-prefixes/src/main/kotlin/com/example/dynamodb/theory/sortkeyprefixes/SortKeyPrefixesApplication.kt`
- Create: `theory/05-sort-key-prefixes/src/main/kotlin/com/example/dynamodb/theory/sortkeyprefixes/DynamoDbConfig.kt`

**Interfaces:**
- Produces: `SortKeyPrefixesApplication`
- Produces: `DynamoDbProperties(region: String, tableName: String, endpoint: String, useDummyCredentials: Boolean)`
- Produces: Spring bean `DynamoDbClient`

- [ ] **Step 1: Create Gradle, wrapper, resource, and Docker files**

Use the same dependency versions and wrapper files as `theory/04-query-vs-scan`. Set the project name to `theory-05-sort-key-prefixes` and the default table name to `theory_05_sort_key_prefixes`.

`application.yml` must contain:

```yaml
spring:
  application:
    name: theory-05-sort-key-prefixes

app:
  dynamodb:
    region: ${AWS_REGION:ap-northeast-2}
    table-name: ${DYNAMODB_TABLE_NAME:theory_05_sort_key_prefixes}
    endpoint: ${DYNAMODB_ENDPOINT:}
    use-dummy-credentials: ${DYNAMODB_USE_DUMMY_CREDENTIALS:false}
```

- [ ] **Step 2: Write the failing Spring context test**

```kotlin
@SpringBootTest
class SortKeyPrefixesApplicationTest {
    @Autowired
    private lateinit var context: ApplicationContext

    @Test
    fun `Spring context에 DynamoDbClient가 등록된다`() {
        assertThat(context.getBean(DynamoDbClient::class.java)).isNotNull
    }
}
```

- [ ] **Step 3: Run the context test and verify RED**

Run:

```bash
cd theory/05-sort-key-prefixes
./gradlew test --tests '*SortKeyPrefixesApplicationTest'
```

Expected: compilation fails because `SortKeyPrefixesApplication` or DynamoDB configuration is not defined.

- [ ] **Step 4: Add the minimal application marker and client configuration**

`SortKeyPrefixesApplication` uses `@SpringBootApplication` and `@ConfigurationPropertiesScan` without a `main` function.

`DynamoDbConfig` must:

- set `Region.of(properties.region)`
- apply `endpointOverride` only when endpoint is not blank
- use static dummy credentials only when `useDummyCredentials` is true
- otherwise use `DefaultCredentialsProvider`
- return a blocking `DynamoDbClient`

- [ ] **Step 5: Run the context test and verify GREEN**

Run:

```bash
./gradlew test --tests '*SortKeyPrefixesApplicationTest'
```

Expected: one test passes.

---

### Task 2: Key Naming Convention

**Files:**
- Create: `theory/05-sort-key-prefixes/src/test/kotlin/com/example/dynamodb/theory/sortkeyprefixes/ItemKeyFactoryTest.kt`
- Create: `theory/05-sort-key-prefixes/src/main/kotlin/com/example/dynamodb/theory/sortkeyprefixes/ItemKeyFactory.kt`

**Interfaces:**
- Produces: `fun task(taskId: String): String`
- Produces: `fun stats(): String`

- [ ] **Step 1: Write the failing key convention test**

```kotlin
class ItemKeyFactoryTest {
    private val itemKeyFactory = ItemKeyFactory()

    @Test
    fun `task key는 TASK prefix와 taskId를 조합한다`() {
        assertThat(itemKeyFactory.task("task-1")).isEqualTo("TASK#task-1")
    }

    @Test
    fun `stats key는 고정된 STATS 값이다`() {
        assertThat(itemKeyFactory.stats()).isEqualTo("STATS")
    }
}
```

- [ ] **Step 2: Run the test and verify RED**

Run:

```bash
./gradlew test --tests '*ItemKeyFactoryTest'
```

Expected: compilation fails because `ItemKeyFactory` is unresolved.

- [ ] **Step 3: Implement the key factory**

```kotlin
@Component
class ItemKeyFactory {
    fun task(taskId: String): String = "TASK#$taskId"

    fun stats(): String = "STATS"
}
```

Add Korean comments explaining that `TASK#` distinguishes task sort keys and that the prefix is an application convention.

- [ ] **Step 4: Run the test and verify GREEN**

Run:

```bash
./gradlew test --tests '*ItemKeyFactoryTest'
```

Expected: two tests pass.

---

### Task 3: DynamoDB Local Prefix Query

**Files:**
- Create: `theory/05-sort-key-prefixes/src/main/kotlin/com/example/dynamodb/theory/sortkeyprefixes/DynamoDbAttributeMapper.kt`
- Create: `theory/05-sort-key-prefixes/src/main/kotlin/com/example/dynamodb/theory/sortkeyprefixes/SortKeyPrefixService.kt`
- Create: `theory/05-sort-key-prefixes/src/test/kotlin/com/example/dynamodb/theory/sortkeyprefixes/SortKeyPrefixesDynamoDbLocalTest.kt`

**Interfaces:**
- Consumes: `DynamoDbClient`, `DynamoDbProperties`, `ItemKeyFactory`
- Produces: `fun saveDemoItems()`
- Produces: `fun findTaskItems(ownerId: String): List<Map<String, String>>`

- [ ] **Step 1: Write the failing DynamoDB Local test**

Use `@SpringBootTest` with local endpoint and dummy credentials. Skip only when a socket cannot connect to `localhost:8000`.

```kotlin
@Test
fun `TASK prefix Query는 같은 partition에서 task item만 반환한다`() {
    service.saveDemoItems()

    val taskItems = service.findTaskItems("owner-1")

    assertThat(taskItems.map { it.getValue("itemKey") })
        .containsExactly("TASK#task-1", "TASK#task-2")
    assertThat(taskItems.map { it.getValue("itemType") })
        .containsOnly("TASK")
}
```

- [ ] **Step 2: Run the integration test and verify RED**

Run:

```bash
./gradlew test --tests '*SortKeyPrefixesDynamoDbLocalTest'
```

Expected: compilation fails because `SortKeyPrefixService` is unresolved.

- [ ] **Step 3: Implement the mapper and service**

The service creates an on-demand table with:

```text
partition key: ownerId (String)
sort key: itemKey (String)
```

`saveDemoItems()` stores:

```text
owner-1 / TASK#task-1 / itemType=TASK
owner-1 / TASK#task-2 / itemType=TASK
owner-1 / STATS       / itemType=STATS
```

`findTaskItems()` must use this Query request:

```kotlin
QueryRequest.builder()
    .tableName(properties.tableName)
    .keyConditionExpression(
        "ownerId = :ownerId AND begins_with(itemKey, :prefix)",
    )
    .expressionAttributeValues(
        mapOf(
            ":ownerId" to DynamoDbAttributeMapper.s(ownerId),
            ":prefix" to DynamoDbAttributeMapper.s("TASK#"),
        ),
    )
    .build()
```

Return items in DynamoDB sort key order. Do not add a filter expression.

- [ ] **Step 4: Run the integration test and verify GREEN**

Run:

```bash
./gradlew test --tests '*SortKeyPrefixesDynamoDbLocalTest'
```

Expected: one integration test passes and is not skipped.

---

### Task 4: Beginner README and Final Verification

**Files:**
- Modify: `theory/05-sort-key-prefixes/README.md`

**Interfaces:**
- Consumes: the final project behavior and file names from Tasks 1-3
- Produces: the complete learner-facing chapter for topic 05

- [ ] **Step 1: Replace the outline README with the full chapter**

Explain in this order:

1. learning goals
2. table, item, attribute, partition key, sort key recap
3. the three demo items
4. what a sort key prefix and `#` separator mean
5. why the prefix is an application convention rather than a DynamoDB schema declaration
6. how `begins_with(itemKey, :prefix)` works inside `KeyConditionExpression`
7. why this is not `FilterExpression`
8. Spring Boot integration and code flow
9. local execution commands
10. production migration risk and convention tests
11. excluded topics and review questions
12. official AWS Query and sort key documentation links

- [ ] **Step 2: Run the full clean test suite**

Run:

```bash
./gradlew clean test
```

Expected: four tests pass: one context test, two key convention tests, and one DynamoDB Local test.

- [ ] **Step 3: Verify the integration test was not skipped**

Inspect `build/test-results/test/*.xml` and confirm:

```text
tests=4
skipped=0
failures=0
errors=0
```

- [ ] **Step 4: Verify scope and formatting**

Run source checks for `Controller`, `CommandLineRunner`, `ApplicationRunner`, `GlobalSecondaryIndex`, `LastEvaluatedKey`, and `TransactWriteItems`; none should appear. Run `git diff --check`.

- [ ] **Step 5: Commit and push**

```bash
git add .codex/plans/2026-08-09-sort-key-prefixes.md theory/05-sort-key-prefixes
git commit -m "[starryeye, 2026.08.09] - Sort Key Prefixes 이론 프로젝트 추가"
git push origin main
```

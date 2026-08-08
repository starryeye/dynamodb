# Query vs Scan Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 실제 DynamoDB Local에서 Query와 Scan이 같은 결과를 반환하면서 다른 수의 item을 평가하는 과정을 보여주는 Spring Boot 이론 프로젝트를 만든다.

**Architecture:** non-web Spring Boot test가 실행을 시작한다. `QueryVsScanService`는 같은 table에 demo item을 저장하고 Query와 Scan 응답을 `ReadResult`로 변환한다.

**Tech Stack:** Kotlin 2.1.20, Gradle Kotlin DSL, Spring Boot 3.4.5, AWS SDK for Java v2 BOM 2.31.78, JUnit 5, AssertJ, DynamoDB Local

## Global Constraints

- `main`에서 직접 작업한다.
- controller와 runner를 만들지 않는다.
- blocking `DynamoDbClient`를 사용한다.
- 실행은 Spring Boot test로만 확인한다.
- README에 전체 개념을 설명하고 코드에는 DynamoDB 관련 한국어 주석만 둔다.
- pagination, GSI, parallel Scan, consumed capacity 계산은 구현하지 않는다.
- Gradle wrapper를 프로젝트 코드와 함께 커밋한다.
- 완료 후 `[starryeye, 2026.08.09] - Query와 Scan 비교 이론 프로젝트 추가`로 커밋하고 `main`을 push한다.

---

### Task 1: Spring Boot 기반 구성

**Files:**
- Create: `theory/04-query-vs-scan/build.gradle.kts`
- Create: `theory/04-query-vs-scan/settings.gradle.kts`
- Create: `theory/04-query-vs-scan/docker-compose.yml`
- Create: `theory/04-query-vs-scan/gradle/wrapper/*`
- Create: `theory/04-query-vs-scan/gradlew`
- Create: `theory/04-query-vs-scan/gradlew.bat`
- Create: `theory/04-query-vs-scan/src/main/resources/application.yml`
- Create: `theory/04-query-vs-scan/src/main/resources/application-local.yml`
- Create: `theory/04-query-vs-scan/src/main/kotlin/com/example/dynamodb/theory/queryvsscan/QueryVsScanApplication.kt`
- Create: `theory/04-query-vs-scan/src/main/kotlin/com/example/dynamodb/theory/queryvsscan/DynamoDbConfig.kt`
- Test: `theory/04-query-vs-scan/src/test/kotlin/com/example/dynamodb/theory/queryvsscan/QueryVsScanApplicationTest.kt`

**Interfaces:**
- Produces: `QueryVsScanApplication`, `DynamoDbProperties`, `DynamoDbClient` Spring bean

- [ ] **Step 1: build 설정과 실패하는 context test 작성**

```kotlin
@SpringBootTest(
    classes = [QueryVsScanApplication::class],
    properties = [
        "app.dynamodb.endpoint=http://localhost:8000",
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class QueryVsScanApplicationTest {
    @Test
    fun `Spring context가 Query와 Scan용 DynamoDbClient 설정을 포함해 로딩된다`() {
    }
}
```

- [ ] **Step 2: RED 확인**

```bash
cd theory/04-query-vs-scan
./gradlew test --tests '*QueryVsScanApplicationTest'
```

Expected: `QueryVsScanApplication` 미정의로 `compileTestKotlin` 실패.

- [ ] **Step 3: application marker와 DynamoDbClient 설정 구현**

`DynamoDbProperties` 기본값:

```kotlin
data class DynamoDbProperties(
    val region: String = "ap-northeast-2",
    val tableName: String = "theory_04_query_vs_scan",
    val endpoint: String = "",
    val useDummyCredentials: Boolean = false,
)
```

`DynamoDbClient.builder()`에 `Region.of(properties.region)`을 설정한다. `endpoint`가 비어 있지 않으면 `endpointOverride(URI.create(properties.endpoint))`를 적용한다. `useDummyCredentials`가 true면 `StaticCredentialsProvider`와 `AwsBasicCredentials.create("dummy", "dummy")`를 사용하고, false면 `DefaultCredentialsProvider.builder().build()`를 사용한다.

- [ ] **Step 4: GREEN 확인**

```bash
./gradlew test --tests '*QueryVsScanApplicationTest'
```

Expected: 1 test, 0 failures.

---

### Task 2: Query와 Scan 비교 구현

**Files:**
- Create: `theory/04-query-vs-scan/src/main/kotlin/com/example/dynamodb/theory/queryvsscan/DynamoDbAttributeMapper.kt`
- Create: `theory/04-query-vs-scan/src/main/kotlin/com/example/dynamodb/theory/queryvsscan/ItemKeys.kt`
- Create: `theory/04-query-vs-scan/src/main/kotlin/com/example/dynamodb/theory/queryvsscan/ReadResult.kt`
- Create: `theory/04-query-vs-scan/src/main/kotlin/com/example/dynamodb/theory/queryvsscan/QueryVsScanService.kt`
- Test: `theory/04-query-vs-scan/src/test/kotlin/com/example/dynamodb/theory/queryvsscan/QueryVsScanDynamoDbLocalTest.kt`

**Interfaces:**
- Produces: `saveDemoTasks()`, `queryTasksByOwner(ownerId): ReadResult`, `scanTasksByOwner(ownerId): ReadResult`

- [ ] **Step 1: 실패하는 실제 DynamoDB 비교 test 작성**

```kotlin
@Test
fun `Query와 Scan은 같은 task를 반환하지만 평가한 item 수가 다르다`() {
    service.saveDemoTasks()

    val queryResult = service.queryTasksByOwner("owner-1")
    val scanResult = service.scanTasksByOwner("owner-1")

    assertThat(queryResult.items).containsExactlyInAnyOrderElementsOf(scanResult.items)
    assertThat(queryResult.count).isEqualTo(2)
    assertThat(queryResult.scannedCount).isEqualTo(2)
    assertThat(scanResult.count).isEqualTo(2)
    assertThat(scanResult.scannedCount).isEqualTo(5)
    assertThat(scanResult.scannedCount).isGreaterThan(queryResult.scannedCount)
}
```

- [ ] **Step 2: RED 확인**

```bash
./gradlew test --tests '*QueryVsScanDynamoDbLocalTest'
```

Expected: `QueryVsScanService` 미정의로 `compileTestKotlin` 실패.

- [ ] **Step 3: 최소 production code 구현**

```kotlin
data class ReadResult(
    val items: List<Map<String, String>>,
    val count: Int,
    val scannedCount: Int,
)
```

Query request:

```kotlin
QueryRequest.builder()
    .tableName(properties.tableName)
    .keyConditionExpression("ownerId = :ownerId")
    .expressionAttributeValues(mapOf(":ownerId" to DynamoDbAttributeMapper.s(ownerId)))
    .build()
```

Scan request:

```kotlin
ScanRequest.builder()
    .tableName(properties.tableName)
    .filterExpression("ownerId = :ownerId")
    .expressionAttributeValues(mapOf(":ownerId" to DynamoDbAttributeMapper.s(ownerId)))
    .build()
```

demo data는 owner-1 두 건, owner-2 세 건으로 고정한다. table primary key는 `ownerId`와 `itemKey`다.

- [ ] **Step 4: DynamoDB Local에서 GREEN 확인**

```bash
./gradlew test --tests '*QueryVsScanDynamoDbLocalTest'
```

Expected: 1 test, skipped 0, failures 0.

---

### Task 3: README와 최종 검증

**Files:**
- Modify: `theory/04-query-vs-scan/README.md`

- [ ] **Step 1: README 전체 학습 본문 작성**

README는 목표, 기초 용어, Query, Scan, FilterExpression, Count와 ScannedCount, 코드 흐름, 실행 방법, 관찰 포인트, Spring 연동, 운영 관점, 확인 질문 순서로 작성한다.

- [ ] **Step 2: 전체 테스트 실행**

```bash
./gradlew clean test
```

Expected: context test 1건과 연동 test 1건, skipped 0, failures 0.

- [ ] **Step 3: 범위 검증**

```bash
grep -R -nE 'Controller|CommandLineRunner|ApplicationRunner|GlobalSecondaryIndex|LastEvaluatedKey' src || true
git diff --check
```

Expected: 범위 밖 type 출력 없음, whitespace 오류 없음.

- [ ] **Step 4: 커밋과 push**

```bash
git add .codex/specs/2026-08-09-query-vs-scan-design.md \
  .codex/plans/2026-08-09-query-vs-scan.md \
  theory/04-query-vs-scan
git commit -m '[starryeye, 2026.08.09] - Query와 Scan 비교 이론 프로젝트 추가'
git push origin main
```

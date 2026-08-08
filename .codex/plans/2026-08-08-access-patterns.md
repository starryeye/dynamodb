# Access Patterns Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `theory/03-access-patterns`에서 애플리케이션 요구사항을 `PutItem`, `GetItem`, `Query`와 key 조건으로 연결하는 독립 Spring Boot 학습 프로젝트를 만든다.

**Architecture:** non-web Spring Boot test가 실행 흐름의 시작점이다. `DynamoDbConfig`가 blocking `DynamoDbClient`를 bean으로 제공하고, 단일 `AccessPatternService`가 세 access pattern을 DynamoDB Local에서 실행한다.

**Tech Stack:** Kotlin 2.1.20, Gradle Kotlin DSL, Spring Boot 3.4.5, AWS SDK for Java v2 BOM 2.31.78, JUnit 5, AssertJ, DynamoDB Local

## Global Constraints

- 학습자용 전체 설명은 `theory/03-access-patterns/README.md`에 둔다.
- Codex 구현 문서는 `.codex/` 아래에만 둔다.
- controller와 runner를 만들지 않는다.
- blocking `DynamoDbClient`와 test 중심 실행 모델을 사용한다.
- GSI, Scan, 수정, 삭제, conditional write, transaction, pagination은 구현하지 않는다.
- 코드의 DynamoDB 관련 주석은 짧고 분명한 한국어로 작성한다.
- Kotlin, JUnit, 일반적인 Spring 문법에는 불필요한 주석을 붙이지 않는다.
- 02 프로젝트의 현재 미커밋 변경은 스테이징하거나 수정하지 않는다.
- 커밋 메시지는 `[starryeye, 2026.08.08] - 커밋 내용` 형식을 사용한다.

## File Map

- `theory/03-access-patterns/build.gradle.kts`: Kotlin, Spring Boot, AWS SDK, test 의존성
- `theory/03-access-patterns/settings.gradle.kts`: 독립 Gradle 프로젝트 이름과 repository 설정
- `theory/03-access-patterns/docker-compose.yml`: 포트 8000의 in-memory DynamoDB Local
- `theory/03-access-patterns/gradle/wrapper/*`, `gradlew`, `gradlew.bat`: 독립 실행용 Gradle wrapper
- `theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternsApplication.kt`: non-web Spring Boot 진입점
- `theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/DynamoDbConfig.kt`: local/production DynamoDB client bean 설정
- `theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/DynamoDbAttributeMapper.kt`: 문자열 attribute 변환
- `theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/ItemKeys.kt`: task sort key 생성 규칙
- `theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternService.kt`: 세 access pattern 실행
- `theory/03-access-patterns/src/main/resources/application.yml`: 기본 region, table, endpoint 설정
- `theory/03-access-patterns/src/main/resources/application-local.yml`: DynamoDB Local 설정
- `theory/03-access-patterns/src/test/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternsApplicationTest.kt`: Spring context 검증
- `theory/03-access-patterns/src/test/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternDynamoDbLocalTest.kt`: 실제 PutItem, GetItem, Query 검증
- `theory/03-access-patterns/README.md`: 초보자용 전체 학습 본문

---

### Task 1: Spring Boot와 DynamoDbClient 기반 구성

**Files:**
- Create: `theory/03-access-patterns/build.gradle.kts`
- Create: `theory/03-access-patterns/settings.gradle.kts`
- Create: `theory/03-access-patterns/docker-compose.yml`
- Create: `theory/03-access-patterns/gradle/wrapper/gradle-wrapper.jar`
- Create: `theory/03-access-patterns/gradle/wrapper/gradle-wrapper.properties`
- Create: `theory/03-access-patterns/gradlew`
- Create: `theory/03-access-patterns/gradlew.bat`
- Create: `theory/03-access-patterns/src/main/resources/application.yml`
- Create: `theory/03-access-patterns/src/main/resources/application-local.yml`
- Create: `theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternsApplication.kt`
- Create: `theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/DynamoDbConfig.kt`
- Test: `theory/03-access-patterns/src/test/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternsApplicationTest.kt`

**Interfaces:**
- Consumes: 없음
- Produces: `AccessPatternsApplication`, `DynamoDbProperties`, `DynamoDbConfig.dynamoDbClient(properties): DynamoDbClient`

- [ ] **Step 1: Gradle 설정과 실패하는 Spring context 테스트 작성**

`build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example.dynamodb.theory"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(kotlin("reflect"))
    implementation(platform("software.amazon.awssdk:bom:2.31.78"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("software.amazon.awssdk:dynamodb")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
```

`settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "theory-03-access-patterns"
```

Gradle wrapper는 커밋된 01 프로젝트에서 복사한다.

```bash
cp -R theory/01-table-item-key/gradle theory/03-access-patterns/gradle
cp theory/01-table-item-key/gradlew theory/03-access-patterns/gradlew
cp theory/01-table-item-key/gradlew.bat theory/03-access-patterns/gradlew.bat
chmod +x theory/03-access-patterns/gradlew
```

`AccessPatternsApplicationTest.kt`:

```kotlin
package com.example.dynamodb.theory.accesspatterns

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [AccessPatternsApplication::class],
    properties = [
        // 테스트에서 DynamoDbClient bean이 실제 AWS 대신 local endpoint를 바라보게 한다.
        "app.dynamodb.endpoint=http://localhost:8000",
        // 실제 AWS credential 없이 Spring context를 로딩한다.
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class AccessPatternsApplicationTest {
    @Test
    fun `Spring context가 DynamoDbClient 설정을 포함해 로딩된다`() {
    }
}
```

- [ ] **Step 2: 테스트가 필요한 production type 부재로 실패하는지 확인**

Run:

```bash
cd theory/03-access-patterns
./gradlew test --tests '*AccessPatternsApplicationTest'
```

Expected: `compileTestKotlin`이 `Unresolved reference 'AccessPatternsApplication'`으로 실패한다.

- [ ] **Step 3: 최소 Spring Boot application과 DynamoDB 설정 구현**

`AccessPatternsApplication.kt`:

```kotlin
package com.example.dynamodb.theory.accesspatterns

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class AccessPatternsApplication
```

`DynamoDbConfig.kt`:

```kotlin
package com.example.dynamodb.theory.accesspatterns

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

/**
 * DynamoDB 접속 설정이다.
 * local에서는 endpoint와 dummy credential을 사용하고 production에서는 기본 AWS credential 흐름을 사용한다.
 */
@ConfigurationProperties(prefix = "app.dynamodb")
data class DynamoDbProperties(
    val region: String = "ap-northeast-2",
    val tableName: String = "theory_03_access_patterns",
    val endpoint: String = "",
    val useDummyCredentials: Boolean = false,
)

/** Spring이 DynamoDbClient를 관리하므로 service는 접속 설정을 직접 만들지 않는다. */
@Configuration
class DynamoDbConfig {
    @Bean
    fun dynamoDbClient(properties: DynamoDbProperties): DynamoDbClient {
        val builder = DynamoDbClient.builder()
            // 모든 DynamoDB 요청은 설정된 AWS region을 기준으로 서명된다.
            .region(Region.of(properties.region))

        if (properties.endpoint.isNotBlank()) {
            // local profile에서는 AWS endpoint 대신 DynamoDB Local로 요청한다.
            builder.endpointOverride(URI.create(properties.endpoint))
        }

        if (properties.useDummyCredentials) {
            builder.credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")),
            )
        } else {
            // production에서는 IAM role이나 환경 변수 등 AWS 기본 credential 흐름을 사용한다.
            builder.credentialsProvider(DefaultCredentialsProvider.builder().build())
        }

        return builder.build()
    }
}
```

`application.yml`:

```yaml
spring:
  application:
    name: theory-03-access-patterns

app:
  dynamodb:
    region: ${AWS_REGION:ap-northeast-2}
    table-name: ${DYNAMODB_TABLE_NAME:theory_03_access_patterns}
    endpoint: ${DYNAMODB_ENDPOINT:}
    use-dummy-credentials: ${DYNAMODB_USE_DUMMY_CREDENTIALS:false}
```

`application-local.yml`:

```yaml
app:
  dynamodb:
    endpoint: http://localhost:8000
    use-dummy-credentials: true
```

`docker-compose.yml`:

```yaml
services:
  dynamodb:
    image: amazon/dynamodb-local:latest
    command: "-jar DynamoDBLocal.jar -sharedDb -inMemory"
    ports:
      - "8000:8000"
```

- [ ] **Step 4: Spring context 테스트 통과 확인**

Run:

```bash
cd theory/03-access-patterns
./gradlew test --tests '*AccessPatternsApplicationTest'
```

Expected: 1 test, 0 failures, `BUILD SUCCESSFUL`.

- [ ] **Step 5: 기반 구성만 선택해 커밋**

```bash
git add theory/03-access-patterns/build.gradle.kts \
  theory/03-access-patterns/settings.gradle.kts \
  theory/03-access-patterns/docker-compose.yml \
  theory/03-access-patterns/gradle \
  theory/03-access-patterns/gradlew \
  theory/03-access-patterns/gradlew.bat \
  theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternsApplication.kt \
  theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/DynamoDbConfig.kt \
  theory/03-access-patterns/src/main/resources \
  theory/03-access-patterns/src/test/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternsApplicationTest.kt
git commit -m '[starryeye, 2026.08.08] - Access Pattern Spring Boot 기반 추가'
```

---

### Task 2: PutItem, GetItem, Query access pattern 구현

**Files:**
- Create: `theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/DynamoDbAttributeMapper.kt`
- Create: `theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/ItemKeys.kt`
- Create: `theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternService.kt`
- Test: `theory/03-access-patterns/src/test/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternDynamoDbLocalTest.kt`

**Interfaces:**
- Consumes: `DynamoDbClient`, `DynamoDbProperties`
- Produces: `saveTask(ownerId: String, taskId: String, title: String)`, `getTask(ownerId: String, taskId: String): Map<String, String>?`, `findTasksByOwner(ownerId: String): List<Map<String, String>>`

- [ ] **Step 1: 실제 access pattern을 표현하는 실패 테스트 작성**

`AccessPatternDynamoDbLocalTest.kt`:

```kotlin
package com.example.dynamodb.theory.accesspatterns

import java.net.InetSocketAddress
import java.net.Socket
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/** 애플리케이션 요구사항이 실제 DynamoDB operation으로 실행되는 학습 흐름이다. */
@SpringBootTest(
    properties = [
        "app.dynamodb.endpoint=http://localhost:8000",
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class AccessPatternDynamoDbLocalTest {
    @Autowired
    private lateinit var service: AccessPatternService

    @BeforeEach
    fun requireDynamoDbLocal() {
        assumeTrue(canConnectToDynamoDbLocal())
    }

    @Test
    fun `완전한 primary key로 task 한 건을 조회한다`() {
        // 'task를 저장한다'는 요구사항은 PutItem access pattern으로 실행된다.
        service.saveTask("owner-1", "task-1", "Access pattern 정리하기")

        // 'task 한 건을 찾는다'는 요구사항에는 partition key와 sort key가 모두 필요하다.
        val task = service.getTask("owner-1", "task-1")

        assertThat(task).containsEntry("ownerId", "owner-1")
        assertThat(task).containsEntry("itemKey", "TASK#task-1")
        assertThat(task).containsEntry("title", "Access pattern 정리하기")
    }

    @Test
    fun `partition key로 owner의 task 목록을 조회한다`() {
        service.saveTask("owner-1", "task-1", "첫 번째 task")
        service.saveTask("owner-1", "task-2", "두 번째 task")
        service.saveTask("owner-2", "task-1", "다른 owner의 task")

        // 'owner의 task 목록' 요구사항은 ownerId를 partition key 조건으로 사용하는 Query다.
        val tasks = service.findTasksByOwner("owner-1")

        assertThat(tasks).hasSize(2)
        assertThat(tasks).allSatisfy { task ->
            assertThat(task).containsEntry("ownerId", "owner-1")
        }
        assertThat(tasks.map { it["itemKey"] })
            .containsExactly("TASK#task-1", "TASK#task-2")
    }

    private fun canConnectToDynamoDbLocal(): Boolean =
        runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("localhost", 8000), 200)
            }
        }.isSuccess
}
```

- [ ] **Step 2: service 부재로 테스트가 실패하는지 확인**

Run:

```bash
cd theory/03-access-patterns
./gradlew test --tests '*AccessPatternDynamoDbLocalTest'
```

Expected: `compileTestKotlin`이 `Unresolved reference 'AccessPatternService'`로 실패한다.

- [ ] **Step 3: 문자열 attribute와 task key 변환 구현**

`DynamoDbAttributeMapper.kt`:

```kotlin
package com.example.dynamodb.theory.accesspatterns

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

/** 이 입문 예제에서 사용하는 문자열 attribute를 AWS SDK 타입과 Kotlin 타입 사이에서 변환한다. */
object DynamoDbAttributeMapper {
    fun s(value: String): AttributeValue =
        // s는 DynamoDB의 String attribute 값이다.
        AttributeValue.builder().s(value).build()

    fun toStringMap(item: Map<String, AttributeValue>): Map<String, String> =
        item.entries
            .sortedBy { it.key }
            .associate { (name, value) -> name to value.s() }
}
```

`ItemKeys.kt`:

```kotlin
package com.example.dynamodb.theory.accesspatterns

/** 같은 owner partition 안에서 task item을 구분할 sort key 규칙이다. */
object ItemKeys {
    private const val TASK_PREFIX = "TASK#"

    fun task(taskId: String): String = "$TASK_PREFIX$taskId"
}
```

- [ ] **Step 4: 세 access pattern을 실행하는 최소 service 구현**

`AccessPatternService.kt`:

```kotlin
package com.example.dynamodb.theory.accesspatterns

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BillingMode
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType

/**
 * 애플리케이션의 세 요구사항을 PutItem, GetItem, Query access pattern으로 연결한다.
 * 어떤 조건으로 읽을지 먼저 정하고 그 조건을 primary key로 표현하는 것이 이번 주제의 핵심이다.
 */
@Service
class AccessPatternService(
    private val client: DynamoDbClient,
    private val properties: DynamoDbProperties,
) {
    fun saveTask(ownerId: String, taskId: String, title: String) {
        createTableIfMissing()

        // task 저장 요구사항은 primary key를 포함한 item 한 건을 PutItem으로 기록한다.
        client.putItem(
            PutItemRequest.builder()
                .tableName(properties.tableName)
                .item(taskItem(ownerId, taskId, title))
                .build(),
        )
    }

    fun getTask(ownerId: String, taskId: String): Map<String, String>? {
        // task 단건 조회는 partition key와 sort key를 모두 아는 GetItem access pattern이다.
        val response = client.getItem(
            GetItemRequest.builder()
                .tableName(properties.tableName)
                .key(primaryKey(ownerId, taskId))
                .build(),
        )

        return if (response.hasItem()) {
            DynamoDbAttributeMapper.toStringMap(response.item())
        } else {
            null
        }
    }

    fun findTasksByOwner(ownerId: String): List<Map<String, String>> {
        // 목록 조회는 ownerId라는 partition key 조건으로 item collection을 읽는 Query다.
        val response = client.query(
            QueryRequest.builder()
                .tableName(properties.tableName)
                .keyConditionExpression("ownerId = :ownerId")
                .expressionAttributeValues(
                    mapOf(":ownerId" to DynamoDbAttributeMapper.s(ownerId)),
                )
                .build(),
        )

        return response.items().map(DynamoDbAttributeMapper::toStringMap)
    }

    private fun primaryKey(ownerId: String, taskId: String): Map<String, AttributeValue> =
        mapOf(
            // ownerId는 item이 속한 partition을 찾는 partition key다.
            "ownerId" to DynamoDbAttributeMapper.s(ownerId),
            // itemKey는 같은 owner 안에서 task 한 건을 찾는 sort key다.
            "itemKey" to DynamoDbAttributeMapper.s(ItemKeys.task(taskId)),
        )

    private fun taskItem(ownerId: String, taskId: String, title: String): Map<String, AttributeValue> =
        primaryKey(ownerId, taskId) + mapOf(
            "taskId" to DynamoDbAttributeMapper.s(taskId),
            "title" to DynamoDbAttributeMapper.s(title),
        )

    private fun createTableIfMissing() {
        try {
            client.createTable(
                CreateTableRequest.builder()
                    .tableName(properties.tableName)
                    // 입문 예제에서는 capacity 수치를 직접 정하지 않는 on-demand mode를 사용한다.
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .attributeDefinitions(
                        AttributeDefinition.builder()
                            .attributeName("ownerId")
                            .attributeType(ScalarAttributeType.S)
                            .build(),
                        AttributeDefinition.builder()
                            .attributeName("itemKey")
                            .attributeType(ScalarAttributeType.S)
                            .build(),
                    )
                    .keySchema(
                        KeySchemaElement.builder()
                            // HASH는 AWS SDK에서 partition key를 뜻한다.
                            .attributeName("ownerId")
                            .keyType(KeyType.HASH)
                            .build(),
                        KeySchemaElement.builder()
                            // RANGE는 AWS SDK에서 sort key를 뜻한다.
                            .attributeName("itemKey")
                            .keyType(KeyType.RANGE)
                            .build(),
                    )
                    .build(),
            )

            client.waiter().waitUntilTableExists { it.tableName(properties.tableName) }
        } catch (_: ResourceInUseException) {
            // 같은 학습 테스트를 반복 실행할 때 기존 table을 그대로 사용한다.
        }
    }
}
```

- [ ] **Step 5: DynamoDB Local을 준비하고 연동 테스트 통과 확인**

먼저 포트 8000에서 DynamoDB Local이 이미 실행 중인지 확인한다.

```bash
docker ps --filter publish=8000 --format '{{.Names}}'
```

출력이 없을 때만 다음을 실행한다.

```bash
cd theory/03-access-patterns
docker compose up -d
```

그다음 테스트를 실행한다.

```bash
cd theory/03-access-patterns
./gradlew test --tests '*AccessPatternDynamoDbLocalTest'
```

Expected: 2 tests, 0 skipped, 0 failures, `BUILD SUCCESSFUL`.

- [ ] **Step 6: access pattern 구현만 선택해 커밋**

```bash
git add theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/DynamoDbAttributeMapper.kt \
  theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/ItemKeys.kt \
  theory/03-access-patterns/src/main/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternService.kt \
  theory/03-access-patterns/src/test/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternDynamoDbLocalTest.kt
git commit -m '[starryeye, 2026.08.08] - Access Pattern 연동 예제 추가'
```

---

### Task 3: 초보자용 README와 전체 검증

**Files:**
- Modify: `theory/03-access-patterns/README.md`
- Test: `theory/03-access-patterns/src/test/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternsApplicationTest.kt`
- Test: `theory/03-access-patterns/src/test/kotlin/com/example/dynamodb/theory/accesspatterns/AccessPatternDynamoDbLocalTest.kt`

**Interfaces:**
- Consumes: `AccessPatternService`의 세 public method와 완료된 테스트 흐름
- Produces: README 학습 본문과 전체 프로젝트 검증 결과

- [ ] **Step 1: README를 전체 학습 본문으로 교체**

README에는 다음 순서와 내용을 빠짐없이 작성한다.

```markdown
# Access Patterns

이 프로젝트는 DynamoDB table을 설계하기 전에 애플리케이션이 데이터를 어떻게 읽고 쓸지 정리하는 방법을 배운다.

## Access Pattern이란

access pattern은 애플리케이션이 어떤 데이터를 어떤 조건으로 읽거나 쓰는지를 적은 요구사항이다. DynamoDB에서는 SQL 조건을 나중에 자유롭게 추가하기 어렵기 때문에 table과 index를 만들기 전에 access pattern을 먼저 정리한다.

## 다시 보는 기본 용어

- table: item을 저장하는 공간
- item: 저장되는 데이터 한 건
- attribute: item 안의 필드 하나
- primary key: item을 찾고 구분하는 key
- partition key: item이 속할 데이터 묶음을 결정하는 key
- sort key: 같은 partition key 안에서 item 한 건을 구분하는 key

## 이번 프로젝트의 Key

| 역할 | Attribute | 예시 |
| --- | --- | --- |
| partition key | `ownerId` | `owner-1` |
| sort key | `itemKey` | `TASK#task-1` |

## 요구사항을 Operation으로 바꾸기

| 요구사항 | Operation | 필요한 key |
| --- | --- | --- |
| task 저장 | `PutItem` | `ownerId`, `TASK#{taskId}` |
| task 단건 조회 | `GetItem` | `ownerId`, `TASK#{taskId}` |
| owner별 task 목록 조회 | `Query` | `ownerId` |

`PutItem`은 item 한 건을 저장한다. `GetItem`은 primary key 전체를 사용해 정확히 한 item을 읽는다. `Query`는 partition key 조건으로 같은 item collection의 여러 item을 읽는다.

## 이번 프로젝트에서 배우지 않는 것

GSI, Scan, 수정, 삭제, conditional write, transaction, pagination은 뒤의 독립 주제에서 다룬다.

## 코드 흐름

test가 task를 저장하고, 완전한 primary key로 한 건을 읽고, partition key로 owner의 task 목록을 읽는다. `AccessPatternService`의 `saveTask`, `getTask`, `findTasksByOwner`가 각각 `PutItem`, `GetItem`, `Query`를 실행한다.

## 실행 방법

`docker compose up -d`로 DynamoDB Local을 시작한 뒤 `./gradlew test`를 실행한다. DynamoDB Local이 없으면 연동 테스트는 skip되므로 학습할 때는 테스트 결과의 skipped 수가 0인지 확인한다.

## Spring 연동 포인트

`DynamoDbConfig`가 blocking `DynamoDbClient`를 Spring bean으로 등록하고 `AccessPatternService`가 주입받는다. local에서는 localhost endpoint와 dummy credential을 사용하고 production에서는 endpoint override를 제거하고 IAM 기반 기본 credential 흐름을 사용한다.

## 운영 관점

새 조회 기능을 구현하기 전에 현재 table primary key나 index key로 `GetItem` 또는 `Query`가 가능한지 확인한다. 기존 key로 표현할 수 없다는 이유만으로 일반 애플리케이션 경로에 `Scan`을 추가하지 않고 access pattern과 key 설계를 다시 검토한다.

## 확인 질문

- access pattern을 table 설계보다 먼저 정리하는 이유는 무엇인가?
- 단건 조회에 partition key와 sort key가 모두 필요한 이유는 무엇인가?
- owner별 목록 조회가 `GetItem`이 아니라 `Query`인 이유는 무엇인가?
- 새 조회 요구사항이 현재 key로 표현되지 않으면 무엇을 다시 검토해야 하는가?
```

문장을 그대로 복사하는 데 그치지 않고, 각 operation의 요청 코드와 테스트 assertion을 찾아갈 수 있도록 실제 파일명과 메서드명을 연결한다. 설명을 늘리더라도 이번 범위 밖 개념의 상세 설명은 추가하지 않는다.

- [ ] **Step 2: 전체 테스트를 깨끗한 상태에서 실행**

Run:

```bash
cd theory/03-access-patterns
./gradlew clean test
```

Expected: 3 tests, 0 skipped, 0 failures, `BUILD SUCCESSFUL`.

- [ ] **Step 3: XML 결과에서 skip과 failure가 없는지 확인**

Run:

```bash
grep -H 'testsuite' build/test-results/test/TEST-*.xml
```

Expected: context test 1건과 DynamoDB Local test 2건, 모든 suite가 `skipped="0" failures="0" errors="0"`.

- [ ] **Step 4: controller, runner, 범위 밖 operation이 없는지 확인**

Run:

```bash
find src -type f -print
grep -R -nE 'Controller|CommandLineRunner|ApplicationRunner|ScanRequest|TransactWrite|GlobalSecondaryIndex' src || true
```

Expected: application, config, mapper, key, service, 두 test와 resource만 존재하고 두 번째 명령은 출력이 없다.

- [ ] **Step 5: README와 계획 문서만 선택해 커밋**

```bash
git add theory/03-access-patterns/README.md .codex/plans/2026-08-08-access-patterns.md
git commit -m '[starryeye, 2026.08.08] - Access Pattern 학습 문서 보강'
```

- [ ] **Step 6: 최종 변경 범위 확인**

Run:

```bash
git status --short --branch
git log -4 --pretty=format:'%h %s'
```

Expected: 03 관련 변경은 모두 커밋되어 있고, 이전부터 존재한 02 미커밋 변경만 남는다. 최근 커밋은 모두 `[starryeye, 2026.08.08] - ...` 형식이다.

# Stage 2 - DynamoDB + Spring MVC

Stage 2는 Stage 1과 같은 API, service method, DTO 이름, domain behavior를 유지하면서 persistence를 MySQL/JPA에서 DynamoDB로 바꾼다.

## 먼저 읽을 이론

Stage 2를 구현하기 전에 다음 이론 문서를 먼저 읽는다.

- [Access Pattern](../../theory/03-access-patterns/)
- [Query vs Scan](../../theory/04-query-vs-scan/)
- [Sort Key Prefixes](../../theory/05-sort-key-prefixes/)
- [Single-table Key Design](../../theory/06-single-table-key-design/)
- [GSI Basics](../../theory/07-gsi-basics/)
- [GSI Consistency](../../theory/08-gsi-consistency/)
- [LastEvaluatedKey](../../theory/09-last-evaluated-key/)
- [API Cursor](../../theory/10-api-cursor/)
- [Conditional Put](../../theory/11-conditional-put/)
- [Versioned Update](../../theory/12-versioned-update/)
- [Transaction Basics](../../theory/13-transaction-basics/)
- [Transaction Failures](../../theory/14-transaction-failures/)

## Milestone

Stage 2는 한 번에 구현하지 않고 다음 순서로 진행한다.

| Milestone | 목표 | 주요 DynamoDB 개념 |
| --- | --- | --- |
| 2A | DynamoDB Local, client, table initializer 구성 | endpoint override, local credential, table 생성 |
| 2B | task 단건 생성/조회 구현 | `PutItem`, `GetItem`, primary key |
| 2C | owner 기준 task 목록 기본 조회 구현 | `Query`, partition key |
| 2D | 생성일 역순 목록을 위한 GSI 적용 | GSI, `createdAtTaskId`, eventual consistency |
| 2E | cursor pagination 구현 | `LastEvaluatedKey`, `ExclusiveStartKey`, Base64 cursor |
| 2F | 수정/완료/삭제의 version 검증 구현 | condition expression, optimistic locking |
| 2G | `OwnerTaskStats`와 transaction 구현 | `TransactWriteItems`, all-or-nothing write |
| 2H | 예외 매핑과 repository integration test 보강 | `ConditionalCheckFailedException`, `TransactionCanceledException` |

## 핵심 원칙

- Spring Data JPA를 사용하지 않는다.
- third-party Spring Data DynamoDB abstraction을 사용하지 않는다.
- AWS SDK for Java 2.x를 직접 사용한다.
- DynamoDB의 차이를 숨기지 말고 코드와 주석에서 드러낸다.

## Gradle 의존성

필수 의존성:

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `software.amazon.awssdk:dynamodb`
- `software.amazon.awssdk:dynamodb-enhanced`
- `assertj`
- `junit-jupiter`
- `mockito-kotlin` 또는 `mockk`

## Docker Compose

DynamoDB Local 실행 환경을 제공한다.

- image: `amazon/dynamodb-local`
- port: `8000`
- shared DB mode
- 필요하면 persistent volume 사용

## Spring Profile

### local

`application-local.yml` 요구사항:

- `app.aws.region=ap-northeast-2`
- `app.aws.dynamodb.endpoint=http://localhost:8000`
- `app.aws.dynamodb.table-name=tasks`
- local에서만 dummy credential 사용

### prod

`application-prod.yml` 요구사항:

- region은 환경 변수 또는 고정 region으로 설정
- table name은 환경 변수에서 주입
- `endpointOverride` 사용 금지
- dummy credential 사용 금지
- `DefaultCredentialsProvider` 또는 IAM role 사용

## DynamoDB Table Design

테이블 이름:

```text
tasks
```

Primary key:

- partition key: `ownerId`
- sort key: `itemKey`

single-table style을 사용하여 `Task`와 `OwnerTaskStats`를 같은 테이블에 저장한다. 이렇게 하면 transaction 예제를 명확하게 보여줄 수 있다.

## Access Pattern Mapping

| 기능 | DynamoDB operation | Table 또는 Index | Key |
| --- | --- | --- | --- |
| task 생성 | `TransactWriteItems` | `tasks` | `ownerId`, `TASK#{taskId}` |
| task 단건 조회 | `GetItem` | `tasks` | `ownerId`, `TASK#{taskId}` |
| owner의 task 목록 조회 | `Query` | `OwnerCreatedAtIndex` | `ownerId`, `createdAtTaskId` |
| task 제목 수정 | `UpdateItem` 또는 `TransactWriteItems` | `tasks` | `ownerId`, `TASK#{taskId}` |
| task 완료 처리 | `TransactWriteItems` | `tasks` | `TASK` item과 `STATS` item |
| task 삭제 | `TransactWriteItems` | `tasks` | `TASK` item과 `STATS` item |
| owner 통계 조회 | `GetItem` | `tasks` | `ownerId`, `STATS` |

## Item Key Design

### Task item

- `ownerId = actual ownerId`
- `itemKey = TASK#{taskId}`
- `entityType = TASK`
- `taskId`
- `title`
- `status`
- `version`
- `createdAt`
- `updatedAt`
- `createdAtTaskId`

### OwnerTaskStats item

- `ownerId = actual ownerId`
- `itemKey = STATS`
- `entityType = OWNER_TASK_STATS`
- `totalCount`
- `todoCount`
- `doneCount`
- `version`
- `updatedAt`

## GSI

GSI 이름:

```text
OwnerCreatedAtIndex
```

Key:

- partition key: `ownerId`
- sort key: `createdAtTaskId`

요구사항:

- `Task` item만 `createdAtTaskId`를 가진다.
- `createdAtTaskId`는 deterministic descending pagination이 가능해야 한다.
- 필요하면 inverted timestamp 또는 `scanIndexForward=false`를 사용한다.
- GSI query는 eventual consistency라는 점을 Stage README와 코드 주석에서 설명한다.
- 생성 직후 `getTask`와 `listTasks` 결과가 짧은 시간 동안 다를 수 있음을 학습 포인트로 남긴다.

## 페이지네이션

`listTasks`는 반드시 `OwnerCreatedAtIndex`를 `Query`한다.

요구사항:

- application read path에서 `Scan`을 사용하지 않는다.
- `LastEvaluatedKey`를 `nextCursor`의 원본으로 사용한다.
- `LastEvaluatedKey`를 Base64 JSON으로 인코딩한다.
- cursor를 decode하여 `ExclusiveStartKey`로 전달한다.
- DynamoDB pagination은 offset 기반이 아니라 key 기반임을 주석으로 설명한다.
- cursor decode 실패 또는 cursor의 `ownerId`가 path variable과 다른 경우 `InvalidCursorException`으로 처리한다.

## Configuration

필요한 구성 요소:

- `DynamoDbConfig`
- `DynamoDbEnhancedClient` bean
- 필요하면 `DynamoDbTable` bean
- `DynamoDbTableInitializer`

`DynamoDbTableInitializer` 요구사항:

- local profile에서만 동작한다.
- 테이블과 GSI가 없으면 생성할 수 있다.
- production에서는 애플리케이션 시작 시 테이블을 생성하지 않는다.

## Repository 구조

DynamoDB persistence model:

- `TaskDynamoItem`
- `OwnerTaskStatsDynamoItem`

Application port 구현체:

- `DynamoTaskRepository implements TaskRepository`
- `DynamoOwnerTaskStatsRepository implements OwnerTaskStatsRepository`

별도 repository port를 유지한다면 Stage 1과 메서드명을 최대한 비슷하게 유지한다.

## Transaction과 Concurrency

version attribute:

- `TaskDynamoItem`에 `version` 추가
- `OwnerTaskStatsDynamoItem`에 `version` 추가

Enhanced Client write method를 사용할 때는 가능하면 `@DynamoDbVersionAttribute`를 사용한다.

transaction API와 explicit condition write에서는 condition expression을 명시한다.

조건식 요구사항:

- duplicate create 방지: `attribute_not_exists(ownerId) AND attribute_not_exists(itemKey)`
- stale update 방지: `version = :expectedVersion`
- 이미 완료된 task 방지: `status = :todo`
- delete 보호: `version = :expectedVersion`

`TransactWriteItems`를 사용해야 하는 흐름:

- `createTask` + `OwnerTaskStats` 증가
- `completeTask` + `OwnerTaskStats` counter 이동
- `deleteTask` + `OwnerTaskStats` counter 감소

추가 요구사항:

- transactional update에서는 version 증가 동작을 명시적으로 읽기 쉽게 표현한다.
- retry 가능성을 고려해 transaction write request에 idempotency token을 사용한다.
- `ConditionalCheckFailedException`은 작업 성격에 따라 `TaskVersionConflictException` 또는 `DuplicateTaskException`으로 매핑한다.
- `TransactionCanceledException`은 가능한 경우 정확한 domain exception으로 매핑하고, 불가능하면 `TaskTransactionFailedException`으로 매핑한다.

예외 매핑 기준:

| 실패 상황 | DynamoDB 예외 | Domain exception |
| --- | --- | --- |
| 이미 존재하는 task 생성 | `ConditionalCheckFailedException` 또는 transaction cancellation reason | `DuplicateTaskException` |
| 오래된 `expectedVersion`으로 수정/삭제 | `ConditionalCheckFailedException` 또는 transaction cancellation reason | `TaskVersionConflictException` |
| 이미 `DONE`인 task 완료 | transaction cancellation reason | `TaskAlreadyCompletedException` |
| cancellation reason으로 정확히 구분 불가 | `TransactionCanceledException` | `TaskTransactionFailedException` |

## 코드에서 확인할 개념

구현 코드를 읽을 때 다음 개념이 어디에서 드러나는지 확인한다.

- DynamoDB에는 JOIN이 없다.
- DynamoDB 모델링은 access pattern에서 시작한다.
- `Query`에는 partition key가 필요하며 application read path에서는 `Scan`을 피한다.
- DynamoDB에는 MySQL의 `SELECT FOR UPDATE` 같은 row lock이 없다.
- lost update 방지는 conditional write와 optimistic locking으로 처리한다.
- 여러 item의 all-or-nothing write에는 `TransactWriteItems`를 사용한다.
- DynamoDB pagination은 offset이 아니라 `LastEvaluatedKey`를 사용한다.
- GSI는 별도의 query path이며 자체 key design이 필요하다.
- transaction은 capacity cost에 영향을 준다.
- transaction conflict는 `TransactionCanceledException`으로 나타날 수 있다.
- conditional write conflict는 `ConditionalCheckFailedException`으로 나타날 수 있다.
- production에서는 `endpointOverride`와 dummy credential을 사용하지 않는다.
- AWS에서는 IAM permission이 필요하다.
- production table은 IaC로 생성한다.

## Production Notes

- IAM role 또는 default AWS credential chain을 사용한다.
- access key를 하드코딩하지 않는다.
- production table을 application startup에서 생성하지 않는다.
- hot partition을 피한다.
- online request path에서 `Scan`을 피한다.
- throttling, latency, consumed capacity, error rate를 모니터링한다.
- PITR, backup, deletion protection, alarm을 적절히 사용한다.
- table과 index ARN에 대해 least-privilege IAM permission을 정의한다.
- capacity mode는 local에서는 단순하게 두되 production README에서는 on-demand/provisioned 선택 기준을 설명한다.
- GSI write amplification과 transaction cost를 설명한다.

## Stage 1과의 비교 포인트

| 주제 | Stage 1 MySQL/JPA | Stage 2 DynamoDB |
| --- | --- | --- |
| Schema design | table과 relation 중심 | access pattern과 key design 중심 |
| Query model | SQL, index, where/order by | partition key 기반 `Query`, GSI |
| Transaction model | `@Transactional` commit/rollback | `TransactWriteItems` |
| Locking model | `@Version`, 필요 시 pessimistic lock | condition expression, optimistic locking |
| Pagination model | `createdAt + taskId` cursor | `LastEvaluatedKey` cursor |
| Production config | datasource, HikariCP, Flyway | IAM, table/index ARN, capacity/latency monitoring |

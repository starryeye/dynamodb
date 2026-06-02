# Practice Projects

이 디렉터리는 같은 `Task` 애플리케이션을 세 단계로 변환하며 비교 학습하는 실습 트랙이다.

```text
Stage 1: MySQL + Spring MVC + JPA
Stage 2: DynamoDB + Spring MVC
Stage 3: DynamoDB + Spring WebFlux
```

Stage 1에서 RDB/JPA 기준선을 만들고, Stage 2에서 persistence model을 DynamoDB로 바꾼다. Stage 3에서는 DynamoDB 설계는 유지한 채 request 처리와 AWS SDK 호출 방식을 reactive/non-blocking으로 바꾼다.

## 공통 목표

세 Stage는 다음 요소를 최대한 동일하게 유지한다.

- 동일한 비즈니스 기능
- 동일한 도메인
- 동일한 REST API 동작
- 동일한 계층형 아키텍처
- 최대한 1:1로 비교 가능한 클래스명과 메서드명

## 공통 도메인

### Task

필드:

- `taskId: String`
- `ownerId: String`
- `title: String`
- `status: TaskStatus`
- `version: Long`
- `createdAt: Instant`
- `updatedAt: Instant`

### TaskStatus

값:

- `TODO`
- `DONE`

### OwnerTaskStats

필드:

- `ownerId: String`
- `totalCount: Long`
- `todoCount: Long`
- `doneCount: Long`
- `version: Long`
- `updatedAt: Instant`

## 공통 REST API

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/owners/{ownerId}/tasks` | 할 일 생성 |
| `GET` | `/owners/{ownerId}/tasks/{taskId}` | 할 일 단건 조회 |
| `GET` | `/owners/{ownerId}/tasks?size=10&cursor=` | 할 일 목록 커서 페이지 조회 |
| `PATCH` | `/owners/{ownerId}/tasks/{taskId}/title` | 제목 수정 |
| `PATCH` | `/owners/{ownerId}/tasks/{taskId}/complete` | 완료 처리 |
| `DELETE` | `/owners/{ownerId}/tasks/{taskId}` | 삭제 |
| `GET` | `/owners/{ownerId}/task-stats` | 소유자별 통계 조회 |

## Request DTO

### CreateTaskRequest

- `taskId: String?`
- `title: String`

### UpdateTaskTitleRequest

- `title: String`
- `expectedVersion: Long`

### CompleteTaskRequest

- `expectedVersion: Long`

### DeleteTaskRequest 또는 query parameter

- `expectedVersion: Long`

모든 request DTO에는 적절한 validation annotation을 추가한다.

## Response DTO

### TaskResponse

- `taskId`
- `ownerId`
- `title`
- `status`
- `version`
- `createdAt`
- `updatedAt`

### TaskPageResponse

- `items: List<TaskResponse>`
- `nextCursor: String?`
- `hasNext: Boolean`

### OwnerTaskStatsResponse

- `ownerId`
- `totalCount`
- `todoCount`
- `doneCount`
- `version`
- `updatedAt`

## 비즈니스 규칙

- 할 일을 생성하면 `TODO` 상태의 `Task`가 만들어진다.
- 할 일 생성 시 `OwnerTaskStats.totalCount`와 `OwnerTaskStats.todoCount`를 원자적으로 증가시킨다.
- 중복 `Task` 생성은 안전하게 실패해야 하며 기존 데이터를 덮어쓰면 안 된다.
- 제목 수정에는 `expectedVersion`이 필요하다.
- 완료 처리에는 `expectedVersion`이 필요하다.
- `TODO` 상태의 `Task`를 완료하면 `DONE` 상태가 된다.
- 완료 처리 시 `OwnerTaskStats.todoCount`를 감소시키고 `doneCount`를 증가시킨다.
- 이미 `DONE` 상태인 `Task`를 다시 완료하려 하면 실패한다.
- 삭제에는 `expectedVersion`이 필요하다.
- `TODO` 상태의 `Task` 삭제 시 `totalCount`와 `todoCount`를 감소시킨다.
- `DONE` 상태의 `Task` 삭제 시 `totalCount`와 `doneCount`를 감소시킨다.
- 오래된 `expectedVersion`으로 수정하거나 삭제하면 `TaskVersionConflictException`을 발생시킨다.

## 페이지네이션 규칙

모든 Stage는 cursor pagination을 사용한다. offset/page-number pagination은 사용하지 않는다.

- Stage 1 MySQL: `createdAt + taskId` cursor 사용
- Stage 2 DynamoDB: DynamoDB `LastEvaluatedKey`를 Base64 JSON cursor로 인코딩
- Stage 3 DynamoDB: Stage 2와 같은 cursor 방식 유지

페이지네이션 관련 메서드명은 세 Stage에서 최대한 동일하게 유지한다.

## 공통 계층 구조

세 Stage는 다음 계층 구조를 최대한 비슷하게 유지한다.

```text
domain/
  Task
  TaskStatus
  OwnerTaskStats

application/
  TaskService 또는 ReactiveTaskService
  TaskRepository 또는 ReactiveTaskRepository
  OwnerTaskStatsRepository 또는 ReactiveOwnerTaskStatsRepository
  command classes

adapter/in/web/
  TaskController
  request DTOs
  response DTOs

adapter/out/persistence/
  JPA 또는 DynamoDB 구현체

config/

support/
  CursorEncoder
  ClockHolder 또는 TimeProvider
  IdGenerator

exception/
  GlobalExceptionHandler
  domain exceptions
```

## 공통 메서드명

모든 Stage는 다음 메서드명을 유지한다.

- `createTask`
- `getTask`
- `listTasks`
- `updateTaskTitle`
- `completeTask`
- `deleteTask`
- `getOwnerTaskStats`

Stage 3에서는 메서드명은 유지하되 반환 타입을 reactive type으로 바꾼다.

- `Mono<TaskResponse>`
- `Mono<TaskPageResponse>`
- `Mono<OwnerTaskStatsResponse>`
- `Mono<Void>`

## 공통 예외

- `TaskNotFoundException`
- `TaskVersionConflictException`
- `DuplicateTaskException`
- `TaskAlreadyCompletedException`
- `TaskTransactionFailedException`
- `InvalidCursorException`

## 공통 코드 관찰 포인트

세 Stage를 읽을 때 다음 차이를 비교한다.

- 같은 controller method가 어떤 service method를 호출하는지 비교한다.
- 같은 service method가 어떤 repository port를 사용하는지 비교한다.
- 같은 비즈니스 규칙이 JPA transaction과 DynamoDB transaction에서 어떻게 표현되는지 비교한다.
- 같은 cursor pagination이 MySQL query와 DynamoDB `LastEvaluatedKey`에서 어떻게 달라지는지 비교한다.

코드 구조에서는 다음 원칙을 확인한다.

- constructor injection을 사용한다.
- domain model은 persistence annotation에 의존하지 않는다.
- DTO는 domain model과 분리한다.
- persistence model은 Stage별로 분리한다.
- 전역 예외 처리기를 둔다.
- persistence model 차이가 드러나는 곳에는 학습용 주석을 둔다.
- Kotlin data class, null-safety, immutable value를 자연스럽게 활용한다.
- service는 business rule orchestration에 집중하고 controller와 persistence 세부 사항을 섞지 않는다.
- repository port는 각 Stage의 persistence 차이를 이해할 수 있을 만큼만 추상화한다.
- mapper 함수는 읽기 쉬운 확장 함수 또는 작은 factory method로 둔다.
- exception mapping은 한 곳에서 일관되게 처리한다.
- 테스트하기 어려운 정적 시간/ID 생성은 `TimeProvider`, `IdGenerator` 같은 작은 support component로 분리한다.

분리해서 볼 persistence model:

- MySQL: `TaskJpaEntity`, `OwnerTaskStatsJpaEntity`
- DynamoDB: `TaskDynamoItem`, `OwnerTaskStatsDynamoItem`

## 공통 테스트 원칙

모든 Stage는 다음 도구와 스타일을 사용한다.

- JUnit 5
- AssertJ
- BDD-style given/when/then
- Mockito Kotlin 또는 MockK
- Kotlin backtick style의 읽기 쉬운 테스트 이름

모든 Stage에서 검증해야 하는 동작:

- `createTask`는 `TODO` task를 생성한다.
- `createTask`는 `OwnerTaskStats.totalCount`와 `todoCount`를 증가시킨다.
- 중복 task 생성은 실패한다.
- `getTask`는 `ownerId`와 `taskId`로 task를 반환한다.
- `listTasks`는 cursor pagination 결과를 반환한다.
- 더 많은 item이 있으면 `listTasks`는 `nextCursor`를 반환한다.
- `updateTaskTitle`은 일치하는 `expectedVersion`으로 성공한다.
- `updateTaskTitle`은 오래된 `expectedVersion`으로 실패한다.
- `completeTask`는 일치하는 `expectedVersion`으로 성공한다.
- 이미 완료된 task에 대한 `completeTask`는 실패한다.
- `completeTask`는 `OwnerTaskStats`를 원자적으로 갱신한다.
- `deleteTask`는 일치하는 `expectedVersion`으로 성공한다.
- `deleteTask`는 오래된 `expectedVersion`으로 실패한다.
- transaction 실패 시 partial update가 남지 않는다.
- 잘못된 cursor는 `InvalidCursorException`을 발생시킨다.

## Stage별 테스트 포인트

Stage 1:

- mocked `TaskRepository`와 `OwnerTaskStatsRepository`를 사용한 service unit test
- mocked service를 사용한 controller test
- Docker Compose MySQL을 사용하는 repository integration test
- JPA `@Version` conflict behavior test
- `@Transactional` rollback behavior test
- pessimistic locking 교육용 테스트 또는 sample

Stage 2:

- mocked repository를 사용한 service unit test
- mocked service를 사용한 controller test
- DynamoDB Local을 사용하는 repository integration test
- `ConditionalCheckFailedException` mapping test
- `TransactionCanceledException` mapping test
- `TransactWriteItems` 실패 시 partial update가 남지 않는지 검증
- `LastEvaluatedKey` cursor encode/decode test

Stage 3:

- mocked `ReactiveTaskRepository`를 사용한 service unit test
- mocked service와 `WebTestClient`를 사용한 WebFlux controller test
- DynamoDB Local을 사용하는 repository integration test
- `StepVerifier` 사용
- `StepVerifier` assertion 내부에서 AssertJ 사용
- `ConditionalCheckFailedException` reactive mapping test
- `TransactionCanceledException` reactive mapping test
- application code에서 `block()`을 요구하지 않는 구조 검증

## Stage별 산출물

각 Stage는 다음 파일과 디렉터리를 포함한다.

- `settings.gradle.kts`
- `build.gradle.kts`
- `docker-compose.yml`
- `application-local.yml`
- `application-prod.yml`
- `README.md`
- source code
- tests

## Stage별 학습

- [Stage 1 - MySQL + Spring MVC + JPA](./stage1-mysql-mvc/)
- [Stage 2 - DynamoDB + Spring MVC](./stage2-dynamodb-mvc/)
- [Stage 3 - DynamoDB + Spring WebFlux](./stage3-dynamodb-webflux/)

## 최종 비교

| 주제 | Stage 1 | Stage 2 | Stage 3 |
| --- | --- | --- | --- |
| 데이터 모델링 출발점 | entity와 relation | access pattern과 key design | Stage 2와 동일 |
| 저장소 | MySQL table | DynamoDB single table | DynamoDB single table |
| 주요 key | PK, unique key, index | partition key, sort key, GSI | Stage 2와 동일 |
| 단건 조회 | SQL/JPA repository | `GetItem` | async `GetItem` |
| 목록 조회 | SQL cursor query | GSI `Query` | async GSI `Query` |
| pagination | `createdAt + taskId` cursor | `LastEvaluatedKey` cursor | Stage 2와 동일 |
| 동시성 | JPA `@Version` | condition expression | condition expression |
| 다중 item 원자성 | `@Transactional` | `TransactWriteItems` | async `TransactWriteItems` |
| HTTP stack | Spring MVC | Spring MVC | Spring WebFlux |
| DynamoDB client | 없음 | `DynamoDbClient` | `DynamoDbAsyncClient` |

## 회고 질문

- 새로운 API를 추가할 때 Stage 1과 Stage 2에서 각각 무엇을 먼저 바꾸는가?
- Stage 2에서 `Scan`이 필요해진다면 어떤 설계를 다시 봐야 하는가?
- `getTask`와 `listTasks`의 consistency가 달라질 수 있는 이유는 무엇인가?
- 모든 write에 transaction을 쓰지 않고 필요한 곳에만 쓰는 이유는 무엇인가?
- Stage 3에서 DynamoDB table design이 바뀌지 않는 이유는 무엇인가?

# 공통 요구사항

## 프로젝트 목표

초급자가 Spring Boot 애플리케이션을 MySQL + JPA에서 DynamoDB로, 다시 Spring MVC에서 WebFlux로 옮길 때 무엇이 바뀌는지 비교하며 배울 수 있는 학습 프로젝트를 만든다.

세 Stage는 다음 요소를 최대한 동일하게 유지한다.

- 동일한 비즈니스 기능
- 동일한 도메인
- 동일한 REST API 동작
- 동일한 계층형 아키텍처
- 최대한 1:1로 비교 가능한 클래스명과 메서드명

구현 원칙은 다음과 같다.

- Kotlin, Gradle Kotlin DSL, Spring Boot 사용
- Docker Compose로 로컬 인프라 제공
- 계층형 아키텍처와 SOLID 원칙 준수
- 초급자가 읽기 쉬운 단순한 코드 유지
- Kotlin과 Spring Boot best practice를 우선한다.
- 중요한 persistence 차이를 과도한 추상화로 숨기지 않기
- 교육적인 주석은 추가하되 과하게 noisy하게 만들지 않기

실습 프로젝트는 Gradle multi-module이 아니라 `practice/` 아래의 세 개 독립 Gradle 프로젝트로 작성한다. 공통 코드를 별도 shared module로 빼지 않고, 비교 학습을 위해 각 Stage 안에 필요한 코드를 명시적으로 둔다.

이론 프로젝트는 `theory/` 아래에 주제별 독립 프로젝트로 작성한다. 각 이론 프로젝트는 `docs/theory/*.md`와 1:1로 대응하며, 해당 개념을 확인하는 최소 코드, 테스트, 스크립트, README를 가진다.

## 패키지

```text
com.example.taskapp
```

## 도메인

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

## 공통 코드 요구사항

- constructor injection을 사용한다.
- domain model은 persistence annotation에 의존하지 않는다.
- DTO는 domain model과 분리한다.
- persistence model은 Stage별로 분리한다.
- 전역 예외 처리기를 추가한다.
- persistence model 차이가 드러나는 곳에는 교육용 주석을 추가한다.
- Kotlin data class, null-safety, immutable value를 자연스럽게 활용한다.
- service는 business rule orchestration에 집중하고 controller와 persistence 세부 사항을 섞지 않는다.
- repository port는 각 Stage의 persistence 차이를 이해할 수 있을 만큼만 추상화한다.
- mapper 함수는 읽기 쉬운 확장 함수 또는 작은 factory method로 둔다.
- exception mapping은 한 곳에서 일관되게 처리한다.
- 테스트하기 어려운 정적 시간/ID 생성은 `TimeProvider`, `IdGenerator` 같은 작은 support component로 분리한다.

분리해야 하는 persistence model:

- MySQL: `TaskJpaEntity`, `OwnerTaskStatsJpaEntity`
- DynamoDB: `TaskDynamoItem`, `OwnerTaskStatsDynamoItem`

## 구현 방향

세 Stage를 읽는 사람이 다음 방식으로 비교할 수 있어야 한다.

- 같은 controller method가 어떤 service method를 호출하는지 비교한다.
- 같은 service method가 어떤 repository port를 사용하는지 비교한다.
- 같은 비즈니스 규칙이 JPA transaction과 DynamoDB transaction에서 어떻게 표현되는지 비교한다.
- 같은 cursor pagination이 MySQL query와 DynamoDB `LastEvaluatedKey`에서 어떻게 달라지는지 비교한다.

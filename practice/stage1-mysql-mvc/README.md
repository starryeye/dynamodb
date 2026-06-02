# Stage 1 - MySQL + Spring MVC + JPA

Stage 1은 전통적인 관계형 데이터베이스 기반 Spring Boot 애플리케이션의 기준선을 만든다.

## 기술 스택

- Kotlin
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- MySQL 8
- Flyway
- Docker Compose

## Gradle 의존성

필수 의존성:

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `mysql-connector-j`
- `flyway-core`
- `flyway-mysql`
- `assertj`
- `junit-jupiter`
- `mockito-kotlin` 또는 `mockk`

## Docker Compose

MySQL 로컬 실행 환경을 제공한다.

- image: `mysql:8`
- database: `taskdb`
- user: `app`
- password: `app`
- port: `3306`
- healthcheck 포함

## Spring Profile

### local

`application-local.yml` 요구사항:

- localhost MySQL datasource 사용
- Flyway 활성화
- `spring.jpa.hibernate.ddl-auto=validate`
- SQL logging은 local에서만 선택적으로 활성화
- `spring.jpa.open-in-view=false`

### prod

`application-prod.yml` 요구사항:

- datasource URL, user, password는 환경 변수에서 주입
- `spring.jpa.hibernate.ddl-auto=validate`
- `spring.jpa.open-in-view=false`
- SQL logging 비활성화
- HikariCP production setting 명시
- Actuator health와 metrics를 안전하게 활성화

HikariCP에서 명시할 설정:

- `maximum-pool-size`
- `minimum-idle`
- `connection-timeout`
- `validation-timeout`
- `max-lifetime`

## Flyway Migration

생성할 테이블:

- `tasks`
- `owner_task_stats`

`tasks` 요구사항:

- `owner_id + task_id` unique key
- `owner_id + created_at + task_id` index
- 해당 index는 cursor pagination query pattern을 위해 필요하다.

`owner_task_stats` 요구사항:

- `owner_id` unique key

## JPA Model

JPA entity:

- `TaskJpaEntity`
- `OwnerTaskStatsJpaEntity`

요구사항:

- 두 entity 모두 `@Version`을 사용한다.
- `createdAt`, `updatedAt` column을 가진다.
- JPA entity는 persistence adapter 밖으로 노출하지 않는다.
- domain model과 persistence model을 분리한다.

## Repository 구조

Spring Data repository:

- `SpringDataTaskJpaRepository`
- `SpringDataOwnerTaskStatsJpaRepository`

Application port 구현체:

- `JpaTaskRepository implements TaskRepository`
- `JpaOwnerTaskStatsRepository implements OwnerTaskStatsRepository`

## 페이지네이션

Stage 1은 `ownerId + createdAt + taskId` 기반 cursor pagination을 사용한다.

첫 페이지:

```sql
WHERE owner_id = ?
ORDER BY created_at DESC, task_id DESC
LIMIT size + 1
```

다음 페이지:

```sql
WHERE owner_id = ?
  AND (
    created_at < :cursorCreatedAt
    OR (created_at = :cursorCreatedAt AND task_id < :cursorTaskId)
  )
ORDER BY created_at DESC, task_id DESC
LIMIT size + 1
```

cursor는 다음 값을 Base64 JSON으로 인코딩한다.

- `createdAt`
- `taskId`

`size + 1`개를 조회해 다음 페이지 존재 여부를 판단한다.

## 트랜잭션과 락

여러 데이터를 함께 변경하는 service method에는 `@Transactional`을 사용한다.

`@Transactional`이 필요한 흐름:

- `createTask`: `Task` 저장과 `OwnerTaskStats` 증가를 한 transaction으로 처리
- `completeTask`: `Task` 상태 변경과 `OwnerTaskStats` counter 이동을 한 transaction으로 처리
- `deleteTask`: `Task` 삭제와 `OwnerTaskStats` 감소를 한 transaction으로 처리

동시성 요구사항:

- `@Version`으로 optimistic locking을 사용한다.
- task 변경 전에 application level에서 `expectedVersion`을 검증한다.
- flush 또는 commit 시점에도 JPA `@Version`을 통해 lost update를 방지한다.

교육용 pessimistic locking 예시:

- `OwnerTaskStats` repository에 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 적용
- 예시 메서드명: `findByOwnerIdForUpdate`
- main flow와 분리하거나 RDB 전용 비교 포인트임을 명확히 주석으로 표시한다.

## 코드에서 확인할 개념

구현 코드를 읽을 때 다음 개념이 어디에서 드러나는지 확인한다.

- `@Transactional`은 atomic commit/rollback을 제공한다.
- `@Version`은 optimistic locking으로 lost update를 방지한다.
- `PESSIMISTIC_WRITE`는 database-level locking 동작으로 매핑된다.
- 이런 RDB locking 방식은 DynamoDB에 직접 대응되지 않는다.

## Production Notes

- production에서 `ddl-auto=create` 또는 `ddl-auto=update`를 사용하지 않는다.
- schema migration은 Flyway로 관리한다.
- open-in-view를 비활성화한다.
- production에서 SQL log를 활성화하지 않는다.
- HikariCP를 명시적으로 설정한다.
- DB credential은 환경 변수 또는 secret manager로 관리한다.
- query pattern에 맞는 index를 만든다.
- Actuator로 datasource metrics를 모니터링한다.

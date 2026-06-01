# 테스트 및 산출물 요구사항

## 공통 테스트 원칙

모든 Stage는 다음 도구와 스타일을 사용한다.

- JUnit 5
- AssertJ
- BDD-style given/when/then
- Mockito Kotlin 또는 MockK
- Kotlin backtick style의 읽기 쉬운 테스트 이름

## 공통 테스트 케이스

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

## Stage 1 테스트

필수 테스트:

- mocked `TaskRepository`와 `OwnerTaskStatsRepository`를 사용한 service unit test
- mocked service를 사용한 controller test
- Docker Compose MySQL을 사용하는 repository integration test
- JPA `@Version` conflict behavior test
- `@Transactional` rollback behavior test
- pessimistic locking 교육용 테스트 또는 sample

## Stage 2 테스트

필수 테스트:

- mocked repository를 사용한 service unit test
- mocked service를 사용한 controller test
- DynamoDB Local을 사용하는 repository integration test
- `ConditionalCheckFailedException` mapping test
- `TransactionCanceledException` mapping test
- `TransactWriteItems` 실패 시 partial update가 남지 않는지 검증
- `LastEvaluatedKey` cursor encode/decode test

## Stage 3 테스트

필수 테스트:

- mocked `ReactiveTaskRepository`를 사용한 service unit test
- mocked service와 `WebTestClient`를 사용한 WebFlux controller test
- DynamoDB Local을 사용하는 repository integration test
- `StepVerifier` 사용
- `StepVerifier` assertion 내부에서 AssertJ 사용
- `ConditionalCheckFailedException` reactive mapping test
- `TransactionCanceledException` reactive mapping test
- application code에서 `block()`을 요구하지 않는 구조 검증

## 산출물 형식

학습자가 쉽게 따라가고 비교할 수 있도록 세 Stage를 세 개의 독립 Gradle 프로젝트로 구성한다. 멀티 모듈 프로젝트로 만들지 않는다.

최종 구조:

```text
stage1-mysql-mvc/
stage2-dynamodb-mvc/
stage3-dynamodb-webflux/
```

각 Stage는 독립적으로 열고 실행할 수 있어야 한다.

- 각 Stage는 자체 `settings.gradle.kts`를 가진다.
- 각 Stage는 자체 `build.gradle.kts`를 가진다.
- 각 Stage는 자체 `docker-compose.yml`을 가진다.
- 한 Stage의 app/test 실행이 다른 Stage의 Gradle 설정에 의존하지 않는다.
- 공통 module 또는 shared library를 만들지 않는다.
- 중복이 조금 있더라도 비교 학습을 위해 Stage별 코드를 명시적으로 둔다.

각 Stage는 다음 산출물을 포함한다.

- `settings.gradle.kts`
- `build.gradle.kts`
- `docker-compose.yml`
- `application-local.yml`
- `application-prod.yml`
- `README.md`
- source code
- tests

## Stage별 README 요구사항

각 Stage의 `README.md`에는 다음 내용을 포함한다.

- 이 Stage가 가르치는 내용
- Docker Compose 실행 방법
- local profile로 app 실행하는 방법
- test 실행 방법
- 이전 Stage와의 주요 차이점
- production notes

## Stage 2 README 추가 비교 항목

Stage 2 README는 MySQL/JPA와 DynamoDB를 명시적으로 비교한다.

- schema design
- query model
- transaction model
- locking/concurrency model
- pagination model
- production configuration

## Stage 3 README 추가 비교 항목

Stage 3 README는 DynamoDB MVC와 DynamoDB WebFlux를 명시적으로 비교한다.

- sync vs async client
- blocking vs non-blocking execution
- `Mono` / `Flux` adaptation
- `StepVerifier` testing
- DynamoDB 자체에서 바뀌지 않은 것

## 최종 품질 기준

최종 결과물은 Spring Boot MySQL 애플리케이션에서 DynamoDB로 이전하는 과정을 배우기 위한 구조화된 학습 커리큘럼이어야 한다.

코드는 beginner-friendly해야 하지만 기술적으로 정확해야 한다. 단순함을 유지하되, Kotlin/Spring Boot 실무 best practice를 따라 읽기 쉽고 테스트하기 좋은 구조로 작성한다.

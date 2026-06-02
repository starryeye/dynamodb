# Codex Project Guidelines

이 문서는 Codex가 이 저장소를 수정할 때 지켜야 하는 내부 작업 규칙이다. 학습자가 읽어야 하는 DynamoDB 설명은 각 프로젝트의 `README.md`에 둔다.

## Documentation Placement

- 학습자가 알아야 할 개념 설명은 실제 프로젝트 내부 `README.md`에 둔다.
- theory 학습 내용은 `theory/{topic}/README.md`에 둔다.
- practice 학습 내용은 `practice/README.md` 또는 `practice/{stage}/README.md`에 둔다.
- Codex 작업 규칙, 구현 기준, 산출물 기준은 `.codex/` 아래 문서에 둔다.
- `docs/` 폴더를 학습 본문 저장소로 다시 만들지 않는다.

## Theory Projects

- `00-overview`는 오리엔테이션 문서와 학습 메모만 가진다.
- `01-table-item-key` 이후의 theory 프로젝트는 주제별 독립 Spring Boot 프로젝트다.
- 기본 실행 모델은 non-web Spring Boot 애플리케이션을 `gradle test`로 확인하는 방식이다.
- controller와 runner는 theory에서 만들지 않는다.
- DynamoDB는 먼저 blocking `DynamoDbClient`로 학습한다.
- 각 topic은 DynamoDB 핵심 개념 1개, Spring 연동 포인트 1개, 운영 주의점 1개 정도로 작게 유지한다.
- topic이 GSI, pagination, transaction처럼 무거워지면 basics와 failure/consistency/cursor 같은 별도 topic으로 나눈다.

## Theory README Rule

각 theory 프로젝트의 `README.md`는 해당 주제의 본문 역할을 한다.

- README에는 주제 목표, 핵심 개념 정의, 코드 흐름, 실행 방법, 관찰 포인트, 운영 관점을 포함한다.
- DynamoDB를 처음 배우는 사람이 README만 읽어도 해당 주제의 전체 맥락을 이해할 수 있어야 한다.
- table, item, attribute, key처럼 처음 나오는 용어는 README에서 먼저 충분히 설명한다.
- 긴 개념 설명은 코드 주석이 아니라 README에 둔다.

## Theory Code Comment Rule

코드 주석은 README를 반복하지 않고, 해당 코드가 이번 주제에서 어떤 의미인지와 무엇을 하는지 설명한다.

- DynamoDB 개념이 드러나는 주요 코드 라인에는 한국어 주석을 둔다.
- Spring/JUnit/Kotlin 문법처럼 DynamoDB 학습과 직접 관련 없는 코드는 불필요하게 주석을 붙이지 않는다.
- annotation, builder call, test assertion도 DynamoDB 설정이나 operation 이해에 직접 연결될 때만 설명한다.
- 코드를 그대로 한글로 옮기는 데서 끝내지 않고, 그 줄이 학습에서 왜 필요한지도 함께 적는다.

## Practice Projects

- practice Stage는 세 개의 독립 Gradle 프로젝트로 구성한다.
- Stage 간 공통 module 또는 shared library를 만들지 않는다.
- 중복이 조금 있더라도 비교 학습을 위해 Stage별 코드를 명시적으로 둔다.
- 세 Stage는 동일한 도메인, REST API 동작, 계층형 아키텍처, service/repository 메서드명을 최대한 유지한다.
- Stage 3에서는 메서드명은 유지하되 반환 타입을 reactive type으로 바꾼다.

## Practice Common Requirements

- Kotlin, Gradle Kotlin DSL, Spring Boot 사용
- Docker Compose로 로컬 인프라 제공
- 계층형 아키텍처와 SOLID 원칙 준수
- 초급자가 읽기 쉬운 단순한 코드 유지
- 중요한 persistence 차이를 과도한 추상화로 숨기지 않기
- domain model은 persistence annotation에 의존하지 않기
- DTO는 domain model과 분리하기
- service는 business rule orchestration에 집중하고 controller와 persistence 세부 사항을 섞지 않기
- 테스트하기 어려운 정적 시간/ID 생성은 `TimeProvider`, `IdGenerator` 같은 작은 support component로 분리하기

## Practice Test Requirements

모든 Stage는 다음 도구와 스타일을 사용한다.

- JUnit 5
- AssertJ
- BDD-style given/when/then
- Mockito Kotlin 또는 MockK
- Kotlin backtick style의 읽기 쉬운 테스트 이름

공통으로 검증해야 하는 동작:

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

## Stage-Specific Test Requirements

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

## Git Hygiene

- 기존 사용자의 변경을 되돌리지 않는다.
- 커밋 메시지는 기존 스타일인 `[starryeye, YYYY.MM.DD] - docs` 형식을 따른다.
- 이전부터 untracked로 남아 있는 Gradle wrapper 파일은 사용자가 요청하기 전까지 커밋하지 않는다.

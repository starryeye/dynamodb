# Access Patterns 이론 프로젝트 설계

## 목표

`theory/03-access-patterns`를 독립적인 non-web Spring Boot 프로젝트로 만든다.

학습자는 애플리케이션의 요구사항을 먼저 access pattern으로 정리하고, 각 access pattern이 DynamoDB operation과 key 조건으로 어떻게 연결되는지 배운다.

## 학습 범위

이번 주제에서는 다음 세 가지 기본 access pattern만 다룬다.

| 애플리케이션 요구사항 | DynamoDB operation | 필요한 key |
| --- | --- | --- |
| task 저장 | `PutItem` | `ownerId`, `TASK#{taskId}` |
| task 단건 조회 | `GetItem` | `ownerId`, `TASK#{taskId}` |
| owner별 task 목록 조회 | `Query` | `ownerId` |

GSI, Scan, 수정, 삭제, conditional write, transaction, pagination은 이번 주제에서 제외한다. 이 개념들은 뒤의 독립 주제에서 학습한다.

## 프로젝트 구조

`03-access-patterns`는 `01`, `02`와 같은 Kotlin, Gradle Kotlin DSL, Spring Boot, AWS SDK v2 구성을 사용한다.

- `AccessPatternsApplication`: non-web Spring Boot 진입점
- `DynamoDbConfig`: `DynamoDbClient`와 DynamoDB 설정을 Spring bean으로 등록
- `DynamoDbAttributeMapper`: 입문 예제에서 사용하는 문자열 attribute 변환
- `ItemKeys`: `TASK#{taskId}` sort key 생성
- `AccessPatternService`: 세 access pattern을 실제 DynamoDB operation으로 실행
- `AccessPatternsApplicationTest`: Spring context와 bean 설정 검증
- `AccessPatternDynamoDbLocalTest`: DynamoDB Local에서 세 access pattern 검증

controller와 runner는 만들지 않는다. 학습 흐름은 test에서 시작한다.

## 데이터 모델

table primary key는 다음과 같다.

| key 역할 | attribute | 예시 |
| --- | --- | --- |
| partition key | `ownerId` | `owner-1` |
| sort key | `itemKey` | `TASK#task-1` |

task item은 `ownerId`, `itemKey`, `taskId`, `title` 문자열 attribute를 가진다.

이 key 구조는 다음 두 읽기 요구사항을 지원한다.

- `ownerId`와 `taskId`를 모두 알면 `GetItem`으로 task 한 건을 읽는다.
- `ownerId`만 알면 `Query`로 같은 owner의 task item collection을 읽는다.

## 코드 흐름

`AccessPatternService`는 다음 API만 제공한다.

```kotlin
fun saveTask(ownerId: String, taskId: String, title: String)
fun getTask(ownerId: String, taskId: String): Map<String, String>?
fun findTasksByOwner(ownerId: String): List<Map<String, String>>
```

`saveTask`는 학습용 table이 없으면 생성한 뒤 `PutItem`을 실행한다. `getTask`는 완전한 primary key로 `GetItem`을 실행하고 item이 없으면 `null`을 반환한다. `findTasksByOwner`는 partition key 조건으로 `Query`를 실행한다.

table 생성 중 이미 table이 존재해 발생하는 `ResourceInUseException`만 학습 환경의 멱등 실행을 위해 허용한다. 별도 예외 계층이나 재시도 로직은 추가하지 않는다.

## 테스트 설계

DynamoDB Local 연동 테스트는 다음 데이터를 저장한다.

```text
owner-1 / task-1
owner-1 / task-2
owner-2 / task-1
```

그다음 다음 동작을 검증한다.

- `getTask("owner-1", "task-1")`는 정확히 한 task를 반환한다.
- `findTasksByOwner("owner-1")`는 owner-1의 task 두 건만 반환한다.
- 저장, 단건 조회, 목록 조회가 각각 `PutItem`, `GetItem`, `Query` access pattern과 연결된다.

DynamoDB Local이 실행 중이지 않으면 연동 테스트만 skip한다. Spring context 테스트는 항상 실행해 설정과 컴파일 오류를 확인한다.

## 문서와 주석

`theory/03-access-patterns/README.md`를 해당 주제의 전체 학습 본문으로 다시 작성한다.

README는 access pattern의 정의, 요구사항을 먼저 정리하는 이유, 세 operation과 key의 관계, 코드 흐름, 실행 방법, Spring 연동 지점, 운영 관점, 확인 질문을 포함한다. table, item, attribute, primary key처럼 다시 필요한 기초 용어도 초보자가 문맥을 잃지 않을 정도로 설명한다.

코드의 한국어 주석은 DynamoDB operation이나 key 조건이 이번 access pattern에서 어떤 의미인지 설명한다. Kotlin, JUnit, 일반적인 Spring 문법에는 불필요한 주석을 붙이지 않는다.

## 운영 관점

새로운 조회 요구사항이 생기면 구현 전에 현재 table primary key나 index key로 `GetItem` 또는 `Query`가 가능한지 확인한다.

기존 key로 표현할 수 없는 요구사항을 임의의 `Scan`으로 해결하지 않는다. 이 경우 access pattern과 key 설계를 다시 검토해야 한다는 원칙까지만 다룬다.

## 완료 기준

- `03-access-patterns`가 독립 Gradle wrapper를 포함한 Spring Boot 프로젝트다.
- controller와 runner가 없다.
- 실제 DynamoDB Local 테스트가 `PutItem`, `GetItem`, `Query` 흐름을 검증한다.
- README만 읽어도 초보자가 access pattern과 세 operation의 관계를 이해할 수 있다.
- 주요 DynamoDB 코드에 짧고 분명한 한국어 주석이 있다.
- `./gradlew clean test`가 성공하고 연동 테스트가 skip되지 않는다.

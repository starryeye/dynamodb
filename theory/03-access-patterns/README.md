# Access Patterns

이 프로젝트는 DynamoDB table을 만들기 전에 **애플리케이션이 데이터를 어떻게 읽고 쓸지 먼저 정리하는 방법**을 배운다.

이렇게 정리한 데이터 사용 방법을 **access pattern**이라고 한다.

```text
theory/03-access-patterns/
```

## 이번 프로젝트의 목표

- access pattern이 무엇인지 이해한다.
- 애플리케이션 요구사항을 DynamoDB operation으로 바꾼다.
- operation을 실행하려면 어떤 key가 필요한지 확인한다.
- Spring Boot service에서 `PutItem`, `GetItem`, `Query`를 실행한다.
- 새로운 요구사항이 기존 key로 가능한지 먼저 판단하는 습관을 익힌다.

## 먼저 알아야 할 용어

### Table

table은 DynamoDB에서 item을 저장하는 공간이다.

이 프로젝트는 다음 이름의 table을 사용한다.

```text
theory_03_access_patterns
```

### Item

item은 DynamoDB에 저장되는 데이터 한 건이다.

이 프로젝트에서는 할 일 하나를 task item 한 건으로 저장한다.

```text
ownerId = owner-1
itemKey = TASK#task-1
taskId = task-1
title = Access pattern 정리하기
```

### Attribute

attribute는 item 안의 필드 하나다.

위 task item의 `ownerId`, `itemKey`, `taskId`, `title`이 각각 attribute다.
이번 프로젝트에서는 흐름을 단순하게 유지하기 위해 문자열 attribute만 사용한다.

### Primary Key

primary key는 table 안에서 item 한 건을 고유하게 구분하는 key다.

이 프로젝트의 primary key는 두 attribute로 이루어진다.

| 역할 | Attribute | 예시 |
| --- | --- | --- |
| partition key | `ownerId` | `owner-1` |
| sort key | `itemKey` | `TASK#task-1` |

`ownerId`는 item이 어느 owner의 데이터 묶음에 속하는지 결정한다.
`itemKey`는 같은 owner 안에서 task item 한 건을 구분한다.

따라서 다음 두 값이 모두 같아야 같은 item이다.

```text
ownerId = owner-1
itemKey = TASK#task-1
```

## Access Pattern이란

access pattern은 애플리케이션이 **어떤 데이터를 어떤 조건으로 읽거나 쓰는지** 정리한 요구사항이다.

다음과 같은 문장이 access pattern의 시작이다.

```text
task를 저장한다.
ownerId와 taskId로 task 한 건을 찾는다.
ownerId로 해당 owner의 모든 task를 찾는다.
```

각 문장에는 다음 정보가 필요하다.

- 읽기인지 쓰기인지
- 입력으로 어떤 값을 알고 있는지
- item 한 건이 필요한지 여러 건이 필요한지
- 어떤 DynamoDB operation으로 실행할지
- operation에 필요한 key가 무엇인지

## 왜 Access Pattern을 먼저 정리하는가

DynamoDB는 아무 attribute에나 자유롭게 조회 조건을 붙이는 방식을 기본으로 하지 않는다.

효율적인 읽기 경로는 table primary key나 index key로 표현되어야 한다.
따라서 table을 먼저 만든 뒤 조회 코드를 고민하기보다, 자주 사용할 읽기와 쓰기 요구사항을 먼저 정리하고 그 요구사항을 지원하도록 key를 설계한다.

이번 프로젝트의 사고 순서는 다음과 같다.

```text
애플리케이션 요구사항
-> 알고 있는 입력값 확인
-> DynamoDB operation 선택
-> operation에 필요한 key 확인
-> table key 설계
```

## 이번 프로젝트의 Access Patterns

| 애플리케이션 요구사항 | 알고 있는 값 | 결과 | Operation | 필요한 key |
| --- | --- | --- | --- | --- |
| task 저장 | `ownerId`, `taskId`, `title` | item 저장 | `PutItem` | `ownerId`, `TASK#{taskId}` |
| task 단건 조회 | `ownerId`, `taskId` | item 한 건 | `GetItem` | `ownerId`, `TASK#{taskId}` |
| owner별 task 목록 조회 | `ownerId` | item 여러 건 | `Query` | `ownerId` |

### Task 저장: PutItem

`PutItem`은 item 한 건을 table에 저장하는 operation이다.

task를 저장할 때는 primary key가 될 `ownerId`와 `itemKey`를 item에 포함해야 한다.
이 프로젝트는 application의 `taskId` 앞에 `TASK#`를 붙여 sort key를 만든다.

```text
taskId = task-1
-> itemKey = TASK#task-1
```

`AccessPatternService.saveTask()`가 이 access pattern을 실행한다.

이번 단계의 `PutItem`에는 중복 저장을 막는 조건이 없다.
같은 primary key로 다시 저장하면 기존 item을 덮어쓸 수 있으며, 중복 방지는 뒤의 `11-conditional-put`에서 다룬다.

### Task 단건 조회: GetItem

`GetItem`은 primary key 전체를 사용해 item 한 건을 읽는 operation이다.

이 table의 primary key는 `ownerId + itemKey`이므로 두 값을 모두 알아야 한다.

```text
ownerId = owner-1
itemKey = TASK#task-1
```

`AccessPatternService.getTask()`는 application의 `ownerId`와 `taskId`를 받아 위 key를 만들고 `GetItem`을 실행한다.

partition key인 `ownerId`만 알고 있다면 어떤 task 한 건을 원하는지 결정할 수 없으므로 `GetItem`을 사용할 수 없다.

### Owner별 Task 목록 조회: Query

`Query`는 partition key 값이 같은 item 묶음을 읽는 operation이다.

partition key 값이 같은 item 묶음을 **item collection**이라고 한다.
다음 두 item은 `ownerId`가 같으므로 같은 item collection에 속한다.

```text
ownerId = owner-1, itemKey = TASK#task-1
ownerId = owner-1, itemKey = TASK#task-2
```

`AccessPatternService.findTasksByOwner()`는 다음 key condition으로 `Query`를 실행한다.

```text
ownerId = :ownerId
```

`:ownerId`에 `owner-1`을 넣으면 owner-1 item collection만 반환된다.
partition key가 다른 owner-2의 item은 결과에 포함되지 않는다.

## 이번 프로젝트에서 다루지 않는 것

- `Scan`
- GSI
- 수정과 삭제
- conditional write
- transaction
- pagination

이번 주제는 세 가지 기본 access pattern을 정리하고 operation과 key로 옮기는 데 집중한다.
위 개념은 뒤의 독립 주제에서 하나씩 학습한다.

## 코드 흐름

```text
Spring Boot test
-> service.saveTask(ownerId, taskId, title)
-> PutItem으로 task 저장
-> service.getTask(ownerId, taskId)
-> GetItem에 primary key 전체 전달
-> service.findTasksByOwner(ownerId)
-> Query에 partition key 조건 전달
-> 테스트에서 단건과 목록 결과 확인
```

주요 파일의 역할은 다음과 같다.

| 파일 | 역할 |
| --- | --- |
| `AccessPatternService.kt` | 세 요구사항을 `PutItem`, `GetItem`, `Query`로 실행한다. |
| `ItemKeys.kt` | application의 taskId를 DynamoDB sort key로 바꾼다. |
| `DynamoDbAttributeMapper.kt` | 문자열과 AWS SDK의 `AttributeValue`를 변환한다. |
| `DynamoDbConfig.kt` | `DynamoDbClient`를 Spring bean으로 등록한다. |
| `AccessPatternDynamoDbLocalTest.kt` | 실제 DynamoDB Local에서 세 access pattern의 결과를 확인한다. |

## 실행 방법

먼저 DynamoDB Local을 실행한다.

```bash
docker compose up -d
```

그다음 테스트를 실행한다.

```bash
./gradlew test
```

DynamoDB Local이 실행 중이지 않으면 연동 테스트는 skip된다.
학습할 때는 test report에서 `skipped=0`인지 확인해야 실제 `PutItem`, `GetItem`, `Query`가 실행됐음을 알 수 있다.

## Spring 연동 포인트

이 프로젝트에는 controller와 runner가 없다.
Spring Boot context는 test가 로딩한다.

`DynamoDbConfig`는 blocking 방식의 `DynamoDbClient`를 Spring bean으로 등록한다.
`AccessPatternService`는 client를 직접 만들지 않고 생성자에서 주입받는다.

local 설정은 localhost의 DynamoDB Local과 dummy credential을 사용한다.

```yaml
app:
  dynamodb:
    endpoint: http://localhost:8000
    use-dummy-credentials: true
```

production에서는 endpoint override와 dummy credential을 사용하지 않는다.
AWS region과 IAM role, 환경 변수 같은 기본 credential 흐름으로 실제 DynamoDB에 연결한다.

## 테스트에서 관찰할 것

`완전한 primary key로 task 한 건을 조회한다` 테스트에서는 다음 흐름을 확인한다.

- `PutItem`으로 task를 저장한다.
- `GetItem`에 partition key와 sort key를 모두 전달한다.
- 정확한 task 한 건이 반환된다.

`partition key로 owner의 task 목록을 조회한다` 테스트에서는 다음 흐름을 확인한다.

- owner-1 task 두 건과 owner-2 task 한 건을 저장한다.
- owner-1을 partition key 조건으로 `Query`한다.
- owner-1 task 두 건만 반환된다.

## 운영 관점

새로운 조회 기능이 생기면 구현 전에 다음 질문을 먼저 한다.

```text
현재 table primary key나 index key로 GetItem 또는 Query할 수 있는가?
```

가능하다면 필요한 key와 operation을 access pattern 문서에 기록한다.
가능하지 않다면 일반 애플리케이션 경로에 임의의 `Scan`을 추가하기보다 access pattern과 key 설계를 다시 검토한다.

access pattern은 처음 한 번 작성하고 끝나는 문서가 아니다.
API, 화면, batch 요구사항이 바뀔 때 함께 갱신해야 table과 index가 실제 사용 경로를 계속 지원할 수 있다.

## 확인 질문

- access pattern은 무엇인가?
- access pattern을 table 설계보다 먼저 정리하는 이유는 무엇인가?
- task 단건 조회에 partition key와 sort key가 모두 필요한 이유는 무엇인가?
- owner별 task 목록 조회가 `GetItem`이 아니라 `Query`인 이유는 무엇인가?
- 새 조회 요구사항이 현재 key로 표현되지 않으면 무엇을 다시 검토해야 하는가?

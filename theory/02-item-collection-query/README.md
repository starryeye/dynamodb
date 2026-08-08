# Item Collection Query

이 프로젝트는 DynamoDB의 `Query`를 처음 배우는 단계다.

목표는 **partition key가 같은 item 묶음**, 즉 **item collection**을 Spring Boot service와 test로 조회해 보는 것이다.

```text
theory/02-item-collection-query/
```

## 먼저 알아야 할 용어

### Table

table은 DynamoDB에서 item을 저장하는 공간이다.

RDB의 table과 비슷하게 데이터를 담는 단위지만, DynamoDB table은 join을 전제로 설계하지 않는다.
DynamoDB에서는 table을 만들 때 “나중에 어떤 조건으로 읽을 것인가”를 먼저 생각해야 한다.

이 프로젝트의 table 이름은 기본값으로 다음을 사용한다.

```text
theory_02_item_collection_query
```

### Item

item은 DynamoDB에 저장되는 데이터 한 건이다.

RDB의 row와 비슷하게 생각할 수 있다.
이 프로젝트에서는 “할 일 하나”를 item 한 건으로 저장한다.

예를 들면 다음과 같은 item을 저장한다.

```text
ownerId = owner-1
itemKey = TASK#task-1
entityType = TASK
taskId = task-1
title = Query 기본 이해하기
status = TODO
```

### Attribute

attribute는 item 안에 있는 필드 하나다.

위 item에서 `ownerId`, `itemKey`, `title`, `status`가 모두 attribute다.
AWS SDK for Java/Kotlin에서는 attribute 값을 `AttributeValue` 타입으로 표현한다.

`AttributeValue`는 값만 담는 타입이 아니다.
문자열인지, 숫자인지, boolean인지 같은 DynamoDB 값 타입도 함께 담는다.

이 프로젝트는 입문용이므로 문자열 attribute만 사용한다.

### Partition Key

partition key는 DynamoDB가 item을 어느 물리적 partition에 둘지 결정하는 key다.

이 프로젝트에서는 `ownerId`를 partition key로 사용한다.
즉 `ownerId = owner-1`인 item들은 같은 owner의 데이터 묶음으로 조회할 수 있다.

### Sort Key

sort key는 같은 partition key 안에서 item을 구분하고 정렬하는 key다.

이 프로젝트에서는 `itemKey`를 sort key로 사용한다.
값은 다음 규칙을 따른다.

```text
TASK#task-1
TASK#task-2
```

`TASK#` prefix는 이 item이 task라는 의미를 코드에서 읽기 쉽게 만든다.
sort key prefix를 적극적으로 활용하는 패턴은 뒤의 `05-sort-key-prefixes`에서 더 자세히 다룬다.

## Item Collection

item collection은 **partition key 값이 같은 item들의 묶음**이다.

이 프로젝트의 key 구조는 다음과 같다.

| 역할 | Attribute | 예시 |
| --- | --- | --- |
| partition key | `ownerId` | `owner-1` |
| sort key | `itemKey` | `TASK#task-1` |

따라서 다음 두 item은 같은 item collection에 속한다.

```text
ownerId = owner-1, itemKey = TASK#task-1
ownerId = owner-1, itemKey = TASK#task-2
```

반면 다음 item은 partition key가 다르므로 다른 item collection에 속한다.

```text
ownerId = owner-2, itemKey = TASK#task-1
```

## GetItem과 Query의 차이

`GetItem`은 item 한 건을 정확히 읽을 때 사용한다.

이 table은 primary key가 `ownerId + itemKey`로 이루어져 있으므로, `GetItem`을 쓰려면 두 값을 모두 알아야 한다.

```text
ownerId = owner-1
itemKey = TASK#task-1
```

반면 `Query`는 partition key 조건으로 item collection을 읽을 때 사용한다.

예를 들어 다음 요구사항은 `GetItem`이 아니라 `Query`가 필요하다.

```text
owner-1의 모든 task를 조회한다.
```

이때 application은 특정 `itemKey` 하나를 모른다.
알고 있는 것은 `ownerId = owner-1`뿐이다.
그래서 `Query`의 key condition에 partition key 조건을 넣는다.

```text
ownerId = :ownerId
```

이 조건은 “`ownerId`가 같은 item collection만 읽는다”는 뜻이다.

## 이번 프로젝트에서 배우는 것

- item collection이 무엇인지 이해한다.
- partition key 조건으로 `Query`를 호출한다.
- `GetItem`과 `Query`를 구분한다.
- Spring Boot service에서 `DynamoDbClient`를 주입받아 Query를 실행한다.
- test로 DynamoDB Local 연동 흐름을 확인한다.

## 이번 프로젝트에서 아직 다루지 않는 것

- `Scan`
- GSI
- pagination
- sort key 조건 세분화
- conditional write
- transaction

이 주제에서는 `Query`의 가장 기본 형태만 다룬다.
목록 조회의 크기 제한과 `LastEvaluatedKey`는 뒤의 pagination 주제에서 다룬다.

## 코드 흐름

```text
test
-> service.saveDemoTasks()
-> table이 없으면 생성
-> owner-1 item 두 건 저장
-> owner-2 item 한 건 저장
-> service.findTasksByOwner("owner-1")
-> Query(ownerId = :ownerId)
-> owner-1 item collection 반환
```

핵심 코드는 `ItemCollectionQueryService`에 있다.

`saveDemoTasks()`는 Query 결과를 확인할 수 있도록 demo item을 저장한다.
`findTasksByOwner(ownerId)`는 partition key 조건으로 Query를 호출한다.

## 실행 방법

먼저 DynamoDB Local을 실행한다.

```bash
docker compose up -d
```

그 다음 테스트를 실행한다.

```bash
./gradlew test
```

DynamoDB Local을 실행하지 않은 상태에서 테스트를 실행하면 실제 연동 테스트는 skip된다.
컴파일과 Spring context 구성은 그대로 확인된다.

## Spring 연동 포인트

이 프로젝트는 web application이 아니다.
controller나 runner 없이 Spring Boot context를 테스트에서 로딩한다.

`DynamoDbConfig`는 `DynamoDbClient`를 Spring bean으로 등록한다.
service는 이 client를 생성하지 않고 주입받는다.

local profile에서는 다음 값을 사용한다.

```yaml
app:
  dynamodb:
    endpoint: http://localhost:8000
    use-dummy-credentials: true
```

production에서는 `endpoint`와 dummy credential을 사용하지 않는다.
실제 AWS DynamoDB를 호출할 때는 region과 IAM credential 흐름이 중요하다.

## 운영 관점

운영 환경에서 목록 조회 요구사항이 생기면 먼저 질문해야 한다.

```text
이 조회는 partition key 조건으로 Query할 수 있는가?
```

`ownerId`로 “한 owner의 task 목록”을 조회하는 요구사항은 Query로 표현하기 좋다.
반대로 “전체 owner의 TODO task를 모두 조회” 같은 요구사항은 이 key 구조만으로는 효율적인 Query가 어렵다.

또 하나의 주의점은 item collection 크기다.

특정 `ownerId`에 item이 지나치게 많이 몰리면 한 partition에 읽기/쓰기 부하가 집중될 수 있다.
이 문제는 hot partition으로 이어질 수 있다.
따라서 DynamoDB 설계에서는 “어떤 partition key 값에 데이터와 트래픽이 몰리는가”를 계속 확인해야 한다.

## 확인 질문

- item collection은 무엇인가?
- `ownerId`만 알 때 `GetItem`이 아니라 `Query`가 필요한 이유는 무엇인가?
- `owner-1` Query 결과에 `owner-2` item이 섞이지 않는 이유는 무엇인가?
- 같은 partition key에 item이 너무 많이 몰리면 어떤 위험이 생기는가?

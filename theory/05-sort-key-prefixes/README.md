# 05. Sort Key Prefixes

이 프로젝트는 sort key 문자열 앞에 붙이는 prefix와
`begins_with` Query를 배우는 입문 예제다.

같은 owner의 item 중에서 sort key가 `TASK#`로 시작하는 item만
실제 DynamoDB Local에서 조회한다.

Spring Boot test가 `DynamoDbClient`를 주입받아 모든 동작을 실행한다.
Controller나 실행용 Runner는 사용하지 않는다.

## 학습 목표

이 프로젝트를 마치면 다음 내용을 설명할 수 있어야 한다.

- sort key prefix가 무엇인지
- `TASK#task-1` 문자열에서 prefix, 구분자, 식별자를 구분하는 방법
- prefix가 DynamoDB schema가 아니라 application convention인 이유
- `begins_with`를 sort key의 `KeyConditionExpression`에 사용하는 방법
- 같은 partition에서 특정 prefix item만 Query하는 방법
- production의 key naming convention을 쉽게 바꾸면 안 되는 이유

## 먼저 알아야 할 용어

### Table

table은 여러 item을 저장하는 공간이다.

이 프로젝트는 기본적으로 다음 table을 사용한다.

```text
theory_05_sort_key_prefixes
```

### Item

item은 table에 저장하는 데이터 한 건이다.

이 프로젝트에는 task item과 stats item이 있다.
두 종류를 같은 table에 저장하지만, 여러 종류의 데이터를 함께 설계하는 방법은 다음 주제에서 배운다.

### Attribute

attribute는 item을 구성하는 이름과 값이다.

예를 들어 task item에는 다음 attribute가 들어 있다.

| attribute | 값 예시 | 역할 |
| --- | --- | --- |
| `ownerId` | `owner-1` | item이 속한 owner |
| `itemKey` | `TASK#task-1` | item을 구분하는 sort key |
| `itemType` | `TASK` | application이 해석하는 item 종류 |
| `title` | `Sort key 읽기` | task 제목 |

### Partition Key

partition key는 item이 속한 partition을 찾는 key다.

이 프로젝트에서는 `ownerId`가 partition key다.
따라서 `ownerId`가 `owner-1`인 item들은 같은 item collection에 속한다.

### Sort Key

sort key는 같은 partition key를 가진 item을 구분하고 정렬하는 key다.

이 프로젝트에서는 `itemKey`가 sort key다.

table의 composite primary key는 다음과 같다.

```text
partition key: ownerId
sort key:      itemKey
```

한 item은 `ownerId`와 `itemKey`의 조합으로 식별된다.

## 예제 데이터

테스트는 `owner-1` partition에 다음 세 item을 저장한다.

| ownerId | itemKey | itemType |
| --- | --- | --- |
| `owner-1` | `TASK#task-1` | `TASK` |
| `owner-1` | `TASK#task-2` | `TASK` |
| `owner-1` | `STATS` | `STATS` |

세 item의 partition key는 모두 같다.
sort key 값은 서로 다르므로 각각 별도의 item이다.

이번 조회 요구사항은 다음과 같다.

```text
owner-1의 item 중에서 task item만 조회한다.
```

## Sort Key Prefix란

prefix는 문자열의 앞부분에 반복해서 붙이는 구분 값이다.

`TASK#task-1`을 나누면 다음과 같다.

```text
TASK  #  task-1
^^^^  ^  ^^^^^^
종류  구분자  식별자
```

- `TASK`는 이 key가 task item을 나타낸다는 뜻이다.
- `#`은 prefix와 식별자를 눈으로 구분하기 위한 문자다.
- `task-1`은 실제 task 식별자다.

같은 규칙으로 task key를 더 만들 수 있다.

```text
TASK#task-1
TASK#task-2
TASK#task-3
```

이 값들은 모두 `TASK#`로 시작한다.

## `#`에는 특별한 DynamoDB 기능이 있는가

없다.

`#`은 sort key의 **값 안에서** application이 선택한 구분자일 뿐이다.
DynamoDB가 `#`을 보고 item type을 자동으로 이해하지 않는다.

다음처럼 다른 구분자를 사용할 수도 있다.

```text
TASK:task-1
TASK|task-1
TASK/task-1
```

이 프로젝트는 읽기 쉽고 흔히 사용하는 형태인 `TASK#task-1`을 선택한다.
중요한 것은 어떤 문자를 선택했는지가 아니라 모든 저장과 조회에서 같은 규칙을 사용하는 것이다.

## Prefix는 DynamoDB Schema인가

아니다.

table을 생성할 때 DynamoDB에 알려 주는 key 정보는 다음뿐이다.

```text
ownerId: String partition key
itemKey: String sort key
```

DynamoDB table 설정에는 `TASK#`나 `STATS`를 등록하지 않는다.
DynamoDB는 두 값을 모두 `itemKey`라는 String sort key 값으로 취급한다.

`TASK#`를 task의 prefix로 사용한다는 의미는 application 코드와 문서가 정한다.
그래서 이를 **key naming convention**이라고 부른다.

## 왜 Prefix를 사용하는가

같은 partition 안에 sort key 형식이 다른 item이 있을 때 prefix로 원하는 범위를 표현할 수 있다.

현재 `owner-1` partition에는 다음 key가 있다.

```text
STATS
TASK#task-1
TASK#task-2
```

partition key 조건만 사용하면 세 item이 모두 Query 결과에 들어온다.

```text
ownerId = owner-1
```

sort key prefix 조건을 함께 사용하면 task 두 건만 읽을 수 있다.

```text
ownerId = owner-1
itemKey begins_with TASK#
```

## begins_with

`begins_with(attribute, prefix)`는 문자열이 특정 prefix로 시작하는지 확인하는 DynamoDB expression 함수다.

이 프로젝트는 다음 `KeyConditionExpression`을 사용한다.

```kotlin
.keyConditionExpression(
    "ownerId = :ownerId AND begins_with(itemKey, :prefix)",
)
```

조건을 두 부분으로 나누어 읽으면 된다.

```text
ownerId = :ownerId
```

partition key가 조회할 owner와 같아야 한다.

```text
begins_with(itemKey, :prefix)
```

sort key가 지정한 prefix로 시작해야 한다.

표현식의 자리 이름에는 실제 값을 연결한다.

```kotlin
.expressionAttributeValues(
    mapOf(
        ":ownerId" to DynamoDbAttributeMapper.s("owner-1"),
        ":prefix" to DynamoDbAttributeMapper.s("TASK#"),
    ),
)
```

실제 실행 조건은 다음과 같다.

```text
ownerId = owner-1
AND
itemKey begins_with TASK#
```

결과에는 `TASK#task-1`, `TASK#task-2`가 포함되고 `STATS`는 포함되지 않는다.

## 왜 FilterExpression을 사용하지 않는가

`itemKey`는 이 table의 sort key다.
key를 이용해 읽을 범위를 정하는 조건은 `KeyConditionExpression`에 작성한다.

```text
KeyConditionExpression
    -> DynamoDB가 어떤 key 범위를 읽을지 정한다.

FilterExpression
    -> 이미 읽은 결과에서 일부 item을 제거한다.
```

이번 요구사항은 sort key가 `TASK#`로 시작하는 범위를 알고 있다.
따라서 `begins_with(itemKey, :prefix)`를 key condition으로 사용하는 것이 맞다.

이 방식은 이전 주제에서 배운 “FilterExpression은 읽은 뒤 거른다”는 내용과도 연결된다.

## Sort Key 정렬

DynamoDB Query 결과는 기본적으로 sort key 순서로 반환된다.
String sort key는 UTF-8 byte 순서로 정렬된다.

이 예제의 task 결과는 다음 순서다.

```text
TASK#task-1
TASK#task-2
```

이 프로젝트는 단순한 식별자를 사용한다.
시간순 정렬이나 역순 조회처럼 더 복잡한 sort key 설계는 뒤의 주제에서 다룬다.

## ItemKeyFactory

key 문자열을 service 여러 곳에서 직접 조합하면 규칙이 쉽게 달라질 수 있다.

잘못된 예시는 다음과 같다.

```text
TASK#task-1
TASK-task-2
task#task-3
```

세 key는 서로 다른 prefix를 가지므로 같은 `begins_with` Query로 모두 찾을 수 없다.

이 프로젝트는 `ItemKeyFactory` Spring component에 key 생성 규칙을 모은다.

```kotlin
fun task(taskId: String): String = "TASK#$taskId"

fun taskPrefix(): String = "TASK#"

fun stats(): String = "STATS"
```

저장 코드와 조회 코드가 같은 component의 규칙을 사용한다.

- 저장할 때 `task(taskId)`로 전체 sort key를 만든다.
- 조회할 때 `taskPrefix()`로 `begins_with`의 prefix를 얻는다.
- stats item은 `stats()`로 별도의 key를 만든다.

## 코드 흐름

테스트는 다음 순서로 실행된다.

```text
Spring Boot test 시작
    -> DynamoDbClient bean 주입
    -> table이 없으면 생성
    -> owner-1에 TASK item 두 건 저장
    -> owner-1에 STATS item 한 건 저장
    -> ownerId와 TASK# prefix로 Query
    -> TASK item 두 건만 반환되는지 검증
```

주요 파일은 다음과 같다.

| 파일 | 역할 |
| --- | --- |
| `SortKeyPrefixesApplication.kt` | Spring Boot application marker |
| `DynamoDbConfig.kt` | blocking `DynamoDbClient` bean 생성 |
| `DynamoDbAttributeMapper.kt` | Kotlin 문자열과 DynamoDB String attribute 변환 |
| `ItemKeyFactory.kt` | sort key naming convention 관리 |
| `SortKeyPrefixService.kt` | demo item 저장과 prefix Query 실행 |
| `ItemKeyFactoryTest.kt` | key 문자열 규칙 검증 |
| `SortKeyPrefixesDynamoDbLocalTest.kt` | 실제 DynamoDB Local Query 결과 검증 |

## Spring Boot 연동

`DynamoDbConfig`는 AWS SDK의 blocking `DynamoDbClient`를 Spring bean으로 등록한다.

`SortKeyPrefixService`는 다음 bean을 생성자로 주입받는다.

- `DynamoDbClient`: DynamoDB 요청 실행
- `DynamoDbProperties`: region, table, endpoint 설정 제공
- `ItemKeyFactory`: 저장과 조회에 사용할 key 규칙 제공

이 프로젝트는 Servlet Web application이 아니므로 Controller가 필요하지 않다.
학습 동작은 모두 Spring Boot test에서 시작한다.

local 환경에서는 다음 설정을 사용한다.

```yaml
app:
  dynamodb:
    endpoint: http://localhost:8000
    use-dummy-credentials: true
```

- `endpoint`는 실제 AWS 대신 DynamoDB Local로 요청을 보낸다.
- DynamoDB Local은 credential을 검증하지 않으므로 dummy 값을 사용한다.
- 운영 환경에서는 endpoint override와 dummy credential을 사용하지 않는다.
- 운영 환경의 credential은 IAM role을 포함한 AWS 기본 credential 흐름에서 얻는다.

## 실행 방법

필요한 환경은 JDK 21과 Docker다.

다른 학습 프로젝트가 이미 `8000` port에서 DynamoDB Local을 실행 중이면 먼저 종료한다.

```bash
cd theory/05-sort-key-prefixes
docker compose up -d
./gradlew clean test
```

테스트가 끝난 뒤 local container를 종료한다.

```bash
docker compose down
```

테스트 report는 다음 경로에서 확인할 수 있다.

```text
build/reports/tests/test/index.html
```

## 테스트에서 확인할 부분

### ItemKeyFactoryTest

key naming convention을 검증한다.

```text
task(task-1) -> TASK#task-1
stats()      -> STATS
```

### SortKeyPrefixesDynamoDbLocalTest

실제 DynamoDB Local에서 다음을 검증한다.

1. 같은 owner partition에 task와 stats item을 저장한다.
2. `TASK#` prefix로 Query한다.
3. task item 두 건만 반환된다.
4. `STATS` item은 반환되지 않는다.
5. 결과는 sort key 오름차순이다.

테스트가 실행되지 않고 skip된다면 `localhost:8000`의 DynamoDB Local 연결부터 확인한다.

## 운영 환경에서 기억할 점

sort key naming convention은 production 데이터의 일부다.

예를 들어 이미 수백만 개의 `TASK#...` item을 저장한 뒤 prefix를 `TODO#...`로 바꾸면
기존 item은 새로운 prefix Query로 조회되지 않는다.

규칙을 바꾸려면 다음을 함께 고려해야 한다.

- 기존 item의 key를 새 형식으로 옮기는 migration
- migration 중 구형 key와 신형 key를 함께 읽는 방법
- 이전 application version과 새 application version의 호환성
- 실패한 migration을 확인하고 복구하는 방법

DynamoDB의 primary key 값은 같은 item에서 단순히 수정할 수 없다.
key 형식을 바꾸려면 새 key로 item을 쓰고 기존 key의 item을 삭제하는 흐름이 필요하다.

그래서 이 프로젝트는 다음 방식으로 convention을 관리한다.

- key 생성 규칙을 `ItemKeyFactory` 한 곳에 둔다.
- README에 실제 key 형식을 기록한다.
- 단위 테스트로 정확한 문자열을 고정한다.
- DynamoDB Local 테스트로 저장과 Query가 같은 규칙을 사용하는지 확인한다.

## 이번 주제에서 다루지 않는 것

한 번에 너무 많은 개념을 배우지 않도록 다음 내용은 뒤의 주제로 미룬다.

- full single-table design
- 여러 domain item의 attribute shape 설계
- 계층형 composite sort key
- GSI
- transaction
- pagination과 `LastEvaluatedKey`
- sort key 범위 조건과 역순 조회

## 확인 질문

1. `TASK#task-1`에서 prefix와 식별자는 각각 무엇인가?
2. `#` 문자는 DynamoDB가 정한 기능인가, application이 선택한 구분자인가?
3. table을 생성할 때 `TASK#` prefix를 schema로 등록하는가?
4. `begins_with(itemKey, :prefix)`를 `FilterExpression`이 아닌 `KeyConditionExpression`에 두는 이유는 무엇인가?
5. `ItemKeyFactory` 없이 여러 service가 key 문자열을 직접 만들면 어떤 문제가 생길 수 있는가?
6. production에 저장된 prefix를 바꾸기 어려운 이유는 무엇인가?

## 공식 문서

- [DynamoDB Query key condition expressions](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Query.KeyConditionExpressions.html)
- [DynamoDB sort key best practices](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-sort-keys.html)
- [DynamoDB data modeling](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/data-modeling.html)

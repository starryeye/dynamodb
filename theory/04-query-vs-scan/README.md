# 04. Query와 Scan

이 프로젝트는 같은 task 목록을 `Query`와 `Scan`으로 각각 조회하고,
DynamoDB가 실제로 평가한 item 수를 비교하는 입문 예제다.

Spring Boot test가 `DynamoDbClient`를 주입받아 DynamoDB Local에 요청한다.
Controller나 실행용 Runner는 사용하지 않는다.

## 학습 목표

이 프로젝트를 마치면 다음 내용을 설명할 수 있어야 한다.

- table, item, attribute가 무엇인지
- `Query`가 어떤 범위의 item을 읽는지
- `Scan`이 어떤 범위의 item을 읽는지
- `FilterExpression`이 읽기 범위를 줄이지 못하는 이유
- `Count`와 `ScannedCount`의 차이
- application의 일반 조회에서 `Query`를 우선해야 하는 이유

## 먼저 알아야 할 용어

### Table

table은 같은 종류의 데이터를 저장하는 공간이다.

이 프로젝트는 기본적으로 `theory_04_query_vs_scan` table을 사용한다.

### Item

item은 table에 저장하는 데이터 한 건이다.

관계형 데이터베이스의 행과 비슷하게 생각할 수 있다.
이 프로젝트에서는 task 한 건이 item 한 건이다.

### Attribute

attribute는 item을 구성하는 이름과 값이다.

관계형 데이터베이스의 열과 비슷하게 생각할 수 있다.
task item에는 다음 attribute가 들어 있다.

| attribute | 역할 | 예시 |
| --- | --- | --- |
| `ownerId` | task 소유자이자 partition key | `owner-1` |
| `itemKey` | 같은 소유자의 task를 구분하는 sort key | `TASK#task-1` |
| `taskId` | application이 사용하는 task 식별자 | `task-1` |
| `title` | task 제목 | `Query 실습하기` |

AWS SDK에서는 attribute 값을 `AttributeValue` 타입으로 표현한다.
예를 들어 Kotlin 문자열 `owner-1`은 DynamoDB의 String attribute 값으로 변환해서 요청에 넣는다.

### Primary Key

이 table의 primary key는 다음 두 attribute로 구성된다.

```text
partition key: ownerId
sort key:      itemKey
```

`ownerId`가 같은 item은 같은 논리적 partition에 모인다.
`itemKey`는 그 안에서 각 task를 구분하고 정렬한다.

## 예제 데이터

테스트는 다음 5개 item을 저장한다.

| ownerId | itemKey | title |
| --- | --- | --- |
| `owner-1` | `TASK#task-1` | DynamoDB 기본 개념 읽기 |
| `owner-1` | `TASK#task-2` | Query 실습하기 |
| `owner-2` | `TASK#task-1` | Spring 설정 확인하기 |
| `owner-2` | `TASK#task-2` | Scan 실습하기 |
| `owner-2` | `TASK#task-3` | 결과 비교하기 |

조회 요구사항은 다음과 같다.

```text
owner-1의 모든 task를 조회한다.
```

같은 요구사항을 `Query`와 `Scan`으로 각각 실행한다.

## Query란

`Query`는 partition key 값을 지정해서 해당 item collection을 읽는 연산이다.

이 예제의 조건은 다음과 같다.

```text
ownerId = owner-1
```

AWS SDK 요청에서는 이 조건을 `KeyConditionExpression`으로 작성한다.

```kotlin
.keyConditionExpression("ownerId = :ownerId")
```

`:ownerId`는 표현식 안에서 사용할 자리 이름이다.
실제 값 `owner-1`은 `ExpressionAttributeValues`로 연결한다.

```kotlin
.expressionAttributeValues(
    mapOf(":ownerId" to DynamoDbAttributeMapper.s("owner-1")),
)
```

따라서 DynamoDB는 `owner-1` partition에 속한 item 두 건을 대상으로 조회한다.

`Query`에는 partition key의 동등 조건이 반드시 필요하다.
필요하면 sort key 조건을 추가해 partition 안의 범위를 더 줄일 수 있다.

## Scan이란

`Scan`은 key로 읽을 범위를 먼저 좁히지 않고 table의 item을 차례로 평가하는 연산이다.

이 예제는 table을 Scan하면서 다음 filter를 사용한다.

```kotlin
.filterExpression("ownerId = :ownerId")
```

겉으로는 `owner-1` 조건이 있으므로 Query와 비슷해 보인다.
하지만 실행 순서가 다르다.

```text
1. Scan이 table의 item을 읽는다.
2. FilterExpression을 적용한다.
3. owner-1 item만 응답에 남긴다.
```

즉, filter는 응답에서 불필요한 item을 제거할 뿐,
DynamoDB가 먼저 읽고 평가하는 item 수를 줄이지 않는다.

이 작은 예제의 item 5개는 한 요청에서 모두 처리된다.
실제 Scan 응답은 한 번에 최대 1MB까지 처리하므로 전체 table을 계속 읽으려면 pagination도 필요하다.
pagination은 뒤의 별도 주제에서 다룬다.

## Count와 ScannedCount

DynamoDB 응답에는 조회 범위를 이해하는 데 유용한 두 수치가 있다.

### Count

`Count`는 조건과 filter를 모두 통과해 응답에 포함된 item 수다.

### ScannedCount

`ScannedCount`는 filter를 적용하기 전에 DynamoDB가 평가한 item 수다.

이 프로젝트의 결과는 다음과 같다.

| 연산 | 응답 item | Count | ScannedCount |
| --- | ---: | ---: | ---: |
| Query | owner-1의 task 2개 | 2 | 2 |
| Scan + filter | owner-1의 task 2개 | 2 | 5 |

두 연산은 application에 같은 task 두 건을 반환한다.
그러나 Query는 `owner-1`의 item 두 건만 평가하고,
Scan은 두 건을 반환하기 위해 table의 item 다섯 건을 모두 평가한다.

데이터가 커질수록 이 차이는 읽기 비용과 응답 지연의 차이로 이어질 수 있다.

## 왜 실행 시간을 비교하지 않는가

item이 5개뿐인 로컬 환경에서는 실행 시간이 매우 짧고 매번 달라질 수 있다.
이 수치만으로 Query와 Scan의 운영 비용을 판단하면 안 된다.

이 예제는 환경의 영향을 많이 받는 시간 대신 다음을 비교한다.

```text
Count        = application에 반환된 item 수
ScannedCount = DynamoDB가 평가한 item 수
```

## 코드 흐름

테스트는 다음 순서로 실행된다.

```text
Spring Boot test 시작
    -> DynamoDbClient bean 주입
    -> table이 없으면 생성
    -> 두 owner의 task 5개 저장
    -> owner-1을 Query
    -> owner-1 조건으로 table을 Scan
    -> 반환 item과 Count, ScannedCount 비교
```

주요 파일은 다음과 같다.

| 파일 | 역할 |
| --- | --- |
| `DynamoDbConfig.kt` | Spring이 사용할 `DynamoDbClient` bean 생성 |
| `DynamoDbAttributeMapper.kt` | Kotlin 문자열과 DynamoDB String attribute 변환 |
| `ItemKeys.kt` | task의 sort key 값 생성 |
| `QueryVsScanService.kt` | 데이터 저장, Query, Scan 실행 |
| `ReadResult.kt` | item과 Count, ScannedCount 보관 |
| `QueryVsScanDynamoDbLocalTest.kt` | 실제 DynamoDB Local 결과 검증 |

## Spring Boot 연동

`DynamoDbConfig`는 AWS SDK의 blocking `DynamoDbClient`를 Spring bean으로 등록한다.
서비스는 생성자 주입으로 이 client를 받아 사용한다.

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
- DynamoDB Local은 AWS credential을 검증하지 않으므로 dummy 값을 사용한다.
- 운영 환경에서는 endpoint override와 dummy credential을 사용하지 않는다.
- 운영 환경의 credential은 EC2, ECS, EKS 등의 IAM role을 포함한 AWS 기본 credential 흐름에서 얻는다.

## 실행 방법

필요한 환경은 JDK 21과 Docker다.

다른 학습 프로젝트가 이미 `8000` port에서 DynamoDB Local을 실행 중이면 먼저 종료한다.

```bash
cd theory/04-query-vs-scan
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

`QueryVsScanDynamoDbLocalTest`의 핵심 검증은 세 가지다.

1. Query와 Scan이 같은 owner-1 task 두 건을 반환한다.
2. 두 결과의 `Count`는 모두 2다.
3. Query의 `ScannedCount`는 2이고 Scan의 `ScannedCount`는 5다.

테스트가 실행되지 않고 skip된다면 `localhost:8000`의 DynamoDB Local 연결부터 확인한다.

## 운영 환경에서 기억할 점

application의 일반적인 단건 조회는 `GetItem`, 목록 조회는 `Query`를 우선한다.

필요한 조회가 현재 key로 `Query`되지 않는다면 바로 Scan을 추가하기 전에 다음을 확인한다.

- 어떤 조건으로 데이터를 찾는가
- 어떤 순서로 결과가 필요한가
- 이 조회 요구사항을 primary key나 index로 표현할 수 있는가

Scan이 항상 금지되는 것은 아니다.
작은 table의 관리 작업, 일회성 분석, 데이터 점검처럼 전체 item을 읽는 목적이 분명한 작업에는 사용할 수 있다.
다만 운영 application의 빈번한 요청 경로에서는 데이터 증가에 따른 비용과 지연을 먼저 검토해야 한다.

## 이번 주제에서 다루지 않는 것

한 번에 너무 많은 개념을 배우지 않도록 다음 내용은 뒤의 주제로 미룬다.

- sort key 범위 조건
- pagination과 `LastEvaluatedKey`
- GSI를 이용한 추가 조회 조건
- parallel Scan
- 정확한 read capacity 계산
- 성능 benchmark

## 확인 질문

1. item과 attribute의 차이는 무엇인가?
2. Query에 partition key 조건이 필요한 이유는 무엇인가?
3. FilterExpression을 사용한 Scan의 `Count`와 `ScannedCount`가 다른 이유는 무엇인가?
4. 이 예제에서 Query와 Scan이 반환하는 task는 왜 같은가?
5. 새로운 목록 요구사항을 Query로 표현할 수 없다면 무엇부터 다시 확인해야 하는가?

## 공식 문서

- [DynamoDB Query](https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Query.html)
- [DynamoDB Scan](https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Scan.html)
- [DynamoDB expression](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.html)

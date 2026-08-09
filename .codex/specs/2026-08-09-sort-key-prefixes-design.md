# Sort Key Prefixes 이론 프로젝트 설계

## 목표

`theory/05-sort-key-prefixes`를 독립적인 non-web Spring Boot 프로젝트로 만든다.

학습자는 같은 partition에 있는 `TASK#...` item과 `STATS` item 중에서 `begins_with` sort key 조건으로 task item만 조회하며, sort key prefix가 DynamoDB 기능이 아니라 application이 정하는 key naming convention임을 이해한다.

## 선택한 방식

실제 DynamoDB Local의 `owner-1` partition에 item 세 건을 저장한다.

- `TASK#task-1`: 첫 번째 task item
- `TASK#task-2`: 두 번째 task item
- `STATS`: task가 아닌 item

조회는 다음 `KeyConditionExpression`을 사용한다.

```text
ownerId = :ownerId AND begins_with(itemKey, :prefix)
```

문자열 생성 함수만 단위 테스트하면 DynamoDB Query 동작을 확인할 수 없다. 반대로 여러 domain type과 복합 key 구조를 추가하면 다음 주제인 single-table key design과 겹친다. 따라서 이번 주제는 key naming convention과 `begins_with` Query 한 가지에 집중한다.

## 프로젝트 구조

- `SortKeyPrefixesApplication`: Spring Boot application marker
- `DynamoDbConfig`: blocking `DynamoDbClient` bean과 local/production 설정
- `DynamoDbAttributeMapper`: 문자열 attribute 변환
- `ItemKeyFactory`: `TASK#{taskId}`와 `STATS` key 생성 규칙을 가진 Spring component
- `SortKeyPrefixService`: demo item 저장과 task prefix Query 실행
- `ItemKeyFactoryTest`: key naming convention 고정
- `SortKeyPrefixesApplicationTest`: Spring context 검증
- `SortKeyPrefixesDynamoDbLocalTest`: 실제 prefix Query 결과 검증

controller와 runner는 만들지 않는다. 실행은 test로만 확인한다.

## Public API

```kotlin
class ItemKeyFactory {
    fun task(taskId: String): String
    fun stats(): String
}

fun saveDemoItems()
fun findTaskItems(ownerId: String): List<Map<String, String>>
```

## 데이터와 흐름

table의 primary key는 `ownerId` partition key와 `itemKey` sort key로 구성한다.

테스트는 다음 순서로 실행한다.

1. table이 없으면 생성한다.
2. `owner-1`에 task item 두 건과 stats item 한 건을 저장한다.
3. partition key가 `owner-1`이고 sort key가 `TASK#`로 시작하는 item을 Query한다.
4. task item 두 건만 반환되고 `STATS` item은 포함되지 않는지 검증한다.

## 테스트 기준

- `ItemKeyFactory.task("task-1")`은 `TASK#task-1`을 반환한다.
- `ItemKeyFactory.stats()`는 `STATS`를 반환한다.
- prefix Query는 `TASK#task-1`, `TASK#task-2` 두 item만 반환한다.
- prefix Query 결과에 `STATS` item이 포함되지 않는다.
- DynamoDB Local 연동 테스트가 skip되지 않는다.

## 학습 범위

이번 주제에서는 다음만 다룬다.

- sort key prefix와 구분자
- prefix가 application convention이라는 점
- `begins_with` sort key key condition
- key 생성 규칙을 Spring component 한 곳에 모으는 이유
- production key naming convention을 변경하기 어려운 이유

다음은 제외한다.

- full single-table design
- 여러 domain item shape
- hierarchy와 복합 prefix
- GSI
- transaction
- pagination
- sort key 범위와 정렬 방향

## 문서와 주석

README는 table, item, partition key, sort key를 다시 짧게 정의하고 prefix, 구분자, `begins_with`, key condition을 초보자 기준으로 설명한다.

코드 주석은 key 값과 Query builder line이 이번 주제에서 어떤 의미인지 한국어로 설명한다. Kotlin, JUnit, 일반 Spring 문법은 설명하지 않는다.

## 운영 관점

sort key prefix는 application이 해석하는 데이터 규칙이다. production에 저장된 key 형식을 바꾸려면 기존 item migration과 구버전 application 호환성까지 고려해야 한다. 따라서 key 생성 규칙을 한 component에 모으고 README와 테스트로 고정한다.

# Query vs Scan 이론 프로젝트 설계

## 목표

`theory/04-query-vs-scan`을 독립적인 non-web Spring Boot 프로젝트로 만든다.

학습자는 같은 owner의 task 목록을 `Query`와 `Scan + FilterExpression`으로 각각 조회하고, 반환 item은 같아도 평가한 item 수가 다를 수 있음을 `Count`와 `ScannedCount`로 확인한다.

## 선택한 방식

실제 DynamoDB Local에 owner-1 task 두 건과 owner-2 task 세 건을 저장한다.

- `Query`: `ownerId = :ownerId` key condition으로 owner-1 partition만 읽는다.
- `Scan`: table 전체를 읽은 뒤 `ownerId = :ownerId` filter로 owner-1 item만 반환한다.

응답 시간 benchmark는 작은 local 데이터에서 결과가 불안정하므로 제외한다. mock으로 request builder만 검사하는 방식도 실제 DynamoDB 읽기 결과를 보여주지 못하므로 제외한다.

## 프로젝트 구조

- `QueryVsScanApplication`: Spring Boot application marker
- `DynamoDbConfig`: blocking `DynamoDbClient` bean과 local/production 설정
- `DynamoDbAttributeMapper`: 문자열 attribute 변환
- `ItemKeys`: `TASK#{taskId}` sort key 생성
- `ReadResult`: item 목록, `count`, `scannedCount`를 함께 반환
- `QueryVsScanService`: demo item 저장, Query 조회, Scan 조회
- `QueryVsScanApplicationTest`: Spring context 검증
- `QueryVsScanDynamoDbLocalTest`: 실제 Query와 Scan 결과 비교

controller와 runner는 만들지 않는다. 실행은 test로만 확인한다.

## Public API

```kotlin
data class ReadResult(
    val items: List<Map<String, String>>,
    val count: Int,
    val scannedCount: Int,
)

fun saveDemoTasks()
fun queryTasksByOwner(ownerId: String): ReadResult
fun scanTasksByOwner(ownerId: String): ReadResult
```

## 테스트 기준

연동 테스트는 다음을 검증한다.

- Query와 Scan이 owner-1 task 두 건을 동일하게 반환한다.
- Query의 `count`와 `scannedCount`는 모두 2다.
- Scan의 `count`는 2지만 `scannedCount`는 5다.
- Scan의 `scannedCount`가 Query보다 크다.
- DynamoDB Local 연동 테스트가 skip되지 않는다.

## 학습 범위

이번 주제에서는 다음만 다룬다.

- Query의 partition key condition
- Scan의 table 전체 평가
- FilterExpression은 읽은 뒤 결과를 거르는 조건이라는 점
- `Count`와 `ScannedCount`의 차이
- 일반 application read path에서 Scan을 피해야 하는 이유

다음은 제외한다.

- pagination과 `LastEvaluatedKey`
- parallel Scan
- GSI
- sort key 상세 조건
- consumed capacity 계산

## 문서와 주석

README는 table, item, partition key, Query, Scan, FilterExpression, Count, ScannedCount를 초보자 기준으로 설명한다.

코드 주석은 각 builder line이 Query 또는 Scan에서 어떤 의미인지 한국어로 설명한다. Kotlin, JUnit, 일반 Spring 문법은 설명하지 않는다.

## 운영 관점

일반 application 목록 조회는 partition key 기반 Query로 설계한다. 필요한 조회를 Query로 표현할 수 없다면 Scan을 바로 추가하지 않고 access pattern과 key design을 다시 검토한다.

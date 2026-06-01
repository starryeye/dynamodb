# RDB vs DynamoDB

이 문서는 DynamoDB를 처음 배울 때 가장 먼저 바꿔야 하는 관점을 정리한다.

## 설계 출발점

| 관점 | RDB/MySQL | DynamoDB |
| --- | --- | --- |
| 시작점 | entity, relation, normalization | API, 화면, use case, access pattern |
| 조회 방식 | SQL로 다양한 조건 조합 | key 기반 `GetItem`, `Query` 중심 |
| schema | table column과 relation을 명확히 정의 | item마다 attribute가 다를 수 있음 |
| index | query 최적화를 위해 추가 | access pattern을 만족하는 별도 query path |
| join | 일반적인 모델링 도구 | 없음 |
| transaction | connection/session 기반 transaction | 필요한 item을 명시하는 transaction API |
| 동시성 | lock, isolation level, `@Version` | condition expression, optimistic locking |

## 같은 요구사항을 다르게 생각하기

요구사항:

```text
owner가 가진 task 목록을 생성일 역순으로 보여준다.
```

RDB에서는 보통 이렇게 생각한다.

```text
tasks table을 만들고 owner_id, created_at index를 추가한다.
WHERE owner_id = ?
ORDER BY created_at DESC
```

DynamoDB에서는 먼저 이렇게 묻는다.

```text
이 목록 조회는 어떤 partition key로 Query할 수 있는가?
생성일 역순 정렬은 어떤 sort key로 표현할 것인가?
기본 table key로 가능한가, GSI가 필요한가?
다음 페이지는 어떤 LastEvaluatedKey로 이어갈 것인가?
```

## 이 프로젝트에 적용하기

TaskApp의 핵심 access pattern:

| 기능 | DynamoDB에서 먼저 묻는 질문 |
| --- | --- |
| task 생성 | 같은 key의 item이 이미 있으면 어떻게 막을 것인가? |
| task 단건 조회 | `ownerId`와 `taskId`로 바로 `GetItem`할 수 있는가? |
| task 목록 조회 | `ownerId`로 `Query`하고 원하는 정렬을 만들 수 있는가? |
| task 완료 | task 상태 변경과 stats 변경을 원자적으로 묶어야 하는가? |
| stats 조회 | stats item의 key는 무엇인가? |

## 내 말로 정리하기

아래 문장을 완성해본다.

```text
RDB에서는 보통 ________ 를 먼저 설계하지만,
DynamoDB에서는 ________ 를 먼저 정리한다.
```

```text
DynamoDB에서 조회 API를 추가할 때 가장 먼저 확인할 것은
________ 로 조회할 수 있는지 여부다.
```

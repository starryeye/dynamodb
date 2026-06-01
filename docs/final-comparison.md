# 최종 비교

이 문서는 이론 트랙과 실습 트랙을 마친 뒤 세 Stage를 한 번에 비교하기 위한 정리 문서다.

## 큰 흐름

```text
Stage 1: MySQL + Spring MVC + JPA
Stage 2: DynamoDB + Spring MVC
Stage 3: DynamoDB + Spring WebFlux
```

Stage 1에서 RDB/JPA 기준선을 만들고, Stage 2에서 persistence model을 DynamoDB로 바꾼다. Stage 3에서는 DynamoDB 설계는 유지한 채 request 처리와 AWS SDK 호출 방식을 reactive/non-blocking으로 바꾼다.

## 프로젝트 경로

문서와 실제 프로젝트는 같은 경로 패턴을 따른다.

| 문서 | 프로젝트 |
| --- | --- |
| `docs/theory/01-table-item-key.md` | `theory/01-table-item-key/` |
| `docs/theory/08-transactions.md` | `theory/08-transactions/` |
| `docs/practice/stage1-mysql-mvc.md` | `practice/stage1-mysql-mvc/` |
| `docs/practice/stage2-dynamodb-mvc.md` | `practice/stage2-dynamodb-mvc/` |
| `docs/practice/stage3-dynamodb-webflux.md` | `practice/stage3-dynamodb-webflux/` |

## 설계 비교

| 주제 | Stage 1 | Stage 2 | Stage 3 |
| --- | --- | --- | --- |
| 데이터 모델링 출발점 | entity와 relation | access pattern과 key design | Stage 2와 동일 |
| 저장소 | MySQL table | DynamoDB single table | DynamoDB single table |
| 주요 key | PK, unique key, index | partition key, sort key, GSI | Stage 2와 동일 |
| 단건 조회 | SQL/JPA repository | `GetItem` | async `GetItem` |
| 목록 조회 | SQL cursor query | GSI `Query` | async GSI `Query` |
| pagination | `createdAt + taskId` cursor | `LastEvaluatedKey` cursor | Stage 2와 동일 |
| 동시성 | JPA `@Version` | condition expression | condition expression |
| 다중 item 원자성 | `@Transactional` | `TransactWriteItems` | async `TransactWriteItems` |
| HTTP stack | Spring MVC | Spring MVC | Spring WebFlux |
| DynamoDB client | 없음 | `DynamoDbClient` | `DynamoDbAsyncClient` |

## 핵심 학습 포인트

Stage 1에서 배울 것:

- RDB schema와 index 중심 설계
- JPA entity와 domain model 분리
- `@Transactional` commit/rollback
- `@Version` optimistic locking
- SQL cursor pagination

Stage 2에서 배울 것:

- access pattern에서 시작하는 DynamoDB 설계
- `Query`와 `Scan`의 차이
- single-table design과 sort key prefix
- GSI와 eventual consistency
- `LastEvaluatedKey` 기반 pagination
- conditional write와 optimistic locking
- `TransactWriteItems`로 여러 item을 all-or-nothing 처리
- capacity, hot partition, IAM, backup 같은 production 고려사항

Stage 3에서 배울 것:

- DynamoDB 설계와 execution model은 별개의 문제라는 점
- blocking `DynamoDbClient`와 async `DynamoDbAsyncClient`의 차이
- `CompletableFuture`를 `Mono`로 연결하는 법
- request path에서 `block()`, `join()`, `Future.get()`을 피하는 이유
- `WebTestClient`와 `StepVerifier` 기반 reactive test

## 회고 질문

- 새로운 API를 추가할 때 Stage 1과 Stage 2에서 각각 무엇을 먼저 바꾸는가?
- Stage 2에서 `Scan`이 필요해진다면 어떤 설계를 다시 봐야 하는가?
- `getTask`와 `listTasks`의 consistency가 달라질 수 있는 이유는 무엇인가?
- 모든 write에 transaction을 쓰지 않고 필요한 곳에만 쓰는 이유는 무엇인가?
- Stage 3에서 DynamoDB table design이 바뀌지 않는 이유는 무엇인가?

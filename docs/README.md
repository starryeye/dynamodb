# TaskApp DynamoDB 학습 문서

이 문서는 `Task` 애플리케이션을 사용해 DynamoDB를 입문자 관점에서 단계적으로 학습하기 위한 커리큘럼이다.

문서는 이론 트랙과 실습 트랙으로 나뉜다. 이론 트랙에서는 Spring 없이 DynamoDB의 사고방식을 먼저 익히고, 실습 트랙에서는 같은 도메인과 REST API를 유지한 채 MySQL/JPA 애플리케이션을 DynamoDB, WebFlux로 단계적으로 바꾼다.

## 추천 학습 순서

처음 DynamoDB를 학습한다면 다음 순서로 읽는다.

1. [DynamoDB 입문 개요](./theory/00-overview.md)
2. [Table, Item, Key](./theory/01-table-item-key.md)
3. [Access Pattern](./theory/02-access-patterns.md)
4. [Query vs Scan](./theory/03-query-vs-scan.md)
5. [Key Design](./theory/04-key-design.md)
6. [Stage 1 - MySQL + Spring MVC + JPA](./practice/stage1-mysql-mvc.md)
7. [GSI와 Consistency](./theory/05-gsi-and-consistency.md)
8. [Pagination](./theory/06-pagination.md)
9. [Conditional Write](./theory/07-conditional-write.md)
10. [Transactions](./theory/08-transactions.md)
11. [Stage 2 - DynamoDB + Spring MVC](./practice/stage2-dynamodb-mvc.md)
12. [Capacity와 Production](./theory/09-capacity-and-production.md)
13. [Stage 3 - DynamoDB + Spring WebFlux](./practice/stage3-dynamodb-webflux.md)
14. [최종 비교](./final-comparison.md)

## 이론 트랙

Spring Boot 구현 전에 DynamoDB 자체를 학습한다.

| 문서 | 학습 주제 |
| --- | --- |
| [00-overview](./theory/00-overview.md) | DynamoDB 학습 목표와 RDB와의 사고방식 차이 |
| [01-table-item-key](./theory/01-table-item-key.md) | table, item, attribute, partition key, sort key |
| [02-access-patterns](./theory/02-access-patterns.md) | API/use case에서 DynamoDB 설계를 시작하는 법 |
| [03-query-vs-scan](./theory/03-query-vs-scan.md) | `Query`와 `Scan`의 차이, application path에서 `Scan`을 피하는 이유 |
| [04-key-design](./theory/04-key-design.md) | composite key, prefix, single-table design, hot partition |
| [05-gsi-and-consistency](./theory/05-gsi-and-consistency.md) | GSI, projection, eventual consistency, write cost |
| [06-pagination](./theory/06-pagination.md) | `LastEvaluatedKey`, `ExclusiveStartKey`, API cursor |
| [07-conditional-write](./theory/07-conditional-write.md) | condition expression, duplicate 방지, version conflict |
| [08-transactions](./theory/08-transactions.md) | `TransactWriteItems`, stats 동시 갱신, 예외 매핑 |
| [09-capacity-and-production](./theory/09-capacity-and-production.md) | capacity mode, RCU/WCU, IAM, backup, monitoring |

## 실습 트랙

이론에서 배운 내용을 같은 `Task` 애플리케이션에 적용한다.

| 문서 | 실습 목표 |
| --- | --- |
| [공통 요구사항](./requirements.md) | 세 Stage가 공유하는 도메인, API, 비즈니스 규칙 |
| [Stage 1 - MySQL + Spring MVC + JPA](./practice/stage1-mysql-mvc.md) | RDB/JPA 기준선 구현 |
| [Stage 2 - DynamoDB + Spring MVC](./practice/stage2-dynamodb-mvc.md) | DynamoDB table, GSI, condition, transaction으로 persistence 변경 |
| [Stage 3 - DynamoDB + Spring WebFlux](./practice/stage3-dynamodb-webflux.md) | DynamoDB 모델은 유지하고 실행 모델을 reactive로 변경 |
| [테스트 및 산출물 요구사항](./practice/testing-and-deliverables.md) | 테스트와 최종 산출물 기준 |

## 프로젝트 구조

문서 경로와 실제 프로젝트 경로는 같은 패턴을 따른다.

```text
docs/theory/01-table-item-key.md      <-> theory/01-table-item-key/
docs/practice/stage2-dynamodb-mvc.md  <-> practice/stage2-dynamodb-mvc/
```

이론 트랙의 각 주제도 서로 독립된 프로젝트로 둔다. 각 프로젝트는 해당 개념을 실험하는 가장 작은 단위의 코드, 테스트, 스크립트, README를 가진다.

```text
theory/
  00-overview/
  01-table-item-key/
  02-access-patterns/
  03-query-vs-scan/
  04-key-design/
  05-gsi-and-consistency/
  06-pagination/
  07-conditional-write/
  08-transactions/
  09-capacity-and-production/
```

실습 트랙의 세 단계는 멀티 모듈이 아니라 서로 독립된 Gradle 프로젝트로 구성한다. 각 Stage는 자체 `build.gradle.kts`, Gradle wrapper, 설정 파일, Docker Compose 파일을 가진다.

```text
practice/
  stage1-mysql-mvc/
  stage2-dynamodb-mvc/
  stage3-dynamodb-webflux/
```

각 practice Stage는 다음 파일과 디렉터리를 포함한다.

- `build.gradle.kts`
- `docker-compose.yml`
- `src/main/kotlin`
- `src/test/kotlin`
- `application-local.yml`
- `application-prod.yml`
- `README.md`

세 프로젝트는 서로 코드를 공유하지 않는다. 대신 패키지 구조, 클래스명, DTO명, service/repository 메서드명을 최대한 맞춰서 파일을 나란히 열고 비교하기 쉽게 만든다.

## 핵심 비교 포인트

- MySQL/JPA의 스키마, 트랜잭션, 락, 커서 페이지네이션
- DynamoDB의 접근 패턴 기반 모델링, 조건부 쓰기, 트랜잭션, `LastEvaluatedKey` 페이지네이션
- Spring MVC의 동기 실행 모델과 WebFlux의 비동기/논블로킹 실행 모델
- 같은 비즈니스 규칙을 유지하면서 persistence adapter와 repository 구현이 어떻게 달라지는지

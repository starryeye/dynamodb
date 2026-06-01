# TaskApp DynamoDB 학습 문서

이 문서는 `Task` 애플리케이션을 사용해 DynamoDB를 입문자 관점에서 단계적으로 학습하기 위한 커리큘럼이다.

문서는 이론 트랙과 실습 트랙으로 나뉜다. 이론 트랙에서는 작은 Spring Boot 프로젝트로 DynamoDB 개념과 연동 방법을 함께 익히고, 실습 트랙에서는 같은 도메인과 REST API를 유지한 채 MySQL/JPA 애플리케이션을 DynamoDB, WebFlux로 단계적으로 바꾼다.

## 추천 학습 순서

처음 DynamoDB를 학습한다면 다음 순서로 읽는다.

1. [DynamoDB 입문 개요](./theory/00-overview.md)
2. [Table, Item, Key](./theory/01-table-item-key.md)
3. [Item Collection Query](./theory/02-item-collection-query.md)
4. [Access Pattern](./theory/03-access-patterns.md)
5. [Query vs Scan](./theory/04-query-vs-scan.md)
6. [Sort Key Prefixes](./theory/05-sort-key-prefixes.md)
7. [Single-table Key Design](./theory/06-single-table-key-design.md)
8. [Stage 1 - MySQL + Spring MVC + JPA](./practice/stage1-mysql-mvc.md)
9. [GSI Basics](./theory/07-gsi-basics.md)
10. [GSI Consistency](./theory/08-gsi-consistency.md)
11. [LastEvaluatedKey](./theory/09-last-evaluated-key.md)
12. [API Cursor](./theory/10-api-cursor.md)
13. [Conditional Put](./theory/11-conditional-put.md)
14. [Versioned Update](./theory/12-versioned-update.md)
15. [Transaction Basics](./theory/13-transaction-basics.md)
16. [Transaction Failures](./theory/14-transaction-failures.md)
17. [Stage 2 - DynamoDB + Spring MVC](./practice/stage2-dynamodb-mvc.md)
18. [Capacity and Cost](./theory/15-capacity-and-cost.md)
19. [Credentials and IAM](./theory/16-credentials-and-iam.md)
20. [Backup and Monitoring](./theory/17-backup-monitoring.md)
21. [Stage 3 - DynamoDB + Spring WebFlux](./practice/stage3-dynamodb-webflux.md)
22. [최종 비교](./final-comparison.md)

## 이론 트랙

이론 트랙은 각 주제를 독립적인 최소 Spring Boot 프로젝트로 학습한다.

기본 스택은 Spring MVC 기반 Servlet stack을 사용한다. Spring 공식 문서에서도 Spring MVC는 Servlet API 기반의 original web framework이고, WebFlux는 이후 추가된 reactive stack이므로 입문 단계에서는 Spring MVC와 blocking `DynamoDbClient`를 먼저 배운다. WebFlux와 async client는 Stage 3에서 비교한다.

| 문서 | 학습 주제 |
| --- | --- |
| [00-overview](./theory/00-overview.md) | DynamoDB 학습 목표, Spring 연동 기준, RDB와의 사고방식 차이 |
| [01-table-item-key](./theory/01-table-item-key.md) | table, item, full primary key, `GetItem` |
| [02-item-collection-query](./theory/02-item-collection-query.md) | partition key 기반 item collection `Query` |
| [03-access-patterns](./theory/03-access-patterns.md) | API/use case와 DynamoDB operation 매핑 |
| [04-query-vs-scan](./theory/04-query-vs-scan.md) | request path에서 `Query`와 교육용 `Scan` 비교 |
| [05-sort-key-prefixes](./theory/05-sort-key-prefixes.md) | `TASK#...`, `STATS` 같은 sort key prefix |
| [06-single-table-key-design](./theory/06-single-table-key-design.md) | single-table item shape와 hot partition 감각 |
| [07-gsi-basics](./theory/07-gsi-basics.md) | GSI를 별도 read path로 추가하는 법 |
| [08-gsi-consistency](./theory/08-gsi-consistency.md) | GSI eventual consistency와 write cost |
| [09-last-evaluated-key](./theory/09-last-evaluated-key.md) | DynamoDB 원본 pagination key |
| [10-api-cursor](./theory/10-api-cursor.md) | API cursor 인코딩/검증 |
| [11-conditional-put](./theory/11-conditional-put.md) | `attribute_not_exists`로 중복 생성 방지 |
| [12-versioned-update](./theory/12-versioned-update.md) | `expectedVersion` 기반 update conflict |
| [13-transaction-basics](./theory/13-transaction-basics.md) | 두 item을 all-or-nothing으로 쓰기 |
| [14-transaction-failures](./theory/14-transaction-failures.md) | cancellation reason, idempotency token, 예외 매핑 |
| [15-capacity-and-cost](./theory/15-capacity-and-cost.md) | on-demand/provisioned, RCU/WCU, GSI/transaction cost |
| [16-credentials-and-iam](./theory/16-credentials-and-iam.md) | local/prod credential, endpoint override, least privilege |
| [17-backup-monitoring](./theory/17-backup-monitoring.md) | PITR, backup, deletion protection, alarm, metrics |

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

이론 트랙의 각 주제도 서로 독립된 프로젝트로 둔다. 각 프로젝트는 해당 개념을 실험하는 가장 작은 Spring Boot 애플리케이션, 테스트, Docker Compose, README를 가진다.

모든 theory 프로젝트는 Spring Boot MVC 기반으로 작성한다. 첫 주제부터 같은 실행 모델을 사용해 controller, service, configuration, profile, local/prod 차이를 반복해서 익힌다.

```text
theory/
  00-overview/
  01-table-item-key/
  02-item-collection-query/
  03-access-patterns/
  04-query-vs-scan/
  05-sort-key-prefixes/
  06-single-table-key-design/
  07-gsi-basics/
  08-gsi-consistency/
  09-last-evaluated-key/
  10-api-cursor/
  11-conditional-put/
  12-versioned-update/
  13-transaction-basics/
  14-transaction-failures/
  15-capacity-and-cost/
  16-credentials-and-iam/
  17-backup-monitoring/
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

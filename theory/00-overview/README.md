# DynamoDB 입문 개요

이 이론 트랙은 작은 Spring Boot 프로젝트를 통해 DynamoDB의 사고방식과 애플리케이션 연동 방법을 함께 익히기 위한 문서다.

프로젝트 위치:

```text
theory/00-overview/
```

목표는 DynamoDB 문법을 많이 외우는 것이 아니라, 다음 질문에 스스로 답할 수 있게 되는 것이다.

- 이 API는 어떤 key로 조회할 수 있는가?
- `Query`로 해결되는가, `Scan`이 필요한가?
- 정렬과 목록 조회를 위해 GSI가 필요한가?
- 중복 생성과 동시 수정은 어떤 condition으로 막을 수 있는가?
- 여러 item을 함께 바꿔야 할 때 transaction이 필요한가?

## RDB와 다른 출발점

MySQL/JPA에서는 보통 entity와 relation을 먼저 설계한 뒤, SQL query를 작성한다.

DynamoDB에서는 반대로 application의 access pattern을 먼저 정리한다. 어떤 화면과 API가 어떤 조건으로 데이터를 읽고 쓰는지 정리한 다음, 그 access pattern을 만족하도록 partition key, sort key, GSI를 설계한다.

```text
RDB 학습 흐름:
entity -> table -> relation -> query

DynamoDB 학습 흐름:
API/use case -> access pattern -> key design -> item shape
```

## 이 프로젝트의 학습 흐름

이론 트랙에서는 non-web Spring Boot 기반의 작은 프로젝트로 DynamoDB 개념을 하나씩 배운다.

```text
Spring Boot + DynamoDB Local 연결
-> Table / Item / Key
-> Item Collection Query
-> Access Pattern과 Operation 매핑
-> Query vs Scan
-> Sort Key Prefixes
-> Single-table Key Design
-> GSI Basics
-> GSI Consistency
-> LastEvaluatedKey
-> API Cursor
-> Conditional Put
-> Versioned Update
-> Transaction Basics
-> Transaction Failures
-> Capacity and Cost
-> Credentials and IAM
-> Backup and Monitoring
```

실습 트랙에서는 기존 Spring Boot 애플리케이션을 단계적으로 바꾼다.

```text
Stage 1: MySQL + Spring MVC + JPA
Stage 2: DynamoDB + Spring MVC
Stage 3: DynamoDB + Spring WebFlux
```

## 최종적으로 이해해야 하는 문장

DynamoDB는 SQL을 다른 문법으로 쓰는 데이터베이스가 아니다. DynamoDB는 미리 정의한 key access pattern을 매우 빠르고 안정적으로 처리하기 위해 사용하는 key-value/document database다.

## 기본 Spring 실행 모델

이론 트랙의 기본 실행 모델은 non-web Spring Boot 애플리케이션이다.

각 theory 프로젝트는 service, configuration, tests로 DynamoDB 연동을 확인한다. REST API와 controller 설계는 practice Stage에서 다룬다.

운영 환경 관점도 각 주제에 포함한다. local에서는 DynamoDB Local, endpoint override, dummy credential을 사용하지만 production에서는 IAM role 또는 default credential provider를 사용하고 table 생성은 IaC로 관리하는 방향을 기본으로 둔다.

## 완료 기준

이 주제를 마치면 다음을 자기 말로 설명할 수 있어야 한다.

- RDB는 보통 entity와 relation에서 시작하지만 DynamoDB는 access pattern에서 시작한다.
- DynamoDB에서 table 설계는 API/use case와 분리해서 생각하기 어렵다.
- `Query`로 풀 수 없는 요구사항은 key design 또는 GSI 설계를 다시 봐야 한다.
- Stage 1, 2, 3은 각각 데이터 모델, 저장소 모델, 실행 모델의 차이를 비교하기 위한 실습이다.
- theory 프로젝트는 DynamoDB 개념뿐 아니라 Spring Boot configuration, profile, local/prod 차이까지 함께 배운다.
- `theory/00-overview`는 코드 실습 전 오리엔테이션이다. non-web Spring Boot 실습은 `01-table-item-key`부터 시작한다.

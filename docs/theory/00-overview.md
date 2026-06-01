# DynamoDB 입문 개요

이 이론 트랙은 Spring Boot 코드를 작성하기 전에 DynamoDB의 사고방식을 먼저 익히기 위한 문서다.

실습 위치:

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

이론 트랙에서는 Spring을 잠시 잊고 DynamoDB 자체를 배운다.

```text
Table / Item / Key
-> Access Pattern
-> Query vs Scan
-> Key Design
-> GSI와 Consistency
-> Pagination
-> Conditional Write
-> Transaction
-> Capacity와 Production
```

실습 트랙에서는 기존 Spring Boot 애플리케이션을 단계적으로 바꾼다.

```text
Stage 1: MySQL + Spring MVC + JPA
Stage 2: DynamoDB + Spring MVC
Stage 3: DynamoDB + Spring WebFlux
```

## 최종적으로 이해해야 하는 문장

DynamoDB는 SQL을 다른 문법으로 쓰는 데이터베이스가 아니다. DynamoDB는 미리 정의한 key access pattern을 매우 빠르고 안정적으로 처리하기 위해 사용하는 key-value/document database다.

## 완료 기준

이 주제를 마치면 다음을 자기 말로 설명할 수 있어야 한다.

- RDB는 보통 entity와 relation에서 시작하지만 DynamoDB는 access pattern에서 시작한다.
- DynamoDB에서 table 설계는 API/use case와 분리해서 생각하기 어렵다.
- `Query`로 풀 수 없는 요구사항은 key design 또는 GSI 설계를 다시 봐야 한다.
- Stage 1, 2, 3은 각각 데이터 모델, 저장소 모델, 실행 모델의 차이를 비교하기 위한 실습이다.

# 00 Overview

문서: [DynamoDB 입문 개요](../../docs/theory/00-overview.md)

이 프로젝트는 DynamoDB 학습 흐름과 RDB와의 사고방식 차이를 정리하는 독립 프로젝트다.

이 주제도 Spring Boot MVC 애플리케이션으로 실행한다. DynamoDB 요청은 아직 보내지 않지만, 앞으로 모든 theory 프로젝트가 사용할 기본 실행 모델과 학습 방향을 HTTP API로 확인한다.

## 학습 목표

- RDB와 DynamoDB의 설계 출발점 차이를 설명한다.
- 이론 트랙과 실습 트랙의 관계를 이해한다.
- theory 프로젝트의 기본 스택이 Spring MVC 기반 Servlet stack인 이유를 이해한다.
- Stage 1, 2, 3이 각각 무엇을 비교하기 위한 단계인지 말할 수 있다.
- 앞으로 각 주제에서 어떤 질문을 던져야 하는지 정리한다.
- `gradle bootRun`으로 최소 Spring Boot 앱을 실행하고 `/overview` 응답을 확인한다.

## 파일

| 파일 | 설명 |
| --- | --- |
| [learning-checklist.md](./learning-checklist.md) | 이 주제의 학습 체크리스트 |
| [rdb-vs-dynamodb.md](./rdb-vs-dynamodb.md) | RDB와 DynamoDB 사고방식 비교 |
| [reflection.md](./reflection.md) | 자기 말로 정리하는 회고 템플릿 |
| `src/main/kotlin` | 최소 Spring Boot MVC 애플리케이션 |

## 실행 방법

테스트를 실행한다.

```bash
gradle test
```

Spring Boot 애플리케이션을 실행한다.

```bash
gradle bootRun
```

overview API를 호출한다.

```bash
curl http://localhost:8080/overview
```

## 진행 순서

1. [문서](../../docs/theory/00-overview.md)를 읽는다.
2. `gradle test`로 Spring Boot test를 실행한다.
3. `gradle bootRun`으로 앱을 실행하고 `/overview`를 호출한다.
4. [RDB와 DynamoDB 비교](./rdb-vs-dynamodb.md)를 채운다.
5. [학습 체크리스트](./learning-checklist.md)를 확인한다.
6. [회고 템플릿](./reflection.md)에 답을 적는다.

## 완료 기준

다음 문장을 자기 말로 설명할 수 있으면 완료다.

```text
DynamoDB는 query를 나중에 자유롭게 만드는 데이터베이스가 아니라,
미리 정리한 access pattern을 key design으로 표현하는 데이터베이스다.
```

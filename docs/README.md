# TaskApp 학습 프로젝트 문서

이 문서는 `Task` 애플리케이션을 세 단계로 발전시키며 MySQL/JPA, DynamoDB, WebFlux의 차이를 학습하기 위한 프로젝트 명세를 정리한다.

목표는 같은 도메인, 같은 REST API, 같은 계층 구조, 최대한 비슷한 메서드 이름을 유지한 채 애플리케이션이 어떻게 달라지는지 비교하기 쉽게 만드는 것이다.

## 문서 목록

- [공통 요구사항](./requirements.md)
- [Stage 1 - MySQL + Spring MVC + JPA](./stage1-mysql-mvc.md)
- [Stage 2 - DynamoDB + Spring MVC](./stage2-dynamodb-mvc.md)
- [Stage 3 - DynamoDB + Spring WebFlux](./stage3-dynamodb-webflux.md)
- [테스트 및 산출물 요구사항](./testing-and-deliverables.md)

## 학습 흐름

1. Stage 1에서 관계형 데이터베이스와 JPA 기반의 전통적인 Spring MVC 애플리케이션을 만든다.
2. Stage 2에서 동일한 비즈니스 기능을 DynamoDB 단일 테이블 모델과 AWS SDK v2로 옮긴다.
3. Stage 3에서 DynamoDB 모델은 유지하되 실행 모델을 WebFlux와 비동기 DynamoDB 클라이언트로 바꾼다.

## 프로젝트 구조

세 단계는 멀티 모듈이 아니라 서로 독립된 Gradle 프로젝트로 구성한다. 각 Stage는 자체 `build.gradle.kts`, Gradle wrapper, 설정 파일, Docker Compose 파일을 가진다.

```text
stage1-mysql-mvc/
stage2-dynamodb-mvc/
stage3-dynamodb-webflux/
```

각 Stage는 다음 파일과 디렉터리를 포함한다.

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

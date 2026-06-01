# DynamoDB 학습 프로젝트

Spring Boot 애플리케이션을 MySQL/JPA에서 DynamoDB로 옮기고, 다시 WebFlux 실행 모델로 바꾸며 DynamoDB를 단계적으로 학습하는 프로젝트다.

학습 문서는 두 트랙으로 나뉜다.

- [이론 트랙](./docs/README.md#이론-트랙): 작은 Spring Boot 프로젝트로 DynamoDB의 table, key, Query, GSI, pagination, condition, transaction과 연동 방법을 함께 학습한다.
- [실습 트랙](./docs/README.md#실습-트랙): 같은 Task 애플리케이션을 Stage 1, 2, 3으로 변환하며 구현한다.

루트의 실제 프로젝트도 같은 방식으로 나뉜다.

- `theory/*`: 이론 주제별 독립 Spring Boot DynamoDB 연동 프로젝트
- `practice/*`: Stage별 독립 애플리케이션 프로젝트

처음 입문자는 [docs/README.md](./docs/README.md)부터 읽는다.

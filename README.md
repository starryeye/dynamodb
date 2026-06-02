# DynamoDB 학습 프로젝트

Spring Boot 애플리케이션을 MySQL/JPA에서 DynamoDB로 옮기고, 다시 WebFlux 실행 모델로 바꾸며 DynamoDB를 단계적으로 학습하는 프로젝트다.

학습 내용은 `docs/`가 아니라 실제 프로젝트 안의 `README.md`에서 읽는다.

- [이론 트랙](./theory/): DynamoDB의 table, key, Query, GSI, pagination, condition, transaction과 Spring 연동 방법을 작은 프로젝트로 학습한다.
- [실습 트랙](./practice/): 같은 Task 애플리케이션을 Stage 1, 2, 3으로 변환하며 MySQL/JPA, DynamoDB, WebFlux 차이를 비교한다.

## 추천 학습 순서

1. [00 Overview](./theory/00-overview/)
2. [01 Table, Item, Key](./theory/01-table-item-key/)
3. [02 Item Collection Query](./theory/02-item-collection-query/)
4. [03 Access Patterns](./theory/03-access-patterns/)
5. [04 Query vs Scan](./theory/04-query-vs-scan/)
6. [05 Sort Key Prefixes](./theory/05-sort-key-prefixes/)
7. [06 Single-table Key Design](./theory/06-single-table-key-design/)
8. [Stage 1 - MySQL + Spring MVC + JPA](./practice/stage1-mysql-mvc/)
9. [07 GSI Basics](./theory/07-gsi-basics/)
10. [08 GSI Consistency](./theory/08-gsi-consistency/)
11. [09 LastEvaluatedKey](./theory/09-last-evaluated-key/)
12. [10 API Cursor](./theory/10-api-cursor/)
13. [11 Conditional Put](./theory/11-conditional-put/)
14. [12 Versioned Update](./theory/12-versioned-update/)
15. [13 Transaction Basics](./theory/13-transaction-basics/)
16. [14 Transaction Failures](./theory/14-transaction-failures/)
17. [Stage 2 - DynamoDB + Spring MVC](./practice/stage2-dynamodb-mvc/)
18. [15 Capacity and Cost](./theory/15-capacity-and-cost/)
19. [16 Credentials and IAM](./theory/16-credentials-and-iam/)
20. [17 Backup and Monitoring](./theory/17-backup-monitoring/)
21. [Stage 3 - DynamoDB + Spring WebFlux](./practice/stage3-dynamodb-webflux/)
22. [최종 비교](./practice/#최종-비교)

## 프로젝트 구조

```text
theory/
  00-overview/
  01-table-item-key/
  ...
  17-backup-monitoring/

practice/
  stage1-mysql-mvc/
  stage2-dynamodb-mvc/
  stage3-dynamodb-webflux/
```

각 디렉터리의 `README.md`가 해당 주제의 학습 본문이다.

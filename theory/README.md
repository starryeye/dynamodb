# Theory Projects

이 디렉터리는 DynamoDB 이론 주제별 독립 학습 프로젝트를 담는다.

각 프로젝트의 `README.md`가 해당 주제의 학습 본문이다.

## 학습 순서

1. [00 Overview](./00-overview/)
2. [01 Table, Item, Key](./01-table-item-key/)
3. [02 Item Collection Query](./02-item-collection-query/)
4. [03 Access Patterns](./03-access-patterns/)
5. [04 Query vs Scan](./04-query-vs-scan/)
6. [05 Sort Key Prefixes](./05-sort-key-prefixes/)
7. [06 Single-table Key Design](./06-single-table-key-design/)
8. [07 GSI Basics](./07-gsi-basics/)
9. [08 GSI Consistency](./08-gsi-consistency/)
10. [09 LastEvaluatedKey](./09-last-evaluated-key/)
11. [10 API Cursor](./10-api-cursor/)
12. [11 Conditional Put](./11-conditional-put/)
13. [12 Versioned Update](./12-versioned-update/)
14. [13 Transaction Basics](./13-transaction-basics/)
15. [14 Transaction Failures](./14-transaction-failures/)
16. [15 Capacity and Cost](./15-capacity-and-cost/)
17. [16 Credentials and IAM](./16-credentials-and-iam/)
18. [17 Backup and Monitoring](./17-backup-monitoring/)

## 실행 모델

`00-overview`는 오리엔테이션 문서이고, `01-table-item-key` 이후의 기본 실행 모델은 non-web Spring Boot와 blocking `DynamoDbClient`다.

각 주제는 다음을 함께 배운다.

- DynamoDB 개념
- Spring Boot configuration과 bean 등록
- test를 통한 작은 실행 흐름
- local profile과 DynamoDB Local 연동
- prod profile에서 endpoint override와 dummy credential을 제거하는 이유
- 해당 주제와 연결되는 운영 포인트

각 주제의 자세한 설명과 실행 방법은 각 프로젝트의 `README.md`에서 확인한다.

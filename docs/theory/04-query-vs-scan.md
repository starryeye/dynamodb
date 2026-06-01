# Query vs Scan

DynamoDB 입문에서 가장 먼저 확실히 구분해야 하는 것은 `Query`와 `Scan`이다.

## Query

`Query`는 partition key를 기준으로 item을 찾는다. sort key 조건을 함께 사용할 수 있고, 정렬 방향도 지정할 수 있다.

이 프로젝트에서는 task 목록을 조회할 때 `Query`를 사용한다.

```text
Index: OwnerCreatedAtIndex
PK: ownerId = owner-1
SK: createdAtTaskId 기준 정렬
```

## Scan

`Scan`은 table 또는 index의 item을 훑는다. 작은 실험이나 admin tool에서는 사용할 수 있지만, 일반 request path에서는 피해야 한다.

`FilterExpression`을 붙여도 먼저 읽고 나중에 거르는 방식이기 때문에, 비용과 지연 시간 문제가 해결되지 않는다.

## 이 프로젝트의 규칙

- application read path에서는 `Scan`을 사용하지 않는다.
- 단건 조회는 `GetItem`을 사용한다.
- 목록 조회는 `Query`를 사용한다.
- 필요한 조회가 `Query`로 표현되지 않으면 access pattern과 key design을 다시 본다.

## 실습 아이디어

DynamoDB Local에서 작은 데이터를 넣고 다음을 비교한다.

```text
1. ownerId로 Query
2. 전체 table Scan 후 ownerId filter
3. item 수를 늘린 뒤 읽은 item 수와 응답 시간을 비교
```

## 확인 질문

- `FilterExpression`은 read capacity 사용량을 줄여주는가?
- `Query`에는 왜 partition key 조건이 필요한가?
- 이 프로젝트의 REST API 중 `Scan`이 필요한 API가 있는가?


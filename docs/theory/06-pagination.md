# Pagination

DynamoDB의 pagination은 offset이 아니라 key 기반이다.

## LastEvaluatedKey

DynamoDB `Query`는 한 번에 모든 결과를 반환하지 않을 수 있다. 다음 페이지가 있으면 응답에 `LastEvaluatedKey`가 포함된다.

다음 요청에서는 이 값을 `ExclusiveStartKey`로 전달한다.

```text
첫 요청:
Query(ownerId = owner-1, limit = 10)

응답:
items = 10개
LastEvaluatedKey = {...}

다음 요청:
Query(ownerId = owner-1, limit = 10, ExclusiveStartKey = LastEvaluatedKey)
```

## API Cursor

외부 API에는 DynamoDB key 구조를 그대로 노출하지 않는 것이 좋다. 그래서 이 프로젝트에서는 `LastEvaluatedKey`를 JSON으로 만든 뒤 Base64 URL-safe 문자열로 인코딩한다.

```json
{
  "ownerId": "owner-1",
  "itemKey": "TASK#task-10",
  "createdAtTaskId": "2026-06-01T10:15:30Z#task-10"
}
```

이 값을 API 응답의 `nextCursor`로 내려준다.

## Cursor 검증

cursor decode에 실패하면 `InvalidCursorException`을 발생시킨다.

또한 cursor 안의 `ownerId`가 path variable의 `ownerId`와 다르면 잘못된 cursor로 처리한다. 이렇게 하면 다른 owner의 cursor를 재사용하는 실수를 막을 수 있다.

## MySQL Cursor와의 차이

Stage 1 MySQL은 `createdAt + taskId` 값을 직접 cursor로 사용한다.

Stage 2 DynamoDB는 `LastEvaluatedKey`를 cursor 원본으로 사용한다.

두 방식 모두 offset pagination은 아니지만, DynamoDB cursor는 DynamoDB의 key 기반 pagination 모델에 더 가깝다.

## 확인 질문

- DynamoDB에서 offset pagination을 기본으로 쓰지 않는 이유는 무엇인가?
- `LastEvaluatedKey`와 `ExclusiveStartKey`는 각각 언제 쓰이는가?
- API cursor에 DynamoDB key를 그대로 노출하지 않는 이유는 무엇인가?


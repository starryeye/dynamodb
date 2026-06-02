# API Cursor

이 주제는 DynamoDB `LastEvaluatedKey`를 외부 API cursor로 감싸는 단계다.

프로젝트 위치:

```text
theory/10-api-cursor/
```

## 이번 주제에서 배우는 것

- `LastEvaluatedKey`를 JSON으로 표현하기
- Base64 URL-safe cursor 인코딩
- cursor decode 실패 처리
- cursor의 `ownerId`와 path variable 검증

## 이번 주제에서 아직 다루지 않는 것

- GSI 설계
- transaction
- WebFlux pagination

## API 형태

```text
GET /owners/{ownerId}/tasks?size=10&cursor=
```

응답:

```json
{
  "items": [],
  "nextCursor": "...",
  "hasNext": true
}
```

## 운영 포인트

API cursor는 클라이언트가 임의로 수정할 수 있는 값이다. decode 실패, owner 불일치, 필수 key 누락은 모두 invalid cursor로 처리한다.

## 확인 질문

- DynamoDB key 구조를 API에 그대로 노출하지 않는 이유는 무엇인가?
- cursor에 `ownerId`를 포함하면 어떤 검증이 가능한가?
- cursor format을 바꾸고 싶다면 version field가 필요한가?

# LastEvaluatedKey

이 주제는 DynamoDB 원본 pagination 모델을 배우는 단계다.

프로젝트 위치:

```text
theory/09-last-evaluated-key/
```

## 이번 주제에서 배우는 것

- DynamoDB pagination은 offset이 아니라 key 기반이라는 점
- `Limit`
- `LastEvaluatedKey`
- `ExclusiveStartKey`

## 이번 주제에서 아직 다루지 않는 것

- Base64 API cursor
- cursor owner 검증
- WebFlux pagination

## 기본 흐름

```text
첫 요청:
Query(ownerId = owner-1, limit = 10)

응답:
items = 10개
LastEvaluatedKey = {...}

다음 요청:
Query(ownerId = owner-1, limit = 10, ExclusiveStartKey = LastEvaluatedKey)
```

## Spring 실행 흐름

이 주제에서는 학습을 위해 `LastEvaluatedKey` 구조를 응답에 드러낼 수 있다. 다음 주제에서 이 값을 API cursor로 감싼다.

## 운영 포인트

offset pagination처럼 임의 페이지 번호로 건너뛰는 UX는 DynamoDB와 잘 맞지 않는다. 목록 API는 "다음 페이지" 흐름으로 설계하는 편이 자연스럽다.

## 확인 질문

- DynamoDB에서 offset pagination을 기본으로 쓰지 않는 이유는 무엇인가?
- `LastEvaluatedKey`와 `ExclusiveStartKey`는 각각 언제 쓰이는가?
- 첫 페이지와 다음 페이지 요청은 어떤 점이 다른가?

# Transaction Basics

이 주제는 DynamoDB transaction의 기본만 배우는 단계다. 목표는 두 item 변경을 all-or-nothing으로 묶는 것이다.

실습 위치:

```text
theory/13-transaction-basics/
```

## 이번 주제에서 배우는 것

- `TransactWriteItems`
- `Put`과 `Update`를 하나로 묶기
- task item과 stats item 동시 변경
- partial update 방지

## 이번 주제에서 아직 다루지 않는 것

- cancellation reason
- idempotency token
- 복잡한 예외 매핑

## 필요한 흐름

`createTask`:

```text
1. TASK item Put
2. STATS item Update
```

하나라도 실패하면 전체 transaction이 실패하고 partial update가 남지 않아야 한다.

## 운영 포인트

transaction은 일반 write보다 비용과 conflict 가능성을 더 신중히 봐야 한다. 모든 write를 transaction으로 감싸지 말고 여러 item의 정합성이 필요한 흐름에만 사용한다.

## 확인 질문

- task 저장은 성공하고 stats 증가만 실패하면 어떤 문제가 생기는가?
- DynamoDB transaction은 JPA `@Transactional`과 어떤 점이 다른가?
- 모든 write를 transaction으로 감싸면 왜 좋지 않은가?

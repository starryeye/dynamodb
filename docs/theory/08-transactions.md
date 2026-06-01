# Transactions

DynamoDB transaction은 여러 item 변경을 all-or-nothing으로 묶기 위해 사용한다.

이 프로젝트에서는 `Task` item과 `OwnerTaskStats` item을 함께 변경해야 하므로 transaction이 필요하다.

## 필요한 흐름

`createTask`:

```text
1. TASK item Put
2. STATS item Update
```

`completeTask`:

```text
1. TASK item status 변경
2. STATS item todoCount 감소, doneCount 증가
```

`deleteTask`:

```text
1. TASK item Delete
2. STATS item counter 감소
```

## TransactWriteItems

`TransactWriteItems`는 `Put`, `Update`, `Delete`, `ConditionCheck`를 하나의 transaction으로 묶는다.

하나라도 실패하면 전체 transaction이 실패하고 partial update가 남지 않는다.

## Condition과 함께 쓰기

Transaction 안의 각 write에도 condition을 붙일 수 있다.

예:

```text
TASK update condition:
version = :expectedVersion AND status = :todo

STATS update:
ADD todoCount :minusOne, doneCount :plusOne
SET version = version + :one
```

## Idempotency Token

네트워크 오류 후 같은 transaction을 재시도할 수 있으므로 client request token을 사용해 idempotency를 고려한다.

학습 프로젝트에서는 request별 token을 생성하고 transaction write request에 명시한다.

## 예외 매핑

`TransactionCanceledException`은 여러 이유로 발생할 수 있다.

- condition 실패
- transaction conflict
- capacity 부족
- validation error

가능하면 cancellation reason을 보고 domain exception으로 매핑한다. 정확히 알 수 없으면 `TaskTransactionFailedException`으로 매핑한다.

## 확인 질문

- `createTask`에서 task 저장은 성공하고 stats 증가만 실패하면 어떤 문제가 생기는가?
- DynamoDB transaction은 JPA `@Transactional`과 어떤 점이 다른가?
- 모든 write를 transaction으로 감싸면 좋은가, 필요한 곳에만 써야 하는가?


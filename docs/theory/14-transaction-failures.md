# Transaction Failures

이 주제는 transaction 실패를 해석하는 단계다.

실습 위치:

```text
theory/14-transaction-failures/
```

## 이번 주제에서 배우는 것

- `TransactionCanceledException`
- cancellation reason 해석
- idempotency token
- domain exception 매핑

## 이번 주제에서 아직 다루지 않는 것

- transaction 기본 구성
- capacity mode 전체
- backup/monitoring

## 예외 매핑 예시

| 실패 상황 | Domain exception |
| --- | --- |
| duplicate task | `DuplicateTaskException` |
| stale version | `TaskVersionConflictException` |
| 이미 완료된 task | `TaskAlreadyCompletedException` |
| 정확히 구분 불가 | `TaskTransactionFailedException` |

## 운영 포인트

transaction 실패는 모두 같은 문제가 아니다. condition 실패, transaction conflict, throttling을 구분해서 관찰해야 retry 전략을 잘못 세우지 않는다.

## 확인 질문

- cancellation reason을 보지 않으면 어떤 예외 매핑 문제가 생기는가?
- idempotency token은 어떤 재시도 상황에서 필요한가?
- transaction conflict와 validation error를 같은 방식으로 retry해도 되는가?

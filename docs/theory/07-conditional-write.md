# Conditional Write

Conditional write는 조건이 참일 때만 write를 수행하는 DynamoDB 기능이다.

동시성 제어, 중복 생성 방지, 상태 전이 검증에 사용한다.

## 중복 생성 방지

같은 task를 두 번 생성하면 기존 item을 덮어쓰면 안 된다.

```text
attribute_not_exists(ownerId) AND attribute_not_exists(itemKey)
```

이 condition을 `PutItem` 또는 transaction의 `Put`에 붙이면 이미 같은 key의 item이 있을 때 write가 실패한다.

## Version Conflict 방지

수정과 삭제에는 `expectedVersion`을 사용한다.

```text
version = :expectedVersion
```

사용자가 오래된 version으로 수정하려 하면 condition이 실패하고 `TaskVersionConflictException`으로 매핑한다.

## 상태 전이 검증

완료 처리는 `TODO` 상태에서만 가능하다.

```text
status = :todo AND version = :expectedVersion
```

이미 `DONE`인 task를 다시 완료하려 하면 `TaskAlreadyCompletedException` 또는 적절한 domain exception으로 매핑한다.

## JPA Optimistic Locking과 비교

JPA에서는 `@Version`과 transaction commit 시점의 optimistic locking이 핵심이다.

DynamoDB에서는 write 요청 자체에 condition expression을 명시한다. 즉, 동시성 규칙이 database transaction annotation 뒤에 숨지 않고 write operation의 일부로 드러난다.

## 확인 질문

- 중복 생성을 막을 때 왜 단순 `PutItem`만으로는 부족한가?
- `expectedVersion`은 어디에서 받아야 하는가?
- condition 실패를 모두 같은 예외로 처리하면 어떤 학습 포인트를 놓치는가?


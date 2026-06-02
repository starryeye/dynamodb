# Conditional Put

이 주제는 조건부 생성만 배우는 단계다. 목표는 중복 task 생성을 안전하게 막는 것이다.

프로젝트 위치:

```text
theory/11-conditional-put/
```

## 이번 주제에서 배우는 것

- `PutItem`
- `conditionExpression`
- `attribute_not_exists`
- `ConditionalCheckFailedException`
- Spring service/test에서 duplicate conflict 확인하기

## 이번 주제에서 아직 다루지 않는 것

- versioned update
- state transition
- transaction

## 중복 생성 방지

같은 task를 두 번 생성하면 기존 item을 덮어쓰면 안 된다.

```text
attribute_not_exists(ownerId) AND attribute_not_exists(itemKey)
```

이 condition을 `PutItem`에 붙이면 이미 같은 key의 item이 있을 때 write가 실패한다.

## 운영 포인트

재시도 가능한 create API는 중복 생성 방지 조건을 가져야 한다. 단순 `PutItem`은 같은 key의 item을 덮어쓸 수 있으므로 학습용 코드에서도 피한다.

## 확인 질문

- 중복 생성을 막을 때 단순 `PutItem`만으로 부족한 이유는 무엇인가?
- condition 실패를 domain exception으로 바꾸는 이유는 무엇인가?
- duplicate create는 서버 장애인가, business conflict인가?

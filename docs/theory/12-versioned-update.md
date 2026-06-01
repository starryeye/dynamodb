# Versioned Update

이 주제는 `expectedVersion` 기반 update conflict를 배우는 단계다.

실습 위치:

```text
theory/12-versioned-update/
```

## 이번 주제에서 배우는 것

- item에 `version` attribute 두기
- `version = :expectedVersion` condition
- update 성공 시 version 증가
- Spring API에서 conflict 응답 반환하기

## 이번 주제에서 아직 다루지 않는 것

- state transition condition
- transaction
- cancellation reason

## 예시 요청

```json
{
  "title": "새 제목",
  "expectedVersion": 1
}
```

## 운영 포인트

동시 수정이 가능한 write API는 conflict를 정상적인 business response로 다뤄야 한다. 로그를 error로만 쌓기보다 conflict rate를 관찰할 수 있게 분리한다.

## 확인 질문

- `expectedVersion`을 request에 받는 이유는 무엇인가?
- update 후 version을 증가시키지 않으면 어떤 문제가 생기는가?
- conflict를 500으로 처리하면 왜 잘못인가?

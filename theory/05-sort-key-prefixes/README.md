# Sort Key Prefixes

이 주제는 sort key prefix를 배우는 단계다. 목표는 `TASK#...`, `STATS` 같은 prefix로 item type을 구분하는 방법을 익히는 것이다.

프로젝트 위치:

```text
theory/05-sort-key-prefixes/
```

## 이번 주제에서 배우는 것

- sort key naming convention
- prefix로 item type 구분하기
- `begins_with(itemKey, "TASK#")` query 조건
- Spring component로 key 생성 로직 분리하기

## 이번 주제에서 아직 다루지 않는 것

- full single-table design
- GSI
- transaction

## 예시 key

```text
TASK#task-1
TASK#task-2
STATS
```

## 운영 포인트

prefix 규칙은 한 번 production 데이터에 들어가면 바꾸기 어렵다. key naming convention은 README와 테스트로 고정한다.

## 확인 질문

- `TASK#` prefix가 없으면 task item만 골라내기 어려운 이유는 무엇인가?
- sort key prefix는 schema인가, application convention인가?
- key 생성 로직을 여러 service/test 흐름에 흩뿌리지 않는 이유는 무엇인가?

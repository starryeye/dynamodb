# GSI Basics

이 주제는 GSI를 처음 추가하는 단계다. 목표는 table primary key로는 어려운 조회를 별도 read path로 만드는 것이다.

프로젝트 위치:

```text
theory/07-gsi-basics/
```

## 이번 주제에서 배우는 것

- GSI의 역할
- GSI partition key와 sort key
- `OwnerCreatedAtIndex` 생성
- Spring service에서 table 조회와 index 조회를 분리하기

## 이번 주제에서 아직 다루지 않는 것

- eventual consistency
- GSI write cost
- pagination

## 이 프로젝트의 GSI

Task 목록은 생성일 역순으로 조회해야 한다.

기본 table key:

```text
PK = ownerId
SK = itemKey
```

GSI:

```text
OwnerCreatedAtIndex
PK = ownerId
SK = createdAtTaskId
```

예시:

```text
createdAtTaskId = 2026-06-01T10:15:30Z#task-1
```

## 운영 포인트

GSI는 새로운 read path다. API 하나를 추가하기 위해 GSI가 필요한지, 기존 key design으로 가능한지 먼저 access pattern 표에 기록한다.

## 확인 질문

- task 목록 조회는 왜 table primary key만으로 부족한가?
- GSI key도 access pattern에서 출발해야 하는 이유는 무엇인가?
- GSI 이름과 key schema를 README에 남겨야 하는 이유는 무엇인가?

# GSI와 Consistency

GSI는 Global Secondary Index의 약자다. 기본 primary key로 해결하기 어려운 조회를 위한 추가 query path다.

## 이 프로젝트의 GSI

Task 목록은 생성일 역순으로 조회해야 한다.

기본 table key는 다음과 같다.

```text
PK = ownerId
SK = itemKey
```

이 key만으로는 `createdAt` 기준 정렬 목록을 자연스럽게 만들기 어렵다. 그래서 별도 GSI를 둔다.

```text
GSI: OwnerCreatedAtIndex
PK: ownerId
SK: createdAtTaskId
```

예시:

```text
createdAtTaskId = 2026-06-01T10:15:30Z#task-1
```

또는 역순 정렬을 안정적으로 만들기 위해 inverted timestamp를 사용할 수 있다.

## GSI는 별도 조회 경로다

GSI는 table의 복사본처럼 생각하면 쉽지만, 전체 item을 그대로 복사하는 것은 아니다. projection 설정에 따라 필요한 attribute만 index에 포함할 수 있다.

이 프로젝트에서는 목록 응답에 필요한 attribute를 GSI query만으로 만들 수 있게 설계하는 편이 학습하기 좋다.

## Eventual Consistency

GSI query는 eventual consistency다.

즉, task 생성 요청이 성공한 직후 `GetItem`으로는 task가 보이지만, GSI를 사용하는 목록 조회에서는 아주 짧은 시간 동안 보이지 않을 수 있다.

이 차이는 DynamoDB를 배울 때 중요한 지점이다.

```text
getTask  -> table GetItem
listTask -> GSI Query
```

## 비용 관점

GSI는 read path를 추가해주지만 공짜가 아니다.

- table에 item을 쓰면 GSI에도 반영된다.
- GSI projection attribute가 많을수록 저장 공간과 write 비용이 늘 수 있다.
- provisioned mode에서는 GSI capacity도 별도로 고려해야 한다.

## 확인 질문

- task 목록 조회는 왜 table primary key가 아니라 GSI를 사용하는가?
- 생성 직후 단건 조회와 목록 조회 결과가 다를 수 있는 이유는 무엇인가?
- GSI를 추가하면 write 비용 관점에서 어떤 변화가 생기는가?


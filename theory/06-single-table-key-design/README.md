# Single-table Key Design

이 주제는 single-table design을 처음 적용하는 단계다. 목표는 서로 다른 item type을 같은 table에 두는 이유를 작게 확인하는 것이다.

프로젝트 위치:

```text
theory/06-single-table-key-design/
```

## 이번 주제에서 배우는 것

- 같은 table에 `TASK` item과 `STATS` item 함께 두기
- 같은 `ownerId` partition 안에 관련 item 배치하기
- 단순한 single-table item shape
- hot partition 위험을 처음 인식하기

## 이번 주제에서 아직 다루지 않는 것

- GSI
- transaction
- capacity mode

## 이 프로젝트의 Primary Key

```text
Table: tasks
PK: ownerId
SK: itemKey
```

task item:

```text
PK = owner-1
SK = TASK#task-1
```

stats item:

```text
PK = owner-1
SK = STATS
```

sort key prefix 자체는 이전 주제에서 이미 배웠다고 가정한다.

## Single-table Design을 쓰는 이유

이 프로젝트에서는 `Task`와 `OwnerTaskStats`를 같은 table에 저장한다.

이유:

- owner 기준으로 관련 item을 같은 partition에 둘 수 있다.
- task 변경과 stats 변경을 transaction 예제로 보여주기 좋다.
- RDB의 entity 중심 설계와 DynamoDB의 access pattern 중심 설계를 비교하기 좋다.

## 운영 포인트

`ownerId`가 partition key라는 것은 특정 owner에게 traffic이 몰릴 수 있다는 뜻이기도 하다. 이 예제에서는 학습을 위해 단순한 key를 사용한다. production에서 특정 owner가 매우 많은 task나 traffic을 가질 수 있다면 write sharding, tenant 분산, 다른 GSI 설계 같은 선택지를 검토한다.

## 확인 질문

- `Task`와 `OwnerTaskStats`를 같은 table에 두는 이유는 무엇인가?
- `ownerId`를 partition key로 쓰면 어떤 access pattern은 쉬워지고 어떤 위험이 생기는가?
- 이 설계가 모든 서비스에 무조건 적합하지 않은 이유는 무엇인가?

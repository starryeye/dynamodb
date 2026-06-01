# Key Design

DynamoDB key design은 access pattern을 실제 table 구조로 바꾸는 과정이다.

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

## Prefix를 쓰는 이유

`TASK#task-1`처럼 sort key에 prefix를 붙이면 같은 table 안에서 여러 item type을 구분하기 쉽다.

```text
TASK#task-1
TASK#task-2
STATS
```

나중에 다른 item type을 추가할 때도 sort key naming convention을 유지할 수 있다.

```text
COMMENT#comment-1
TAG#tag-1
```

## Single-table Design을 쓰는 이유

이 프로젝트에서는 `Task`와 `OwnerTaskStats`를 같은 table에 저장한다.

이유:

- owner 기준으로 관련 item을 같은 partition에 둘 수 있다.
- task 변경과 stats 변경을 transaction 예제로 보여주기 좋다.
- RDB의 entity 중심 설계와 DynamoDB의 access pattern 중심 설계를 비교하기 좋다.

## 주의할 점

`ownerId`가 partition key라는 것은 특정 owner에게 traffic이 몰릴 수 있다는 뜻이기도 하다.

이 예제에서는 학습을 위해 단순한 key를 사용한다. production에서 특정 owner가 매우 많은 task나 traffic을 가질 수 있다면 write sharding, tenant 분산, 다른 GSI 설계 같은 선택지를 검토해야 한다.

## 확인 질문

- `TASK#` prefix가 없다면 어떤 점이 불편해지는가?
- `ownerId`를 partition key로 쓰면 어떤 access pattern은 쉬워지고 어떤 위험이 생기는가?
- 이 프로젝트에서 stats item을 별도 table에 두지 않는 이유는 무엇인가?


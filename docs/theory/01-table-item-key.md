# Table, Item, Key

DynamoDB의 가장 기본 단위는 table, item, attribute, key다.

## 기본 개념

Table은 item의 모음이다. RDB table과 비슷해 보이지만, 모든 item이 같은 attribute를 가질 필요는 없다.

Item은 하나의 record다. JSON document처럼 여러 attribute를 가진다.

Attribute는 item의 필드다. 문자열, 숫자, boolean, list, map 같은 값을 가질 수 있다.

Primary key는 item을 찾기 위한 key다.

- partition key: item이 어느 partition에 속하는지 결정한다.
- sort key: 같은 partition key 안에서 item을 구분하고 정렬한다.

## Composite Primary Key

이 프로젝트의 DynamoDB table은 composite primary key를 사용한다.

```text
partition key = ownerId
sort key      = itemKey
```

예를 들어 owner가 가진 task item은 다음처럼 저장한다.

```json
{
  "ownerId": "owner-1",
  "itemKey": "TASK#task-1",
  "entityType": "TASK",
  "taskId": "task-1",
  "title": "DynamoDB 공부",
  "status": "TODO"
}
```

통계 item은 같은 owner partition 안에 다른 sort key로 저장한다.

```json
{
  "ownerId": "owner-1",
  "itemKey": "STATS",
  "entityType": "OWNER_TASK_STATS",
  "totalCount": 3,
  "todoCount": 2,
  "doneCount": 1
}
```

## Item Collection

같은 partition key를 가진 item 묶음을 item collection이라고 생각할 수 있다.

```text
ownerId = owner-1

itemKey = STATS
itemKey = TASK#task-1
itemKey = TASK#task-2
itemKey = TASK#task-3
```

이 구조 덕분에 owner 기준으로 관련 item을 함께 다룰 수 있다.

## 확인 질문

- `ownerId`만 알 때 어떤 item들을 찾을 수 있는가?
- `ownerId`와 `TASK#taskId`를 모두 알 때 어떤 operation이 적합한가?
- `STATS` item과 `TASK#...` item을 같은 table에 넣는 이유는 무엇인가?


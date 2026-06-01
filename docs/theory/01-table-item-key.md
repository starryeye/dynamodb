# Table, Item, Key

DynamoDB의 가장 기본 단위는 table, item, attribute, key다.

실습 위치:

```text
theory/01-table-item-key/
```

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

이번 주제에서는 full primary key로 task item 하나를 저장하고 조회하는 데 집중한다. 같은 partition key의 item 묶음은 다음 주제에서 `Query`로 다룬다.

## 확인 질문

- `ownerId`만 알 때 단건 `GetItem`을 할 수 없는 이유는 무엇인가?
- `ownerId`와 `TASK#taskId`를 모두 알 때 어떤 operation이 적합한가?
- Spring service에서 DynamoDB client를 직접 생성하지 않고 bean으로 주입받는 이유는 무엇인가?

## 실습에서 확인할 것

`theory/01-table-item-key` 프로젝트는 non-web Spring Boot 애플리케이션을 테스트로 로딩해 다음을 확인한다.

- DynamoDB Local에 `ownerId` + `itemKey` composite primary key를 가진 table을 만든다.
- `DynamoDbClient`를 Spring bean으로 등록한다.
- local profile에서 endpoint override와 dummy credential을 사용한다.
- `TASK#task-1` item을 저장한다.
- 테스트에서 `GetItem`으로 full primary key 조회를 한다.

`Query`와 item collection은 다음 주제인 [Item Collection Query](./02-item-collection-query.md)에서 다룬다.

운영 환경에서는 local endpoint override와 dummy credential을 사용하지 않는다. Production table은 애플리케이션 요청으로 만드는 것이 아니라 IaC로 관리하는 방향을 기본으로 둔다.

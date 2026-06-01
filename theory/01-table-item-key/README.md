# 01 Table, Item, Key

문서: [Table, Item, Key](../../docs/theory/01-table-item-key.md)

이 프로젝트는 non-web Spring Boot에서 DynamoDB table, item, partition key, sort key를 실험하는 독립 프로젝트다.

목표는 `DynamoDbClient`를 Spring bean으로 등록하고, 테스트를 통해 단순 item을 저장한 뒤 primary key로 다시 조회하는 것이다.

## 학습 목표

- DynamoDB table, item, attribute의 관계를 이해한다.
- Spring Boot에서 DynamoDB client를 설정하고 주입받는 방법을 이해한다.
- partition key와 sort key가 함께 primary key를 만든다는 점을 확인한다.
- full primary key로 `GetItem`을 호출하는 흐름을 확인한다.
- local profile과 prod profile에서 credential/endpoint 설정이 달라져야 하는 이유를 이해한다.

## Table Design

```text
table name: theory_01_table_item_key
partition key: ownerId
sort key: itemKey
```

예시 item:

```text
ownerId=owner-1, itemKey=TASK#task-1
```

## 테스트 방법

실제 DynamoDB 연동까지 확인하려면 Docker Desktop 또는 Docker daemon이 실행 중이어야 한다.

DynamoDB Local을 실행한다.

```bash
docker compose up -d
```

테스트를 실행한다. DynamoDB Local이 실행 중이면 table 생성, item 저장, `GetItem` 조회까지 같은 test 안에서 확인한다.

```bash
gradle test
```

같은 partition key의 item collection 조회는 다음 주제인 `02-item-collection-query`에서 다룬다.

다른 endpoint나 table name을 쓰고 싶으면 test property 또는 환경 변수로 바꾼다.

테스트 코드는 `src/test/kotlin`에서 확인한다.

## 관찰 포인트

- `GetItem`은 `ownerId`와 `itemKey`를 모두 알고 있을 때 사용한다.
- `ownerId`만으로는 full primary key가 아니므로 단건 `GetItem`을 할 수 없다.
- 같은 `ownerId`의 여러 item을 조회하는 흐름은 다음 주제에서 `Query`로 다룬다.
- local profile은 DynamoDB Local endpoint와 dummy credential을 사용한다.
- production에서는 endpoint override와 dummy credential을 제거하고 IAM role/default credential provider를 사용한다.

## 운영 관점

- 이 프로젝트는 학습 편의를 위해 test에서 table을 만든다.
- production에서는 application startup이나 test 흐름으로 table을 만들지 않는다.
- production table은 Terraform, CloudFormation, CDK 같은 IaC로 만든다.
- local profile의 `endpointOverride`는 DynamoDB Local 전용이다.
- prod profile에서는 AWS region, table name, IAM permission, monitoring을 명시적으로 관리한다.

## 완료 기준

- `TASK#task-1`가 `itemKey` sort key 값으로 저장되는 이유를 설명할 수 있다.
- `ownerId + itemKey`를 모두 알 때 `GetItem`이 적합한 이유를 설명할 수 있다.
- Spring Boot에서 `DynamoDbClient`를 bean으로 등록하고 service에서 주입받는 흐름을 설명할 수 있다.

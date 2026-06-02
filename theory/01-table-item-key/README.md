# 01 Table, Item, Key

이 프로젝트는 DynamoDB를 처음 배울 때 반드시 알아야 하는 table, item, attribute, key를 코드와 테스트로 확인하는 독립 프로젝트다.

이번 주제의 목표는 복잡한 애플리케이션을 만드는 것이 아니다. DynamoDB Local에 table을 만들고, task item 한 건을 저장한 뒤, primary key로 다시 조회하면서 DynamoDB가 데이터를 바라보는 가장 기본적인 방식을 익힌다.

## 이번 주제에서 배우는 것

- table은 item을 담는 공간이다.
- item은 DynamoDB에 저장되는 데이터 한 건이다.
- attribute는 item 안의 이름-값 한 쌍이다.
- primary key는 item을 찾기 위해 사용하는 attribute다.
- partition key와 sort key를 함께 쓰면 composite primary key가 된다.
- `PutItem`은 item 한 건을 저장하는 operation이다.
- `GetItem`은 full primary key를 알고 있을 때 item 한 건을 조회하는 operation이다.
- Spring Boot에서는 `DynamoDbClient`를 bean으로 등록하고 service에서 주입받아 사용한다.
- local 환경에서는 DynamoDB Local endpoint와 dummy credential을 사용하지만 production에서는 사용하지 않는다.

## 핵심 개념

### Table

Table은 item을 담는 공간이다. RDB의 table과 비슷하게 느껴질 수 있지만, DynamoDB table은 모든 item이 같은 attribute를 가져야 하는 구조가 아니다.

이 프로젝트에서는 다음 table을 사용한다.

```text
table name: theory_01_table_item_key
```

### Item

Item은 DynamoDB에 저장되는 데이터 한 건이다. RDB의 row 한 줄이나 JSON object 하나를 떠올리면 된다.

이번 예제에서는 "할 일 하나"를 item 한 건으로 저장한다.

```text
ownerId=owner-1
itemKey=TASK#task-1
entityType=TASK
taskId=task-1
title=DynamoDB table 이해하기
status=TODO
```

### Attribute

Attribute는 item 안에 들어 있는 이름-값 한 쌍이다. 위 예시에서 `title`이라는 attribute는 `DynamoDB table 이해하기`라는 값을 가진다.

DynamoDB attribute 값은 문자열, 숫자, boolean, list, map 같은 타입을 가질 수 있다. AWS SDK for Java/Kotlin에서는 이런 값을 `AttributeValue` 타입으로 표현한다.

이번 주제에서는 처음 배우는 단계이므로 문자열 attribute만 사용한다.

### Primary Key

Primary key는 item을 찾기 위해 사용하는 key다.

이 프로젝트는 partition key와 sort key를 함께 쓰는 composite primary key를 사용한다.

```text
partition key = ownerId
sort key      = itemKey
```

`ownerId`는 item이 어떤 owner에 속하는지 나타낸다. `itemKey`는 같은 owner 안에서 item 하나를 구분한다.

예를 들어 다음 key는 owner-1의 task-1 item을 뜻한다.

```text
ownerId=owner-1
itemKey=TASK#task-1
```

## 왜 ownerId와 itemKey가 둘 다 필요한가

`GetItem`은 item 한 건을 정확히 조회하는 operation이다. 이 table의 primary key는 `ownerId + itemKey`이므로 `GetItem`을 호출할 때도 두 값을 모두 알아야 한다.

`ownerId`만 알면 "owner-1이 가진 여러 item"을 가리킬 뿐이다. item 한 건을 정확히 지정하지 못한다. 같은 owner의 여러 item을 조회하는 방법은 다음 주제인 `02-item-collection-query`에서 `Query`로 다룬다.

## 코드 흐름

이 프로젝트는 controller나 runner 없이 test로 실행한다.

```text
TableItemKeyDynamoDbLocalTest
-> TableItemKeyService.saveDemoTask()
-> DynamoDB table 생성
-> demo task item 저장
-> TableItemKeyService.getItem(ownerId, itemKey)
-> full primary key로 item 조회
```

### 주요 파일

| 파일 | 역할 |
| --- | --- |
| `DynamoDbConfig.kt` | `DynamoDbClient`를 Spring bean으로 등록한다. |
| `DynamoDbProperties` | region, table name, endpoint, credential 설정을 담는다. |
| `TableItemKeyService.kt` | table 생성, item 저장, item 조회 흐름을 담는다. |
| `DynamoDbAttributeMapper.kt` | 문자열 값을 DynamoDB `AttributeValue`로 바꾸고 다시 읽기 쉬운 값으로 바꾼다. |
| `ItemKeys.kt` | `TASK#task-1` 같은 sort key 규칙을 만든다. |
| `TableItemKeyDynamoDbLocalTest.kt` | DynamoDB Local을 사용해 실제 저장/조회 흐름을 검증한다. |

## Spring Boot 연동 포인트

Spring Boot에서 DynamoDB를 사용할 때 service가 매번 `DynamoDbClient`를 직접 만들지 않는다. 대신 configuration에서 `DynamoDbClient`를 bean으로 등록하고, service는 생성자 주입으로 받아 사용한다.

이렇게 하면 local과 production 설정 차이를 코드 곳곳에 흩뿌리지 않을 수 있다. local에서는 `endpointOverride=http://localhost:8000`과 dummy credential을 쓰고, production에서는 AWS region과 IAM/default credential provider를 사용한다.

## 테스트 방법

실제 DynamoDB 연동까지 확인하려면 Docker Desktop 또는 Docker daemon이 실행 중이어야 한다.

DynamoDB Local을 실행한다.

```bash
docker compose up -d
```

테스트를 실행한다.

```bash
gradle test
```

DynamoDB Local이 실행 중이면 test가 table 생성, item 저장, `GetItem` 조회까지 확인한다. DynamoDB Local이 꺼져 있으면 실제 연동 테스트는 skip되고, Spring context와 key 규칙 테스트만 실행된다.

## 관찰 포인트

- `PutItem` 요청에는 저장할 table 이름과 item attribute들이 들어간다.
- `GetItem` 요청에는 table 이름과 full primary key가 들어간다.
- `ownerId`만으로는 이 table에서 item 한 건을 조회할 수 없다.
- `TASK#` prefix는 sort key 값에 item type을 함께 담기 위한 규칙이다.
- table 생성 시에는 key로 사용할 attribute만 schema에 선언한다.
- 일반 attribute인 `title`, `status`는 table 생성 시 미리 선언하지 않는다.

## 운영 관점

- 이 프로젝트는 학습 편의를 위해 test에서 table을 만든다.
- production에서는 application startup이나 test 흐름으로 table을 만들지 않는다.
- production table은 Terraform, CloudFormation, CDK 같은 IaC로 만든다.
- local profile의 `endpointOverride`는 DynamoDB Local 전용이다.
- production에서는 endpoint override와 dummy credential을 제거하고 IAM role/default credential provider를 사용한다.

## 완료 기준

- table, item, attribute, key의 차이를 설명할 수 있다.
- `ownerId + itemKey`가 이 table의 full primary key라는 점을 설명할 수 있다.
- `ownerId`만으로 `GetItem`을 할 수 없는 이유를 설명할 수 있다.
- `TASK#task-1`가 `itemKey` sort key 값으로 저장되는 이유를 설명할 수 있다.
- Spring Boot에서 `DynamoDbClient`를 bean으로 등록하고 service에서 주입받는 흐름을 설명할 수 있다.

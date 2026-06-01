# Item Collection Query

이 주제는 `Query`를 처음 배우는 단계다. 목표는 partition key가 같은 item 묶음을 Spring Boot API로 조회하는 것이다.

실습 위치:

```text
theory/02-item-collection-query/
```

## 이번 주제에서 배우는 것

- item collection의 의미
- partition key 조건으로 `Query` 호출하기
- `GetItem`과 `Query`의 차이
- Spring controller에서 owner별 item 목록 API 만들기

## 이번 주제에서 아직 다루지 않는 것

- `Scan`
- GSI
- pagination
- conditional write

## Spring API

```text
POST /demo/setup
GET /demo/owners/{ownerId}/items
```

`ownerId=owner-1`로 `Query`하면 같은 partition key를 가진 item만 반환된다.

## 운영 포인트

application request path에서 목록 조회를 만들 때는 먼저 `Query`로 표현 가능한지 확인한다. `Query`로 표현되지 않는 요구사항은 다음 주제에서 access pattern으로 다시 정리한다.

## 확인 질문

- `ownerId`만 알 때 `GetItem`이 아니라 `Query`가 필요한 이유는 무엇인가?
- `owner-1` query 결과에 `owner-2` item이 섞이지 않는 이유는 무엇인가?
- 같은 partition key에 item이 너무 많이 몰리면 어떤 위험이 생기는가?

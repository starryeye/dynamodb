# GSI Consistency

이 주제는 GSI의 consistency와 비용을 분리해서 배우는 단계다. 목표는 GSI가 별도 read path이며 eventual consistency라는 점을 이해하는 것이다.

실습 위치:

```text
theory/08-gsi-consistency/
```

## 이번 주제에서 배우는 것

- table `GetItem`과 GSI `Query`의 read path 차이
- GSI eventual consistency
- GSI projection과 write cost
- test 결과에서 consistency 차이를 관찰하는 방법

## 이번 주제에서 아직 다루지 않는 것

- pagination
- cursor
- capacity mode 전체 비교

## 핵심 관찰

```text
getTask  -> table GetItem
listTask -> GSI Query
```

task 생성 직후 단건 조회와 목록 조회 결과가 짧게 다를 수 있다.

## 운영 포인트

GSI는 read path를 추가해주지만 write path에도 영향을 준다. GSI에 projection되는 attribute와 index 수는 비용과 latency 관점에서 관리해야 한다.

## 확인 질문

- GSI query가 strongly consistent read를 지원하지 않는 이유는 무엇인가?
- 목록 API가 GSI를 쓸 때 사용자 경험에 어떤 설명이 필요한가?
- GSI projection을 무작정 넓히면 어떤 비용이 생기는가?

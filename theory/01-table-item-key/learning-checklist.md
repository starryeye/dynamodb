# Learning Checklist

## 읽기

- [ ] `docs/theory/01-table-item-key.md`를 읽었다.
- [ ] table, item, attribute를 구분할 수 있다.
- [ ] partition key와 sort key의 역할을 설명할 수 있다.
- [ ] Spring Boot에서 DynamoDB client를 bean으로 등록하는 흐름을 읽었다.
- [ ] local profile과 prod profile의 DynamoDB 설정 차이를 확인했다.

## 실행

- [ ] `docker compose up -d`로 DynamoDB Local을 실행했다.
- [ ] `gradle test`로 key naming 테스트를 실행했다.
- [ ] `gradle bootRun --args='--spring.profiles.active=local'`로 Spring Boot 애플리케이션을 실행했다.
- [ ] `POST /demo/setup`으로 table 생성과 seed item 저장을 실행했다.
- [ ] `GET /demo/items?ownerId=owner-1&itemKey=TASK%23task-1`로 full primary key 조회를 실행했다.

## 이해

- [ ] `GetItem`에 partition key와 sort key가 모두 필요한 이유를 설명할 수 있다.
- [ ] `TASK#` prefix가 item type을 구분하는 데 어떻게 쓰이는지 이해했다.
- [ ] item collection query는 다음 주제에서 다룬다는 점을 확인했다.
- [ ] production에서 request path가 table을 만들면 안 되는 이유를 이해했다.
- [ ] production에서 dummy credential과 endpoint override를 제거해야 하는 이유를 이해했다.

## 다음 주제로 넘어가기 전 질문

- [ ] 지금 만든 table은 어떤 access pattern을 만족하는가?
- [ ] `ownerId` 없이 `taskId`만으로 task를 찾으려면 이 key design으로 가능한가?
- [ ] 새로운 조회 요구사항이 생기면 왜 key design을 다시 봐야 하는가?

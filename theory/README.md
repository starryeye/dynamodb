# Theory Projects

이 디렉터리는 DynamoDB 이론 주제별 독립 Spring Boot 프로젝트를 담는다.

각 프로젝트는 `docs/theory/*.md` 문서와 1:1로 대응한다.

```text
docs/theory/01-table-item-key.md <-> theory/01-table-item-key/
docs/theory/10-api-cursor.md     <-> theory/10-api-cursor/
```

각 프로젝트는 형제 프로젝트에 의존하지 않는다. 기본 스택은 Spring MVC 기반 Servlet stack과 blocking `DynamoDbClient`다.

각 주제는 다음을 함께 배운다.

- DynamoDB 개념
- Spring Boot configuration과 bean 등록
- local profile과 DynamoDB Local 연동
- prod profile에서 endpoint override와 dummy credential을 제거하는 이유
- 해당 주제와 연결되는 운영 포인트

모든 theory 프로젝트는 Spring Boot MVC 기반으로 작성한다. topic마다 다루는 DynamoDB 기능은 다르지만, 애플리케이션 골격은 일관되게 유지한다.

## Topic Size Rule

한 topic은 다음 범위를 넘지 않는다.

- DynamoDB 핵심 개념 1개
- Spring 연동 포인트 1개
- 운영 주의점 1개

주제가 무거워지면 새 topic으로 분리한다. 예를 들어 GSI는 `07-gsi-basics`와 `08-gsi-consistency`로 나누고, pagination은 `09-last-evaluated-key`와 `10-api-cursor`로 나눈다.

## Comment Rule

코드 주석은 친절하지만 짧게 쓴다.

- DynamoDB key, condition, transaction, profile처럼 처음 보면 헷갈리는 지점에만 주석을 둔다.
- 구현을 그대로 반복하는 주석은 쓰지 않는다.
- 자세한 설명은 해당 `docs/theory/*.md` 문서로 안내한다.

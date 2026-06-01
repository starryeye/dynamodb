# 03 Access Patterns

문서: [Access Pattern](../../docs/theory/03-access-patterns.md)

이 프로젝트는 Spring Boot MVC API/use case를 DynamoDB operation과 key 조건으로 바꾸는 연습을 위한 독립 프로젝트다.

목표는 구현 전에 access pattern 표를 먼저 작성하는 습관을 익히는 것이다.

구현 시 `Controller -> Service -> DynamoDB adapter` 흐름을 최소 코드로 만들고, 각 endpoint가 `GetItem`, `Query`, `PutItem` 중 무엇으로 매핑되는지 확인한다.

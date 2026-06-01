# 07 GSI Basics

문서: [GSI Basics](../../docs/theory/07-gsi-basics.md)

이 프로젝트는 Spring Boot MVC API로 GSI를 별도 read path로 추가하는 독립 프로젝트다.

목표는 `OwnerCreatedAtIndex`를 만들고 table primary key로는 어려운 조회를 GSI `Query`로 해결하는 것이다.

eventual consistency와 GSI 비용은 다음 주제에서 분리해서 다룬다.

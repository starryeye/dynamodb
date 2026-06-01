# 08 GSI Consistency

문서: [GSI Consistency](../../docs/theory/08-gsi-consistency.md)

이 프로젝트는 Spring Boot MVC API로 GSI eventual consistency와 write cost를 학습하는 독립 프로젝트다.

목표는 table `GetItem`과 GSI `Query`가 서로 다른 read path라는 점을 확인하는 것이다.

# 04 Query vs Scan

문서: [Query vs Scan](../../docs/theory/04-query-vs-scan.md)

이 프로젝트는 Spring Boot MVC request path에서 `Query`와 `Scan`의 차이를 실험하는 독립 프로젝트다.

목표는 같은 데이터를 `Query`와 `Scan`으로 읽어보며 application path에서 `Scan`을 피해야 하는 이유를 확인하는 것이다.

구현 시 일반 API는 `Query`만 사용하고, `Scan`은 교육용 endpoint 또는 테스트로 분리한다.

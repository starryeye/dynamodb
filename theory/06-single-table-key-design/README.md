# 06 Single-table Key Design

문서: [Single-table Key Design](../../docs/theory/06-single-table-key-design.md)

이 프로젝트는 Spring Boot MVC 애플리케이션에서 single-table item shape와 key design을 설계하는 독립 프로젝트다.

목표는 `TASK#...`, `STATS` 같은 item을 같은 table에 둘 때의 장점과 hot partition 위험을 작게 확인하는 것이다.

구현 시 key 생성 로직을 작은 component로 분리하고, 운영 위험을 README에 기록한다.

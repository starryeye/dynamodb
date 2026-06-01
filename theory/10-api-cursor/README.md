# 10 API Cursor

문서: [API Cursor](../../docs/theory/10-api-cursor.md)

이 프로젝트는 Spring Boot MVC API에서 DynamoDB `LastEvaluatedKey`를 외부 cursor로 감싸는 독립 프로젝트다.

목표는 Base64 URL-safe cursor 인코딩, decode 실패, owner 검증을 구현하는 것이다.

# 06 Pagination

문서: [Pagination](../../docs/theory/06-pagination.md)

이 프로젝트는 `LastEvaluatedKey`, `ExclusiveStartKey`, API cursor를 실험하는 독립 프로젝트다.

목표는 DynamoDB cursor를 Base64 JSON으로 인코딩하고 다음 페이지 요청에 사용하는 흐름을 확인하는 것이다.

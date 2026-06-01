# Theory Projects

이 디렉터리는 DynamoDB 이론 주제별 독립 프로젝트를 담는다.

각 프로젝트는 `docs/theory/*.md` 문서와 1:1로 대응한다.

```text
docs/theory/01-table-item-key.md <-> theory/01-table-item-key/
docs/theory/06-pagination.md     <-> theory/06-pagination/
```

각 프로젝트는 형제 프로젝트에 의존하지 않는다. 코드가 필요한 주제는 자체 Gradle 설정, 테스트, 실행 스크립트를 가진다.

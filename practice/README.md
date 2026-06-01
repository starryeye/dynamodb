# Practice Projects

이 디렉터리는 Stage별 독립 애플리케이션 프로젝트를 담는다.

각 프로젝트는 `docs/practice/*.md` 문서와 1:1로 대응한다.

```text
docs/practice/stage1-mysql-mvc.md        <-> practice/stage1-mysql-mvc/
docs/practice/stage2-dynamodb-mvc.md     <-> practice/stage2-dynamodb-mvc/
docs/practice/stage3-dynamodb-webflux.md <-> practice/stage3-dynamodb-webflux/
```

각 Stage는 자체 Gradle 설정과 실행 환경을 가진다. 형제 Stage의 설정이나 코드에 의존하지 않는다.

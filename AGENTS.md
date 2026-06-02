# AGENTS.md

이 파일은 Codex가 이 저장소에서 작업할 때 읽는 프로젝트 지침이다.

OpenAI Codex는 repository root의 `AGENTS.md`를 project instruction으로 읽는다. 자세한 작업 규칙은 [.codex/project-guidelines.md](./.codex/project-guidelines.md)를 따른다.

## 핵심 원칙

- 학습자가 알아야 할 내용은 각 실제 프로젝트의 `README.md`에 둔다.
- Codex가 구현과 문서 정리 시 알아야 할 규칙은 `.codex/` 아래 문서에 둔다.
- `docs/` 폴더를 학습 본문 저장소로 다시 만들지 않는다.
- theory 코드는 DynamoDB 개념을 배우기 위한 최소 Spring Boot test 중심 예제로 유지한다.
- practice 코드는 Stage 1, 2, 3을 서로 비교하기 쉽게 독립 프로젝트로 유지한다.

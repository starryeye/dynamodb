# Access Pattern

DynamoDB 설계는 access pattern에서 시작한다.

Access pattern은 application이 데이터를 읽고 쓰는 방식이다. REST API, 화면, batch job, admin 기능을 보고 어떤 조건으로 데이터를 찾는지 먼저 정리한다.

## 이 프로젝트의 Access Pattern

| 기능 | Operation | Table 또는 Index | Key 조건 |
| --- | --- | --- | --- |
| task 생성 | `TransactWriteItems` | `tasks` | `ownerId`, `TASK#{taskId}` |
| task 단건 조회 | `GetItem` | `tasks` | `ownerId`, `TASK#{taskId}` |
| owner의 task 목록 조회 | `Query` | `OwnerCreatedAtIndex` | `ownerId` |
| task 제목 수정 | `UpdateItem` 또는 `TransactWriteItems` | `tasks` | `ownerId`, `TASK#{taskId}` |
| task 완료 처리 | `TransactWriteItems` | `tasks` | `TASK` item과 `STATS` item |
| task 삭제 | `TransactWriteItems` | `tasks` | `TASK` item과 `STATS` item |
| owner 통계 조회 | `GetItem` | `tasks` | `ownerId`, `STATS` |

## 왜 먼저 정리해야 하는가

DynamoDB에서는 SQL처럼 나중에 자유롭게 `WHERE` 조건을 늘리는 방식이 잘 맞지 않는다.

조회가 자주 발생하는 경로는 table primary key 또는 GSI key로 표현되어야 한다. 표현되지 않는 조회는 `Scan`으로 흘러가기 쉽고, 데이터가 커질수록 비용과 지연 시간이 커진다.

## 좋은 Access Pattern 문서의 조건

- API 또는 use case 이름이 있다.
- 읽기인지 쓰기인지 구분한다.
- `GetItem`, `Query`, `PutItem`, `UpdateItem`, `DeleteItem`, `TransactWriteItems` 중 어떤 operation인지 적는다.
- table primary key를 쓰는지 GSI를 쓰는지 적는다.
- 필요한 consistency와 pagination 방식을 적는다.

## 확인 질문

- `GET /owners/{ownerId}/tasks/{taskId}`는 왜 `Query`가 아니라 `GetItem`인가?
- `GET /owners/{ownerId}/tasks`는 왜 GSI를 사용하는가?
- 새로운 API를 추가할 때 문서에서 가장 먼저 추가해야 할 표는 무엇인가?


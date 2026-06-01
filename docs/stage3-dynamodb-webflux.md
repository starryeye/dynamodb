# Stage 3 - DynamoDB + Spring WebFlux

Stage 3는 Stage 2의 DynamoDB table design과 business behavior를 유지하면서 실행 모델을 reactive/non-blocking으로 바꾼다.

## 핵심 원칙

- Spring WebFlux를 사용한다.
- request handling에서 blocking `DynamoDbClient`를 사용하지 않는다.
- `DynamoDbAsyncClient`와 `DynamoDbEnhancedAsyncClient`를 사용한다.
- DynamoDB의 consistency model과 transaction strategy는 Stage 2와 동일하게 유지한다.

## Gradle 의존성

필수 의존성:

- `spring-boot-starter-webflux`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `software.amazon.awssdk:dynamodb`
- `software.amazon.awssdk:dynamodb-enhanced`
- `reactor-test`
- `assertj`
- `junit-jupiter`
- `mockito-kotlin` 또는 `mockk`

## Docker Compose

Stage 2와 같은 DynamoDB Local 구성을 사용한다.

- image: `amazon/dynamodb-local`
- port: `8000`
- shared DB mode
- 같은 table과 GSI design 유지

## API와 DTO

Stage 2와 같은 API behavior와 DTO 이름을 유지한다.

controller와 service의 반환 타입은 reactive type을 사용한다.

- `Mono<TaskResponse>`
- `Mono<TaskPageResponse>`
- `Mono<OwnerTaskStatsResponse>`
- `Mono<Void>`

## Reactive Architecture

필요한 구성 요소:

- `ReactiveTaskRepository`
- 필요하면 `ReactiveOwnerTaskStatsRepository`
- `ReactiveDynamoTaskRepository`
- `ReactiveTaskService`
- WebFlux `TaskController`

AWS SDK async 결과는 Reactor type으로 변환한다.

- 단건 `CompletableFuture` 결과: `Mono.fromFuture`
- async pagination publisher: `Flux.from` 또는 적절한 `Publisher` 변환

request handling에서 금지되는 패턴:

- `block()`
- `join()`
- `Future.get()`
- blocking DynamoDB call을 `Mono.just`로 감싸기

## Configuration

필요한 구성 요소:

- `DynamoDbAsyncConfig`
- `DynamoDbAsyncClient` bean
- `DynamoDbEnhancedAsyncClient` bean

local profile:

- DynamoDB Local endpoint override 사용

prod profile:

- endpoint override 사용 금지
- IAM role 또는 default credential provider 사용
- production에 맞는 timeout과 async HTTP client setting 포함

table initializer:

- local profile에서만 사용할 수 있다.
- async 방식 또는 request handling과 분리된 startup logic으로 구현한다.
- production에서는 startup에서 table을 생성하지 않는다.

## Transaction과 Concurrency

Stage 2와 같은 DynamoDB conditional write와 `TransactWriteItems` 전략을 유지한다.

요구사항:

- transaction operation은 `DynamoDbAsyncClient.transactWriteItems`를 사용한다.
- `CompletableFuture` 결과는 `Mono.fromFuture`로 변환한다.
- `ConditionalCheckFailedException`과 `TransactionCanceledException`을 reactive flow에서 domain exception으로 매핑한다.
- transaction result를 확인하기 위해 blocking하지 않는다.
- `version`과 `expectedVersion` 동작은 Stage 2와 동일하게 유지한다.

## 교육용 주석 포인트

코드에는 다음 내용을 짧고 명확하게 설명하는 주석을 추가한다.

- Stage 3는 execution model을 바꾸는 것이지 DynamoDB consistency model을 바꾸는 것이 아니다.
- Stage 2의 conditional write와 transaction 개념은 그대로 적용된다.
- WebFlux는 전체 request path가 non-blocking일 때 효과가 있다.
- blocking `DynamoDbClient` 대신 `DynamoDbAsyncClient`를 사용한다.
- AWS SDK async call은 `Mono.fromFuture`로 Reactor에 연결한다.
- request handling 내부에서 `block()` 또는 `join()`을 호출하지 않는다.
- legacy blocking code가 들어오면 `boundedElastic`로 격리할 수 있지만 이 프로젝트에서는 꼭 필요하지 않으면 사용하지 않는다.
- reactive test에는 `StepVerifier`를 사용한다.

## Production Notes

Stage 3 README에는 다음 내용을 포함한다.

- production에서는 local `endpointOverride`를 사용하지 않는다.
- production에서는 IAM role 또는 default credential provider를 사용한다.
- latency, throttling, error rate, consumed capacity를 모니터링한다.
- async HTTP client setting을 신중히 튜닝한다.
- reactive request path에 blocking code를 넣지 않는다.

## Stage 2와의 비교 포인트

| 주제 | Stage 2 DynamoDB MVC | Stage 3 DynamoDB WebFlux |
| --- | --- | --- |
| HTTP stack | Spring Web MVC | Spring WebFlux |
| DynamoDB client | `DynamoDbClient` 중심 | `DynamoDbAsyncClient` |
| 실행 모델 | blocking request thread | non-blocking reactive pipeline |
| 반환 타입 | plain object | `Mono` / `Flux` |
| AWS SDK 연결 | direct call | `Mono.fromFuture`, `Flux.from` |
| 테스트 | MVC controller test | `WebTestClient`, `StepVerifier` |
| DynamoDB 설계 | single-table, GSI, transaction | 동일하게 유지 |

Stage 3에서 바뀌지 않는 것:

- table key design
- GSI design
- condition expression
- `TransactWriteItems`
- `LastEvaluatedKey` cursor
- business rule과 domain exception

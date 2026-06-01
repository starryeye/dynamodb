# 13 Transaction Basics

문서: [Transaction Basics](../../docs/theory/13-transaction-basics.md)

이 프로젝트는 Spring Boot MVC service에서 `TransactWriteItems` 기본 흐름을 학습하는 독립 프로젝트다.

목표는 task item과 stats item을 함께 변경하고, 실패 시 partial update가 남지 않는지 확인하는 것이다.

transaction 실패 해석과 idempotency token은 다음 주제에서 분리해서 다룬다.

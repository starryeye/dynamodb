package com.example.dynamodb.theory.queryvsscan

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [QueryVsScanApplication::class],
    properties = [
        // 테스트용 DynamoDbClient는 실제 AWS 대신 DynamoDB Local endpoint를 사용한다.
        "app.dynamodb.endpoint=http://localhost:8000",
        // 실제 AWS credential 없이 Spring context를 로딩한다.
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class QueryVsScanApplicationTest {
    @Test
    fun `Spring context가 Query와 Scan용 DynamoDbClient 설정을 포함해 로딩된다`() {
    }
}

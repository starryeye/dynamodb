package com.example.dynamodb.theory.tableitemkey

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "app.dynamodb.endpoint=http://localhost:8000",
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class TableItemKeyApplicationTest {
    @Test
    fun `Spring context가 DynamoDbClient 설정을 포함해 로딩된다`() {
    }
}

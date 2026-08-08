package com.example.dynamodb.theory.accesspatterns

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [AccessPatternsApplication::class],
    properties = [
        // 테스트에서 DynamoDbClient bean이 실제 AWS 대신 local endpoint를 바라보게 한다.
        "app.dynamodb.endpoint=http://localhost:8000",
        // 실제 AWS credential 없이 Spring context를 로딩한다.
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class AccessPatternsApplicationTest {
    @Test
    fun `Spring context가 DynamoDbClient 설정을 포함해 로딩된다`() {
    }
}

package com.example.dynamodb.theory.itemcollectionquery

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        // Spring context 로딩 중 DynamoDbClient bean이 만들어질 때 사용할 local endpoint다.
        "app.dynamodb.endpoint=http://localhost:8000",
        // 실제 AWS credential 없이 context를 로딩하기 위해 dummy credential을 사용한다.
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class ItemCollectionQueryApplicationTest {
    @Test
    fun `Spring context가 DynamoDbClient Query 설정을 포함해 로딩된다`() {
    }
}

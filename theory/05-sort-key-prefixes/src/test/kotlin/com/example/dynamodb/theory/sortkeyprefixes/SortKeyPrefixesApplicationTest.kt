package com.example.dynamodb.theory.sortkeyprefixes

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@SpringBootTest(
    classes = [SortKeyPrefixesApplication::class],
    properties = [
        // 테스트용 client는 실제 AWS 대신 DynamoDB Local endpoint를 사용한다.
        "app.dynamodb.endpoint=http://localhost:8000",
        // 실제 AWS credential 없이 Spring context를 로딩한다.
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class SortKeyPrefixesApplicationTest {
    @Autowired
    private lateinit var context: ApplicationContext

    @Test
    fun `Spring context에 DynamoDbClient가 등록된다`() {
        assertThat(context.getBean(DynamoDbClient::class.java)).isNotNull
    }
}

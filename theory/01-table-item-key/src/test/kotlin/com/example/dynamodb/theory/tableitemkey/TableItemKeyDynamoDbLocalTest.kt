package com.example.dynamodb.theory.tableitemkey

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.net.InetSocketAddress
import java.net.Socket

/**
 * 이 테스트가 이번 주제의 실행 흐름이다.
 * DynamoDB Local에 table을 만들고, item을 저장한 뒤, full primary key로 다시 조회한다.
 */
@SpringBootTest(
    properties = [
        "app.dynamodb.endpoint=http://localhost:8000",
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class TableItemKeyDynamoDbLocalTest {
    @Autowired
    private lateinit var service: TableItemKeyService

    @BeforeEach
    fun requireDynamoDbLocal() {
        // DynamoDB Local이 떠 있을 때만 실제 연동 흐름을 실행한다.
        assumeTrue(canConnectToDynamoDbLocal())
    }

    @Test
    fun `full primary key로 저장한 item을 다시 조회한다`() {
        service.saveDemoTask()

        val item = service.getItem("owner-1", ItemKeys.task("task-1"))

        assertThat(item).containsEntry("ownerId", "owner-1")
        assertThat(item).containsEntry("itemKey", "TASK#task-1")
        assertThat(item).containsEntry("entityType", "TASK")
    }

    private fun canConnectToDynamoDbLocal(): Boolean =
        runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("localhost", 8000), 200)
            }
        }.isSuccess
}

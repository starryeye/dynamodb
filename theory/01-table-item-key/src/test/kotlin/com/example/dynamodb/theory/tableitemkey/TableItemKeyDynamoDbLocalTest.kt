package com.example.dynamodb.theory.tableitemkey

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.net.InetSocketAddress
import java.net.Socket

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
    fun `stores and reads item with full primary key`() {
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

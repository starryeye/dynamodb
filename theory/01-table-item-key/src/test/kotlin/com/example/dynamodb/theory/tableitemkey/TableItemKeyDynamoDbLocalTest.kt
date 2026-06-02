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
        // 테스트에서는 localhost의 DynamoDB Local로 요청을 보낸다.
        "app.dynamodb.endpoint=http://localhost:8000",
        // DynamoDB Local은 실제 AWS credential이 없어도 동작한다.
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
        // table을 만들고 demo task item 한 건을 저장한다.
        service.saveDemoTask()

        // ownerId와 itemKey를 모두 사용해서 방금 저장한 item을 조회한다.
        val item = service.getItem("owner-1", ItemKeys.task("task-1"))

        // 조회 결과의 ownerId가 저장한 값과 같은지 확인한다.
        assertThat(item).containsEntry("ownerId", "owner-1")
        // 조회 결과의 itemKey가 sort key 규칙과 같은지 확인한다.
        assertThat(item).containsEntry("itemKey", "TASK#task-1")
        // 조회 결과가 task item임을 확인한다.
        assertThat(item).containsEntry("entityType", "TASK")
    }

    private fun canConnectToDynamoDbLocal(): Boolean =
        runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("localhost", 8000), 200)
            }
        }.isSuccess
}

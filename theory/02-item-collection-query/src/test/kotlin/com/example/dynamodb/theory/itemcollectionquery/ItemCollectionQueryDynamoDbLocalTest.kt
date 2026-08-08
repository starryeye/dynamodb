package com.example.dynamodb.theory.itemcollectionquery

import java.net.InetSocketAddress
import java.net.Socket
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * 이 테스트가 이번 주제의 실행 흐름이다.
 * 같은 partition key를 가진 item 묶음을 Query로 조회한다.
 */
@SpringBootTest(
    properties = [
        // 테스트에서는 localhost의 DynamoDB Local로 요청을 보낸다.
        "app.dynamodb.endpoint=http://localhost:8000",
        // DynamoDB Local은 실제 AWS credential이 없어도 동작한다.
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class ItemCollectionQueryDynamoDbLocalTest {
    @Autowired
    private lateinit var service: ItemCollectionQueryService

    @BeforeEach
    fun requireDynamoDbLocal() {
        // DynamoDB Local이 떠 있을 때만 실제 Query 흐름을 실행한다.
        assumeTrue(canConnectToDynamoDbLocal())
    }

    @Test
    fun `partition key가 같은 task item collection만 조회한다`() {
        // owner-1 item 두 건과 owner-2 item 한 건을 같은 table에 저장한다.
        service.saveDemoTasks()

        // Query는 partition key 조건으로 owner-1의 item collection을 조회한다.
        val items = service.findTasksByOwner("owner-1")

        // owner-1 partition에 속한 task 두 건만 반환되는지 확인한다.
        assertThat(items).hasSize(2)
        // Query 결과에 다른 partition key(owner-2)의 item이 섞이지 않는지 확인한다.
        assertThat(items).allSatisfy { item ->
            assertThat(item).containsEntry("ownerId", "owner-1")
        }
        // 같은 owner 안에서 sort key 순서로 item을 구분할 수 있는지 확인한다.
        assertThat(items.map { it["itemKey"] }).containsExactly("TASK#task-1", "TASK#task-2")
    }

    private fun canConnectToDynamoDbLocal(): Boolean =
        runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("localhost", 8000), 200)
            }
        }.isSuccess
}

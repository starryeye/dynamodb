package com.example.dynamodb.theory.sortkeyprefixes

import java.net.InetSocketAddress
import java.net.Socket
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/** 실제 DynamoDB Local에서 sort key prefix가 task item만 선택하는지 확인한다. */
@SpringBootTest(
    classes = [SortKeyPrefixesApplication::class],
    properties = [
        "app.dynamodb.endpoint=http://localhost:8000",
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class SortKeyPrefixesDynamoDbLocalTest {
    @Autowired
    private lateinit var service: SortKeyPrefixService

    @BeforeEach
    fun requireDynamoDbLocal() {
        assumeTrue(canConnectToDynamoDbLocal())
    }

    @Test
    fun `TASK prefix Query는 같은 partition에서 task item만 반환한다`() {
        // 같은 owner에 TASK item 두 건과 TASK가 아닌 STATS item 한 건을 저장한다.
        service.saveDemoItems()

        // sort key가 TASK#로 시작하는 item만 key condition으로 조회한다.
        val taskItems = service.findTaskItems("owner-1")

        // STATS는 빠지고 TASK# item 두 건만 sort key 오름차순으로 반환되어야 한다.
        assertThat(taskItems.map { it.getValue("itemKey") })
            .containsExactly("TASK#task-1", "TASK#task-2")
        assertThat(taskItems.map { it.getValue("itemType") })
            .containsOnly("TASK")
    }

    private fun canConnectToDynamoDbLocal(): Boolean =
        runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("localhost", 8000), 200)
            }
        }.isSuccess
}

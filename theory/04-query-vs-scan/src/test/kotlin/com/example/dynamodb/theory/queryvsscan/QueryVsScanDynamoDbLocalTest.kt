package com.example.dynamodb.theory.queryvsscan

import java.net.InetSocketAddress
import java.net.Socket
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/** 같은 목록 요구사항을 Query와 Scan으로 실행해 평가한 item 수를 비교한다. */
@SpringBootTest(
    properties = [
        "app.dynamodb.endpoint=http://localhost:8000",
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class QueryVsScanDynamoDbLocalTest {
    @Autowired
    private lateinit var service: QueryVsScanService

    @BeforeEach
    fun requireDynamoDbLocal() {
        assumeTrue(canConnectToDynamoDbLocal())
    }

    @Test
    fun `Query와 Scan은 같은 task를 반환하지만 평가한 item 수가 다르다`() {
        // owner-1 두 건과 owner-2 세 건을 저장해 Query와 Scan의 읽기 범위를 비교한다.
        service.saveDemoTasks()

        // Query는 owner-1 partition만 읽고 Scan은 table 전체를 읽은 뒤 owner-1만 남긴다.
        val queryResult = service.queryTasksByOwner("owner-1")
        val scanResult = service.scanTasksByOwner("owner-1")

        // 조회 방식은 달라도 application에 반환되는 owner-1 task는 같아야 한다.
        assertThat(queryResult.items).containsExactlyInAnyOrderElementsOf(scanResult.items)
        assertThat(queryResult.count).isEqualTo(2)
        assertThat(scanResult.count).isEqualTo(2)

        // Query는 결과가 있는 partition만 평가하지만 Scan은 filter 전에 table의 모든 item을 평가한다.
        assertThat(queryResult.scannedCount).isEqualTo(2)
        assertThat(scanResult.scannedCount).isEqualTo(5)
        assertThat(scanResult.scannedCount).isGreaterThan(queryResult.scannedCount)
    }

    private fun canConnectToDynamoDbLocal(): Boolean =
        runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("localhost", 8000), 200)
            }
        }.isSuccess
}

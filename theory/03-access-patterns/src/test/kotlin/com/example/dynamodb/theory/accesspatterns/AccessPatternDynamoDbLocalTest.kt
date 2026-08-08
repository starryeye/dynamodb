package com.example.dynamodb.theory.accesspatterns

import java.net.InetSocketAddress
import java.net.Socket
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/** 애플리케이션 요구사항이 실제 DynamoDB operation으로 실행되는 학습 흐름이다. */
@SpringBootTest(
    properties = [
        "app.dynamodb.endpoint=http://localhost:8000",
        "app.dynamodb.use-dummy-credentials=true",
    ],
)
class AccessPatternDynamoDbLocalTest {
    @Autowired
    private lateinit var service: AccessPatternService

    @BeforeEach
    fun requireDynamoDbLocal() {
        assumeTrue(canConnectToDynamoDbLocal())
    }

    @Test
    fun `완전한 primary key로 task 한 건을 조회한다`() {
        // 'task를 저장한다'는 요구사항은 PutItem access pattern으로 실행된다.
        service.saveTask("owner-1", "task-1", "Access pattern 정리하기")

        // 'task 한 건을 찾는다'는 요구사항에는 partition key와 sort key가 모두 필요하다.
        val task = service.getTask("owner-1", "task-1")

        assertThat(task).containsEntry("ownerId", "owner-1")
        assertThat(task).containsEntry("itemKey", "TASK#task-1")
        assertThat(task).containsEntry("title", "Access pattern 정리하기")
    }

    @Test
    fun `partition key로 owner의 task 목록을 조회한다`() {
        service.saveTask("owner-1", "task-1", "첫 번째 task")
        service.saveTask("owner-1", "task-2", "두 번째 task")
        service.saveTask("owner-2", "task-1", "다른 owner의 task")

        // 'owner의 task 목록' 요구사항은 ownerId를 partition key 조건으로 사용하는 Query다.
        val tasks = service.findTasksByOwner("owner-1")

        assertThat(tasks).hasSize(2)
        assertThat(tasks).allSatisfy { task ->
            assertThat(task).containsEntry("ownerId", "owner-1")
        }
        assertThat(tasks.map { it["itemKey"] })
            .containsExactly("TASK#task-1", "TASK#task-2")
    }

    private fun canConnectToDynamoDbLocal(): Boolean =
        runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("localhost", 8000), 200)
            }
        }.isSuccess
}

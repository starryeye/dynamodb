package com.example.dynamodb.theory.sortkeyprefixes

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ItemKeyFactoryTest {
    private val itemKeyFactory = ItemKeyFactory()

    @Test
    fun `task key는 TASK prefix와 taskId를 조합한다`() {
        // prefix가 달라지면 기존 task item을 같은 Query로 찾을 수 없으므로 정확한 형식을 고정한다.
        assertThat(itemKeyFactory.task("task-1")).isEqualTo("TASK#task-1")
    }

    @Test
    fun `stats key는 고정된 STATS 값이다`() {
        // TASK#로 시작하지 않는 key를 사용해야 task prefix Query에서 제외된다.
        assertThat(itemKeyFactory.stats()).isEqualTo("STATS")
    }
}

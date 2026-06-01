package com.example.dynamodb.theory.tableitemkey

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ItemKeysTest {
    @Test
    fun `task item의 sort key는 TASK prefix를 사용한다`() {
        assertThat(ItemKeys.task("task-1")).isEqualTo("TASK#task-1")
    }
}

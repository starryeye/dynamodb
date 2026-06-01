package com.example.dynamodb.theory.tableitemkey

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ItemKeysTest {
    @Test
    fun `task key uses TASK prefix`() {
        assertThat(ItemKeys.task("task-1")).isEqualTo("TASK#task-1")
    }
}

package com.example.dynamodb.theory.tableitemkey

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ItemKeysTest {
    @Test // sort key 규칙이 실수로 바뀌지 않는지 확인한다.
    fun `task item의 sort key는 TASK prefix를 사용한다`() {
        // task-1이라는 application id가 DynamoDB sort key에서는 TASK#task-1이 된다.
        assertThat(ItemKeys.task("task-1")).isEqualTo("TASK#task-1")
    }
}

package com.example.dynamodb.theory.tableitemkey

object ItemKeys {
    private const val TASK_PREFIX = "TASK#"

    // sort key에 type과 id를 함께 담으면 나중에 관련 item을 묶어 조회하기 쉽다.
    fun task(taskId: String): String = "$TASK_PREFIX$taskId"
}

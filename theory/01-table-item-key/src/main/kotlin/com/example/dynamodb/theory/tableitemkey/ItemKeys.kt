package com.example.dynamodb.theory.tableitemkey

/**
 * sort key 값을 만드는 규칙을 모아둔다.
 * TASK#task-1은 "TASK 타입의 task-1 item"이라는 뜻이다.
 */
object ItemKeys {
    private const val TASK_PREFIX = "TASK#"

    // sort key에 type과 id를 함께 담으면 나중에 관련 item을 묶어 조회하기 쉽다.
    fun task(taskId: String): String = "$TASK_PREFIX$taskId"
}

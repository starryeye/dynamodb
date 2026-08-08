package com.example.dynamodb.theory.itemcollectionquery

/**
 * sort key 값을 만드는 규칙을 모아둔다.
 * 같은 ownerId partition 안에서 TASK#task-1, TASK#task-2처럼 item을 구분한다.
 */
object ItemKeys {
    // TASK# prefix를 붙이면 Query 결과에서 task item임을 구분하기 쉽다.
    private const val TASK_PREFIX = "TASK#"

    // task id를 DynamoDB sort key 값으로 바꾼다.
    fun task(taskId: String): String = "$TASK_PREFIX$taskId"
}

package com.example.dynamodb.theory.tableitemkey

/**
 * sort key 값을 만드는 규칙을 모아둔다.
 * TASK#task-1은 "TASK 타입의 task-1 item"이라는 뜻이다.
 */
object ItemKeys {
    // TASK# prefix를 붙이면 sort key만 봐도 task item임을 알 수 있다.
    private const val TASK_PREFIX = "TASK#"

    // task id를 DynamoDB sort key 값으로 바꾼다.
    fun task(taskId: String): String = "$TASK_PREFIX$taskId"
}

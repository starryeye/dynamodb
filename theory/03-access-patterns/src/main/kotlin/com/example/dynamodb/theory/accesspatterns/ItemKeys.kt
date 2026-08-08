package com.example.dynamodb.theory.accesspatterns

/** 같은 owner partition 안에서 task item을 구분할 sort key 규칙이다. */
object ItemKeys {
    private const val TASK_PREFIX = "TASK#"

    fun task(taskId: String): String = "$TASK_PREFIX$taskId"
}

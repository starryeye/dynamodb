package com.example.dynamodb.theory.tableitemkey

object ItemKeys {
    private const val TASK_PREFIX = "TASK#"

    // The sort key stores both type and id, which later lets us group related item types.
    fun task(taskId: String): String = "$TASK_PREFIX$taskId"
}

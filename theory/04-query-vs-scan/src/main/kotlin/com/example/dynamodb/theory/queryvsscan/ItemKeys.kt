package com.example.dynamodb.theory.queryvsscan

/** 같은 owner 안에서 task를 구분할 sort key 값을 만든다. */
object ItemKeys {
    fun task(taskId: String): String = "TASK#$taskId"
}

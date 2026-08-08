package com.example.dynamodb.theory.queryvsscan

/** 조회 결과와 DynamoDB가 반환한 item 수치를 함께 담아 Query와 Scan을 비교한다. */
data class ReadResult(
    val items: List<Map<String, String>>,
    val count: Int,
    val scannedCount: Int,
)

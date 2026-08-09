package com.example.dynamodb.theory.sortkeyprefixes

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

/** 예제의 문자열 attribute를 AWS SDK 타입과 Kotlin 타입 사이에서 변환한다. */
object DynamoDbAttributeMapper {
    fun s(value: String): AttributeValue =
        // s는 DynamoDB에서 문자열 값을 나타내는 String attribute다.
        AttributeValue.builder().s(value).build()

    fun toStringMap(item: Map<String, AttributeValue>): Map<String, String> =
        item.entries
            .sortedBy { it.key }
            .associate { (name, value) -> name to value.s() }
}

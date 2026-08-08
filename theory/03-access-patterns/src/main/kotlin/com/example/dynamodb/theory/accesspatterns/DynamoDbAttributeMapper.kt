package com.example.dynamodb.theory.accesspatterns

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

/** 이 입문 예제에서 사용하는 문자열 attribute를 AWS SDK 타입과 Kotlin 타입 사이에서 변환한다. */
object DynamoDbAttributeMapper {
    fun s(value: String): AttributeValue =
        // s는 DynamoDB의 String attribute 값이다.
        AttributeValue.builder().s(value).build()

    fun toStringMap(item: Map<String, AttributeValue>): Map<String, String> =
        item.entries
            .sortedBy { it.key }
            .associate { (name, value) -> name to value.s() }
}

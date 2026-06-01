package com.example.dynamodb.theory.tableitemkey

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

object DynamoDbAttributeMapper {
    fun s(value: String): AttributeValue =
        AttributeValue.builder().s(value).build()

    fun toStringMap(item: Map<String, AttributeValue>): Map<String, String> =
        item.entries
            .sortedBy { it.key }
            .associate { (name, value) -> name to value.s() }
}

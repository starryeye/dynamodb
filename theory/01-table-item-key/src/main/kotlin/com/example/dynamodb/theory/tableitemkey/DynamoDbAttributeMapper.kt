package com.example.dynamodb.theory.tableitemkey

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

object DynamoDbAttributeMapper {
    fun s(value: String): AttributeValue =
        AttributeValue.builder().s(value).build()

    fun n(value: Int): AttributeValue =
        AttributeValue.builder().n(value.toString()).build()

    fun toScalarMap(item: Map<String, AttributeValue>): Map<String, Any> =
        item.entries
            .sortedBy { it.key }
            .associate { (name, value) -> name to scalarValue(value) }

    private fun scalarValue(value: AttributeValue): Any =
        when {
            value.s() != null -> value.s()
            value.n() != null -> value.n().toLongOrNull() ?: value.n()
            value.bool() != null -> value.bool()
            else -> value.toString()
        }
}

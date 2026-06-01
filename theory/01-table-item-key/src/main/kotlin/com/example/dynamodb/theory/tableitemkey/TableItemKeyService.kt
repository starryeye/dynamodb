package com.example.dynamodb.theory.tableitemkey

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BillingMode
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType

@Service
class TableItemKeyService(
    private val client: DynamoDbClient,
    private val properties: DynamoDbProperties,
) {
    fun setup(): SetupResponse {
        ensureTable()
        val items = demoItems()

        items.forEach { item ->
            client.putItem(
                PutItemRequest.builder()
                    .tableName(properties.tableName)
                    .item(item)
                    .build(),
            )
        }

        return SetupResponse(
            tableName = properties.tableName,
            itemCount = items.size,
        )
    }

    fun getItem(ownerId: String, itemKey: String): Map<String, Any> {
        // GetItem needs the full primary key: partition key + sort key.
        val item = client.getItem(
            GetItemRequest.builder()
                .tableName(properties.tableName)
                .key(
                    mapOf(
                        "ownerId" to DynamoDbAttributeMapper.s(ownerId),
                        "itemKey" to DynamoDbAttributeMapper.s(itemKey),
                    ),
                )
                .build(),
        ).item()

        return DynamoDbAttributeMapper.toScalarMap(item)
    }

    private fun ensureTable() {
        if (tableExists()) {
            return
        }

        try {
            // This topic uses a composite primary key: ownerId + itemKey.
            client.createTable(
                CreateTableRequest.builder()
                    .tableName(properties.tableName)
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .attributeDefinitions(
                        AttributeDefinition.builder()
                            .attributeName("ownerId")
                            .attributeType(ScalarAttributeType.S)
                            .build(),
                        AttributeDefinition.builder()
                            .attributeName("itemKey")
                            .attributeType(ScalarAttributeType.S)
                            .build(),
                    )
                    .keySchema(
                        KeySchemaElement.builder()
                            .attributeName("ownerId")
                            .keyType(KeyType.HASH)
                            .build(),
                        KeySchemaElement.builder()
                            .attributeName("itemKey")
                            .keyType(KeyType.RANGE)
                            .build(),
                    )
                    .build(),
            )
            client.waiter().waitUntilTableExists { it.tableName(properties.tableName) }
        } catch (_: ResourceInUseException) {
            // Another local request may have created the demo table first.
        }
    }

    private fun tableExists(): Boolean =
        try {
            client.describeTable { it.tableName(properties.tableName) }
            true
        } catch (_: ResourceNotFoundException) {
            false
        }

    private fun demoItems(): List<Map<String, AttributeValue>> =
        listOf(
            taskItem("owner-1", "task-1", "DynamoDB table 이해하기", "TODO"),
        )

    private fun taskItem(
        ownerId: String,
        taskId: String,
        title: String,
        status: String,
    ): Map<String, AttributeValue> =
        mapOf(
            "ownerId" to DynamoDbAttributeMapper.s(ownerId),
            "itemKey" to DynamoDbAttributeMapper.s(ItemKeys.task(taskId)),
            "entityType" to DynamoDbAttributeMapper.s("TASK"),
            "taskId" to DynamoDbAttributeMapper.s(taskId),
            "title" to DynamoDbAttributeMapper.s(title),
            "status" to DynamoDbAttributeMapper.s(status),
        )

}

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
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType

@Service
class TableItemKeyService(
    private val client: DynamoDbClient,
    private val properties: DynamoDbProperties,
) {
    fun saveDemoTask() {
        createTableIfMissing()
        client.putItem(
            PutItemRequest.builder()
                .tableName(properties.tableName)
                .item(demoTaskItem())
                .build(),
        )
    }

    fun getItem(ownerId: String, itemKey: String): Map<String, String> {
        // GetItem은 partition key와 sort key가 모두 필요하다.
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

        return DynamoDbAttributeMapper.toStringMap(item)
    }

    private fun createTableIfMissing() {
        try {
            // 이번 주제는 ownerId + itemKey composite primary key를 사용한다.
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
            // 로컬 테스트가 이미 데모 table을 만든 경우는 넘어간다.
        }
    }

    private fun demoTaskItem(): Map<String, AttributeValue> =
        mapOf(
            "ownerId" to DynamoDbAttributeMapper.s("owner-1"),
            "itemKey" to DynamoDbAttributeMapper.s(ItemKeys.task("task-1")),
            "entityType" to DynamoDbAttributeMapper.s("TASK"),
            "taskId" to DynamoDbAttributeMapper.s("task-1"),
            "title" to DynamoDbAttributeMapper.s("DynamoDB table 이해하기"),
            "status" to DynamoDbAttributeMapper.s("TODO"),
        )
}

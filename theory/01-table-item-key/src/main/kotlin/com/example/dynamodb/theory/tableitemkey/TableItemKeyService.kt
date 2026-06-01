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

/**
 * 이번 주제는 DynamoDB의 가장 기본 단위인 table, item, key를 확인한다.
 *
 * table은 item을 담는 공간이고, item은 DynamoDB에 저장되는 데이터 한 건이다.
 * key는 item을 찾기 위한 값이다. 이 예제는 ownerId와 itemKey를 함께 써서
 * "할 일 item 한 건을 저장하고 다시 정확히 조회하는 흐름"만 다룬다.
 */
@Service
class TableItemKeyService(
    private val client: DynamoDbClient,
    private val properties: DynamoDbProperties,
) {
    /**
     * 학습용 table을 준비하고 demo task item 한 건을 저장한다.
     */
    fun saveDemoTask() {
        createTableIfMissing()
        client.putItem(
            PutItemRequest.builder()
                .tableName(properties.tableName)
                .item(demoTaskItem())
                .build(),
        )
    }

    /**
     * full primary key(ownerId + itemKey)를 알 때 item 한 건을 조회한다.
     */
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
            // ownerId는 partition key, itemKey는 sort key다.
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

    /**
     * DynamoDB item은 attribute 이름과 값으로 이루어진 map이다.
     * 여기서는 "할 일 하나"를 item 한 건으로 저장한다.
     */
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

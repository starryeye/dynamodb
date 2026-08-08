package com.example.dynamodb.theory.accesspatterns

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
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType

/**
 * 애플리케이션의 세 요구사항을 PutItem, GetItem, Query access pattern으로 연결한다.
 * 어떤 조건으로 읽을지 먼저 정하고 그 조건을 primary key로 표현하는 것이 이번 주제의 핵심이다.
 */
@Service
class AccessPatternService(
    private val client: DynamoDbClient,
    private val properties: DynamoDbProperties,
) {
    fun saveTask(ownerId: String, taskId: String, title: String) {
        createTableIfMissing()

        // task 저장 요구사항은 primary key를 포함한 item 한 건을 PutItem으로 기록한다.
        client.putItem(
            PutItemRequest.builder()
                .tableName(properties.tableName)
                .item(taskItem(ownerId, taskId, title))
                .build(),
        )
    }

    fun getTask(ownerId: String, taskId: String): Map<String, String>? {
        // task 단건 조회는 partition key와 sort key를 모두 아는 GetItem access pattern이다.
        val response = client.getItem(
            GetItemRequest.builder()
                .tableName(properties.tableName)
                .key(primaryKey(ownerId, taskId))
                .build(),
        )

        return if (response.hasItem()) {
            DynamoDbAttributeMapper.toStringMap(response.item())
        } else {
            null
        }
    }

    fun findTasksByOwner(ownerId: String): List<Map<String, String>> {
        // 목록 조회는 ownerId라는 partition key 조건으로 item collection을 읽는 Query다.
        val response = client.query(
            QueryRequest.builder()
                .tableName(properties.tableName)
                .keyConditionExpression("ownerId = :ownerId")
                .expressionAttributeValues(
                    mapOf(":ownerId" to DynamoDbAttributeMapper.s(ownerId)),
                )
                .build(),
        )

        return response.items().map(DynamoDbAttributeMapper::toStringMap)
    }

    private fun primaryKey(ownerId: String, taskId: String): Map<String, AttributeValue> =
        mapOf(
            // ownerId는 item이 속한 partition을 찾는 partition key다.
            "ownerId" to DynamoDbAttributeMapper.s(ownerId),
            // itemKey는 같은 owner 안에서 task 한 건을 찾는 sort key다.
            "itemKey" to DynamoDbAttributeMapper.s(ItemKeys.task(taskId)),
        )

    private fun taskItem(ownerId: String, taskId: String, title: String): Map<String, AttributeValue> =
        primaryKey(ownerId, taskId) + mapOf(
            "taskId" to DynamoDbAttributeMapper.s(taskId),
            "title" to DynamoDbAttributeMapper.s(title),
        )

    private fun createTableIfMissing() {
        try {
            client.createTable(
                CreateTableRequest.builder()
                    .tableName(properties.tableName)
                    // 입문 예제에서는 capacity 수치를 직접 정하지 않는 on-demand mode를 사용한다.
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
                            // HASH는 AWS SDK에서 partition key를 뜻한다.
                            .attributeName("ownerId")
                            .keyType(KeyType.HASH)
                            .build(),
                        KeySchemaElement.builder()
                            // RANGE는 AWS SDK에서 sort key를 뜻한다.
                            .attributeName("itemKey")
                            .keyType(KeyType.RANGE)
                            .build(),
                    )
                    .build(),
            )

            client.waiter().waitUntilTableExists { it.tableName(properties.tableName) }
        } catch (_: ResourceInUseException) {
            // 같은 학습 테스트를 반복 실행할 때 기존 table을 그대로 사용한다.
        }
    }
}

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
                // 어느 table에 task item을 저장할지 지정한다.
                .tableName(properties.tableName)
                // item에는 primary key와 task의 attribute가 함께 들어간다.
                .item(taskItem(ownerId, taskId, title))
                .build(),
        )
    }

    fun getTask(ownerId: String, taskId: String): Map<String, String>? {
        // task 단건 조회는 partition key와 sort key를 모두 아는 GetItem access pattern이다.
        val response = client.getItem(
            GetItemRequest.builder()
                .tableName(properties.tableName)
                // GetItem은 partition key와 sort key가 모두 들어간 완전한 primary key가 필요하다.
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
                // Query는 partition key가 ownerId와 같은 item collection을 읽는다.
                .keyConditionExpression("ownerId = :ownerId")
                // :ownerId 자리에 실제 조회할 ownerId 문자열 attribute를 넣는다.
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
            // application이 사용하는 id와 제목도 item의 일반 attribute로 저장한다.
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
                    // primary key에 참여하는 attribute의 이름과 DynamoDB 타입을 선언한다.
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
                    // ownerId + itemKey가 이 table의 primary key라는 구조를 선언한다.
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

            // table 생성 직후 PutItem을 안전하게 실행할 수 있도록 ACTIVE 상태까지 기다린다.
            client.waiter().waitUntilTableExists { it.tableName(properties.tableName) }
        } catch (_: ResourceInUseException) {
            // 같은 학습 테스트를 반복 실행할 때 기존 table을 그대로 사용한다.
        }
    }
}

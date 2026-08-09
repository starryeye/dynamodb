package com.example.dynamodb.theory.sortkeyprefixes

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BillingMode
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType

/** 같은 owner의 여러 item 중 TASK# sort key를 가진 item만 Query한다. */
@Service
class SortKeyPrefixService(
    private val client: DynamoDbClient,
    private val properties: DynamoDbProperties,
    private val itemKeyFactory: ItemKeyFactory,
) {
    fun saveDemoItems() {
        createTableIfMissing()

        // 같은 partition 안에 TASK# item과 다른 형식의 STATS item을 함께 저장한다.
        putItem(taskItem("owner-1", "task-1", "Sort key 읽기"))
        putItem(taskItem("owner-1", "task-2", "Prefix Query 실습하기"))
        putItem(statsItem("owner-1"))
    }

    fun findTaskItems(ownerId: String): List<Map<String, String>> {
        val response = client.query(
            QueryRequest.builder()
                .tableName(properties.tableName)
                // partition key가 같고 sort key가 TASK#로 시작하는 item만 읽는다.
                .keyConditionExpression(
                    "ownerId = :ownerId AND begins_with(itemKey, :prefix)",
                )
                // 표현식의 자리 이름에 실제 owner와 TASK# String attribute 값을 연결한다.
                .expressionAttributeValues(
                    mapOf(
                        ":ownerId" to DynamoDbAttributeMapper.s(ownerId),
                        ":prefix" to DynamoDbAttributeMapper.s(itemKeyFactory.taskPrefix()),
                    ),
                )
                .build(),
        )

        // Query 결과는 같은 partition 안에서 sort key 값의 오름차순으로 반환된다.
        return response.items().map(DynamoDbAttributeMapper::toStringMap)
    }

    private fun putItem(item: Map<String, AttributeValue>) {
        client.putItem(
            PutItemRequest.builder()
                .tableName(properties.tableName)
                // item은 DynamoDB table에 저장하는 한 건의 데이터다.
                .item(item)
                .build(),
        )
    }

    private fun taskItem(
        ownerId: String,
        taskId: String,
        title: String,
    ): Map<String, AttributeValue> =
        mapOf(
            // ownerId는 item이 속한 partition을 정하는 partition key다.
            "ownerId" to DynamoDbAttributeMapper.s(ownerId),
            // TASK# prefix가 있는 itemKey는 task를 구분하고 정렬하는 sort key다.
            "itemKey" to DynamoDbAttributeMapper.s(itemKeyFactory.task(taskId)),
            // itemType은 설명용 일반 attribute이며 task 조회 범위는 itemKey prefix로 정한다.
            "itemType" to DynamoDbAttributeMapper.s("TASK"),
            "title" to DynamoDbAttributeMapper.s(title),
        )

    private fun statsItem(ownerId: String): Map<String, AttributeValue> =
        mapOf(
            "ownerId" to DynamoDbAttributeMapper.s(ownerId),
            // STATS sort key는 TASK#로 시작하지 않아 task prefix Query에서 제외된다.
            "itemKey" to DynamoDbAttributeMapper.s(itemKeyFactory.stats()),
            // itemType 값이 아니라 STATS sort key 때문에 prefix Query에서 제외된다.
            "itemType" to DynamoDbAttributeMapper.s("STATS"),
        )

    private fun createTableIfMissing() {
        try {
            client.createTable(
                CreateTableRequest.builder()
                    .tableName(properties.tableName)
                    // 학습 예제에서는 capacity 수치를 직접 정하지 않는 on-demand mode를 사용한다.
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
                    // ownerId와 itemKey가 이 table의 composite primary key다.
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

            // table 생성 직후 item을 저장할 수 있도록 ACTIVE 상태까지 기다린다.
            client.waiter().waitUntilTableExists { it.tableName(properties.tableName) }
        } catch (_: ResourceInUseException) {
            // 같은 학습 테스트를 반복 실행할 때 기존 table을 그대로 사용한다.
        }
    }
}

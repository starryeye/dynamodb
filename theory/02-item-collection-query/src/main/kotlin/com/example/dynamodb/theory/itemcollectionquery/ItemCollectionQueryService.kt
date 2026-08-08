package com.example.dynamodb.theory.itemcollectionquery

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

/**
 * 이번 주제는 item collection을 Query로 조회한다.
 *
 * item collection은 partition key 값이 같은 item들의 묶음이다.
 * 이 예제에서는 ownerId가 partition key이므로 owner-1의 task item들이 하나의 item collection이다.
 *
 * GetItem은 primary key 전체(ownerId + itemKey)를 알아야 item 한 건을 읽는다.
 * Query는 partition key 조건(ownerId)으로 같은 owner의 item 여러 건을 읽는다.
 */
@Service
class ItemCollectionQueryService(
    private val client: DynamoDbClient,
    private val properties: DynamoDbProperties,
) {
    /**
     * 학습용 table을 준비하고, Query 결과를 확인할 demo task item 세 건을 저장한다.
     */
    fun saveDemoTasks() {
        // item collection을 조회하려면 먼저 같은 partition key를 가진 item들이 있어야 한다.
        createTableIfMissing()

        // owner-1 partition에 속한 첫 번째 task item이다.
        putItem(demoTaskItem(ownerId = "owner-1", taskId = "task-1", title = "Query 기본 이해하기"))
        // owner-1 partition에 속한 두 번째 task item이다.
        putItem(demoTaskItem(ownerId = "owner-1", taskId = "task-2", title = "Item collection 확인하기"))
        // owner-2 item은 다른 partition이라 owner-1 Query 결과에 나오면 안 된다.
        putItem(demoTaskItem(ownerId = "owner-2", taskId = "task-1", title = "다른 owner의 item"))
    }

    /**
     * partition key(ownerId)가 같은 task item collection을 조회한다.
     */
    fun findTasksByOwner(ownerId: String): List<Map<String, String>> {
        // Query는 partition key 조건을 만족하는 item 묶음을 읽는 operation이다.
        val response = client.query(
            QueryRequest.builder()
                // 어느 DynamoDB table에서 item collection을 읽을지 지정한다.
                .tableName(properties.tableName)
                // ownerId = :ownerId 조건은 같은 partition key 값만 읽겠다는 뜻이다.
                .keyConditionExpression("ownerId = :ownerId")
                // :ownerId는 key condition에서 사용할 실제 partition key 값이다.
                .expressionAttributeValues(
                    mapOf(":ownerId" to DynamoDbAttributeMapper.s(ownerId)),
                )
                .build(),
        )

        // Query 결과 item들을 테스트에서 읽기 쉬운 문자열 map 목록으로 바꾼다.
        return response.items().map(DynamoDbAttributeMapper::toStringMap)
    }

    private fun putItem(item: Map<String, AttributeValue>) {
        // PutItem은 Query로 조회할 demo item을 table에 저장한다.
        client.putItem(
            PutItemRequest.builder()
                // demo item을 저장할 DynamoDB table 이름이다.
                .tableName(properties.tableName)
                // item은 attribute 이름과 값의 묶음이다.
                .item(item)
                .build(),
        )
    }

    private fun createTableIfMissing() {
        try {
            // CreateTable은 DynamoDB table을 새로 만드는 operation이다.
            client.createTable(
                CreateTableRequest.builder()
                    // 생성할 DynamoDB table 이름이다.
                    .tableName(properties.tableName)
                    // 입문 예제에서는 capacity 계산 없이 쓰기 위해 on-demand 방식을 사용한다.
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    // Query에 사용할 key attribute 이름과 타입만 table 생성 시 선언한다.
                    .attributeDefinitions(
                        AttributeDefinition.builder()
                            // ownerId는 partition key라 Query 조건에 사용된다.
                            .attributeName("ownerId")
                            // ownerId 값은 문자열이다.
                            .attributeType(ScalarAttributeType.S)
                            .build(),
                        AttributeDefinition.builder()
                            // itemKey는 sort key라 같은 owner 안에서 item을 구분한다.
                            .attributeName("itemKey")
                            // itemKey 값도 문자열이다.
                            .attributeType(ScalarAttributeType.S)
                            .build(),
                    )
                    // table의 primary key 구조를 선언한다.
                    .keySchema(
                        KeySchemaElement.builder()
                            // ownerId를 partition key로 사용한다.
                            .attributeName("ownerId")
                            // HASH는 DynamoDB SDK에서 partition key를 뜻한다.
                            .keyType(KeyType.HASH)
                            .build(),
                        KeySchemaElement.builder()
                            // itemKey를 sort key로 사용한다.
                            .attributeName("itemKey")
                            // RANGE는 DynamoDB SDK에서 sort key를 뜻한다.
                            .keyType(KeyType.RANGE)
                            .build(),
                    )
                    .build(),
            )

            // table 생성은 바로 끝나지 않을 수 있으므로 사용할 수 있을 때까지 기다린다.
            client.waiter().waitUntilTableExists { it.tableName(properties.tableName) }
        } catch (_: ResourceInUseException) {
            // 이미 같은 이름의 table이 있으면 학습 흐름을 계속 진행한다.
        }
    }

    /**
     * DynamoDB item은 attribute 이름과 값으로 이루어진 map이다.
     * ownerId가 같은 item들은 같은 item collection에 속한다.
     */
    private fun demoTaskItem(ownerId: String, taskId: String, title: String): Map<String, AttributeValue> =
        mapOf(
            // partition key: 어떤 owner의 item collection에 속하는지 결정한다.
            "ownerId" to DynamoDbAttributeMapper.s(ownerId),
            // sort key: 같은 owner 안에서 task item 한 건을 구분한다.
            "itemKey" to DynamoDbAttributeMapper.s(ItemKeys.task(taskId)),
            // item의 종류를 코드에서 구분하기 위한 attribute다.
            "entityType" to DynamoDbAttributeMapper.s("TASK"),
            // application에서 사용하는 task id다.
            "taskId" to DynamoDbAttributeMapper.s(taskId),
            // 화면에 보여줄 task 제목이다.
            "title" to DynamoDbAttributeMapper.s(title),
            // task의 현재 상태다.
            "status" to DynamoDbAttributeMapper.s("TODO"),
        )
}

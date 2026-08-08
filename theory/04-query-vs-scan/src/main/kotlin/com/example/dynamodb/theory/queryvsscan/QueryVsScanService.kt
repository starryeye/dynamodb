package com.example.dynamodb.theory.queryvsscan

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
import software.amazon.awssdk.services.dynamodb.model.ScanRequest

/** 같은 조회 요구사항을 Query와 Scan으로 실행해 읽기 범위의 차이를 보여 준다. */
@Service
class QueryVsScanService(
    private val client: DynamoDbClient,
    private val properties: DynamoDbProperties,
) {
    fun saveDemoTasks() {
        createTableIfMissing()

        // 두 owner에 task를 나누어 저장해야 Query의 partition 범위와 Scan의 table 범위를 비교할 수 있다.
        putTask("owner-1", "task-1", "DynamoDB 기본 개념 읽기")
        putTask("owner-1", "task-2", "Query 실습하기")
        putTask("owner-2", "task-1", "Spring 설정 확인하기")
        putTask("owner-2", "task-2", "Scan 실습하기")
        putTask("owner-2", "task-3", "결과 비교하기")
    }

    fun queryTasksByOwner(ownerId: String): ReadResult {
        val response = client.query(
            QueryRequest.builder()
                .tableName(properties.tableName)
                // Query는 partition key 조건으로 owner-1이 있는 partition만 읽는다.
                .keyConditionExpression("ownerId = :ownerId")
                // 표현식의 :ownerId에 실제 DynamoDB String attribute 값을 연결한다.
                .expressionAttributeValues(ownerIdValue(ownerId))
                .build(),
        )

        return ReadResult(
            items = response.items().map(DynamoDbAttributeMapper::toStringMap),
            // Count는 조건을 통과해 결과로 반환된 item 수다.
            count = response.count(),
            // ScannedCount는 DynamoDB가 조건을 평가한 item 수다.
            scannedCount = response.scannedCount(),
        )
    }

    fun scanTasksByOwner(ownerId: String): ReadResult {
        val response = client.scan(
            ScanRequest.builder()
                .tableName(properties.tableName)
                // Scan은 table 전체를 읽은 다음 FilterExpression에 맞는 owner-1 item만 남긴다.
                .filterExpression("ownerId = :ownerId")
                .expressionAttributeValues(ownerIdValue(ownerId))
                .build(),
        )

        return ReadResult(
            items = response.items().map(DynamoDbAttributeMapper::toStringMap),
            // filter를 통과한 item만 Count에 포함된다.
            count = response.count(),
            // filter 전에 읽어 평가한 table의 모든 item이 ScannedCount에 포함된다.
            scannedCount = response.scannedCount(),
        )
    }

    private fun putTask(ownerId: String, taskId: String, title: String) {
        client.putItem(
            PutItemRequest.builder()
                .tableName(properties.tableName)
                // item은 DynamoDB table에 저장하는 한 건의 데이터이며 여러 attribute로 구성된다.
                .item(taskItem(ownerId, taskId, title))
                .build(),
        )
    }

    private fun taskItem(ownerId: String, taskId: String, title: String): Map<String, AttributeValue> =
        mapOf(
            // ownerId는 item이 저장될 partition을 정하는 partition key다.
            "ownerId" to DynamoDbAttributeMapper.s(ownerId),
            // itemKey는 같은 owner의 task들을 구분하고 정렬하는 sort key다.
            "itemKey" to DynamoDbAttributeMapper.s(ItemKeys.task(taskId)),
            // taskId와 title은 task가 가진 일반 attribute다.
            "taskId" to DynamoDbAttributeMapper.s(taskId),
            "title" to DynamoDbAttributeMapper.s(title),
        )

    private fun ownerIdValue(ownerId: String): Map<String, AttributeValue> =
        mapOf(":ownerId" to DynamoDbAttributeMapper.s(ownerId))

    private fun createTableIfMissing() {
        try {
            client.createTable(
                CreateTableRequest.builder()
                    .tableName(properties.tableName)
                    // 학습 예제에서는 capacity 수치를 직접 정하지 않는 on-demand mode를 사용한다.
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    // primary key에 참여하는 두 attribute의 이름과 DynamoDB 타입을 선언한다.
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
                    // ownerId와 itemKey가 이 table의 primary key라는 구조를 선언한다.
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

            // table 생성 직후 저장할 수 있도록 ACTIVE 상태가 될 때까지 기다린다.
            client.waiter().waitUntilTableExists { it.tableName(properties.tableName) }
        } catch (_: ResourceInUseException) {
            // 같은 테스트를 반복 실행할 때 이미 만들어진 table을 그대로 사용한다.
        }
    }
}

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
@Service // Spring이 이 클래스를 service bean으로 등록한다.
class TableItemKeyService(
    private val client: DynamoDbClient, // DynamoDB에 요청을 보내는 AWS SDK client다.
    private val properties: DynamoDbProperties, // table 이름, endpoint 같은 설정값을 담는다.
) {
    /**
     * 학습용 table을 준비하고 demo task item 한 건을 저장한다.
     */
    fun saveDemoTask() {
        // item을 저장하기 전에 table이 필요하므로 먼저 table 생성을 시도한다.
        createTableIfMissing()

        // PutItem은 DynamoDB table에 item 한 건을 저장하는 operation이다.
        client.putItem(
            PutItemRequest.builder()
                // 어떤 table에 저장할지 지정한다.
                .tableName(properties.tableName)
                // 저장할 item의 attribute들을 지정한다.
                .item(demoTaskItem())
                // builder에 넣은 값을 실제 요청 객체로 만든다.
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
                // 어느 table에서 읽을지 지정한다.
                .tableName(properties.tableName)
                // 조회할 item의 full primary key를 지정한다.
                .key(
                    mapOf(
                        // ownerId는 partition key 값이다.
                        "ownerId" to DynamoDbAttributeMapper.s(ownerId),
                        // itemKey는 sort key 값이다.
                        "itemKey" to DynamoDbAttributeMapper.s(itemKey),
                    ),
                )
                // builder에 넣은 값을 실제 요청 객체로 만든다.
                .build(),
        ).item() // DynamoDB 응답에서 item map만 꺼낸다.

        // AWS SDK의 AttributeValue map을 테스트에서 읽기 쉬운 문자열 map으로 바꾼다.
        return DynamoDbAttributeMapper.toStringMap(item)
    }

    private fun createTableIfMissing() {
        try {
            // CreateTable은 DynamoDB table을 새로 만드는 operation이다.
            client.createTable(
                CreateTableRequest.builder()
                    // 생성할 table 이름이다.
                    .tableName(properties.tableName)
                    // 입문 예제에서는 capacity 계산 없이 쓰기 위해 on-demand 방식을 사용한다.
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    // key로 사용할 attribute 이름과 타입을 선언한다.
                    .attributeDefinitions(
                        AttributeDefinition.builder()
                            // ownerId attribute를 key에 사용할 수 있게 선언한다.
                            .attributeName("ownerId")
                            // ownerId 값은 문자열이다.
                            .attributeType(ScalarAttributeType.S)
                            .build(),
                        AttributeDefinition.builder()
                            // itemKey attribute를 key에 사용할 수 있게 선언한다.
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
                    // builder에 넣은 값을 실제 요청 객체로 만든다.
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
     * 여기서는 "할 일 하나"를 item 한 건으로 저장한다.
     */
    private fun demoTaskItem(): Map<String, AttributeValue> =
        mapOf(
            // partition key: 어떤 owner의 item인지 나타낸다.
            "ownerId" to DynamoDbAttributeMapper.s("owner-1"),
            // sort key: 같은 owner 안에서 어떤 item인지 나타낸다.
            "itemKey" to DynamoDbAttributeMapper.s(ItemKeys.task("task-1")),
            // item의 종류를 코드에서 구분하기 위한 attribute다.
            "entityType" to DynamoDbAttributeMapper.s("TASK"),
            // application에서 사용하는 task id다.
            "taskId" to DynamoDbAttributeMapper.s("task-1"),
            // 화면에 보여줄 task 제목이다.
            "title" to DynamoDbAttributeMapper.s("DynamoDB table 이해하기"),
            // task의 현재 상태다.
            "status" to DynamoDbAttributeMapper.s("TODO"),
        )
}

package com.example.dynamodb.theory.itemcollectionquery

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

/**
 * DynamoDB attribute는 item 안의 필드 하나다.
 * Query 결과도 attribute 이름과 AttributeValue 값의 map으로 반환된다.
 *
 * 첫 Query 예제에서는 문자열 attribute만 사용해서 변환 코드를 작게 유지한다.
 */
object DynamoDbAttributeMapper {
    fun s(value: String): AttributeValue =
        // s는 DynamoDB 문자열 attribute 값을 뜻한다.
        AttributeValue.builder().s(value).build()

    fun toStringMap(item: Map<String, AttributeValue>): Map<String, String> =
        // 테스트 결과를 읽기 쉽도록 attribute 이름 순서로 정렬한다.
        item.entries
            .sortedBy { it.key }
            // AttributeValue에서 문자열 값만 꺼내 일반 Kotlin Map으로 바꾼다.
            .associate { (name, value) -> name to value.s() }
}

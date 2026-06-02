package com.example.dynamodb.theory.tableitemkey

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

/**
 * DynamoDB attribute는 item 안의 필드 하나다.
 * 예를 들어 title이라는 attribute는 "DynamoDB table 이해하기"라는 값을 가질 수 있다.
 *
 * AWS SDK는 attribute 값을 AttributeValue 타입으로 표현한다.
 * AttributeValue는 문자열, 숫자, boolean처럼 DynamoDB 값의 타입과 실제 값을 함께 담는다.
 * 첫 주제에서는 문자열 attribute만 사용해서 변환 코드를 작게 유지한다.
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

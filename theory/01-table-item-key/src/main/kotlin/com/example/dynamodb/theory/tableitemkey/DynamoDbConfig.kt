package com.example.dynamodb.theory.tableitemkey

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

/**
 * DynamoDB 접속에 필요한 설정값이다.
 * local에서는 DynamoDB Local endpoint를 쓰고, production에서는 AWS region과 credential 흐름을 쓴다.
 */
@ConfigurationProperties(prefix = "app.dynamodb")
data class DynamoDbProperties(
    val region: String = "ap-northeast-2", // DynamoDB를 호출할 AWS region이다.
    val tableName: String = "theory_01_table_item_key", // 예제에서 사용할 table 이름이다.
    val endpoint: String = "", // DynamoDB Local을 쓸 때만 localhost endpoint가 들어간다.
    val useDummyCredentials: Boolean = false, // DynamoDB Local에서는 dummy credential을 쓸 수 있다.
)

/**
 * Spring Boot에서 DynamoDbClient를 bean으로 등록하는 설정이다.
 * service는 이 bean을 주입받아 DynamoDB에 item을 저장하고 조회한다.
 */
@Configuration
class DynamoDbConfig {
    @Bean
    fun dynamoDbClient(properties: DynamoDbProperties): DynamoDbClient {
        val builder = DynamoDbClient.builder()
            // 어떤 AWS region의 DynamoDB를 호출할지 지정한다.
            .region(Region.of(properties.region))

        // endpoint가 비어 있지 않으면 DynamoDB Local 같은 별도 endpoint를 사용한다.
        if (properties.endpoint.isNotBlank()) {
            // endpointOverride는 DynamoDB Local 전용이며 production에서는 쓰지 않는다.
            builder.endpointOverride(URI.create(properties.endpoint))
        }

        // local profile에서는 AWS 계정 credential 대신 dummy credential을 사용한다.
        if (properties.useDummyCredentials) {
            builder.credentialsProvider(
                StaticCredentialsProvider.create(
                    // DynamoDB Local은 값 자체를 검증하지 않으므로 dummy 값을 넣는다.
                    AwsBasicCredentials.create("dummy", "dummy"),
                ),
            )
        } else {
            // production에서는 IAM role, 환경 변수 등 기본 credential 흐름을 사용한다.
            builder.credentialsProvider(DefaultCredentialsProvider.builder().build())
        }

        return builder.build()
    }
}

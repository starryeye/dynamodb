package com.example.dynamodb.theory.sortkeyprefixes

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

/** DynamoDB Local과 운영 AWS 환경에서 함께 사용하는 접속 설정이다. */
@ConfigurationProperties(prefix = "app.dynamodb")
data class DynamoDbProperties(
    val region: String = "ap-northeast-2",
    val tableName: String = "theory_05_sort_key_prefixes",
    val endpoint: String = "",
    val useDummyCredentials: Boolean = false,
)

/** sort key prefix 예제에서 사용할 blocking DynamoDbClient를 Spring bean으로 등록한다. */
@Configuration
class DynamoDbConfig {
    @Bean
    fun dynamoDbClient(properties: DynamoDbProperties): DynamoDbClient {
        val builder = DynamoDbClient.builder()
            // DynamoDB 요청을 보낼 AWS region을 지정한다.
            .region(Region.of(properties.region))

        if (properties.endpoint.isNotBlank()) {
            // local에서는 실제 AWS 대신 DynamoDB Local endpoint로 요청한다.
            builder.endpointOverride(URI.create(properties.endpoint))
        }

        if (properties.useDummyCredentials) {
            // DynamoDB Local은 credential을 검증하지 않으므로 dummy 값을 사용한다.
            builder.credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")),
            )
        } else {
            // 운영에서는 IAM role 등을 탐색하는 AWS 기본 credential 흐름을 사용한다.
            builder.credentialsProvider(DefaultCredentialsProvider.builder().build())
        }

        return builder.build()
    }
}

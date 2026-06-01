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

@ConfigurationProperties(prefix = "app.dynamodb")
data class DynamoDbProperties(
    val region: String = "ap-northeast-2",
    val tableName: String = "theory_01_table_item_key",
    val endpoint: String = "",
    val useDummyCredentials: Boolean = false,
)

/**
 * Spring Boot에서 DynamoDbClient를 bean으로 등록하는 설정이다.
 * service는 이 bean을 주입받아 DynamoDB에 item을 저장하고 조회한다.
 */
@Configuration
class DynamoDbConfig {
    @Bean
    fun dynamoDbClient(properties: DynamoDbProperties): DynamoDbClient {
        // Spring이 client를 관리하면 service는 DynamoDB operation에 집중할 수 있다.
        // 설정 흐름은 docs/theory/01-table-item-key.md에서 더 자세히 다룬다.
        val builder = DynamoDbClient.builder()
            .region(Region.of(properties.region))

        if (properties.endpoint.isNotBlank()) {
            // endpointOverride는 DynamoDB Local 전용이며 production에서는 쓰지 않는다.
            builder.endpointOverride(URI.create(properties.endpoint))
        }

        if (properties.useDummyCredentials) {
            // DynamoDB Local은 dummy credential을 허용하지만 AWS에서는 IAM/default credential을 쓴다.
            builder.credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("dummy", "dummy"),
                ),
            )
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.builder().build())
        }

        return builder.build()
    }
}

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

@Configuration
class DynamoDbConfig {
    @Bean
    fun dynamoDbClient(properties: DynamoDbProperties): DynamoDbClient {
        // Spring owns the client so services can focus on DynamoDB operations.
        // See docs/theory/01-table-item-key.md for the setup flow.
        val builder = DynamoDbClient.builder()
            .region(Region.of(properties.region))

        if (properties.endpoint.isNotBlank()) {
            // endpointOverride is for DynamoDB Local only, never production.
            builder.endpointOverride(URI.create(properties.endpoint))
        }

        if (properties.useDummyCredentials) {
            // DynamoDB Local accepts dummy credentials; AWS should use IAM/default credentials.
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

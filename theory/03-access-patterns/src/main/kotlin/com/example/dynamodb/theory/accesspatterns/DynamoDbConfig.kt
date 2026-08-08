package com.example.dynamodb.theory.accesspatterns

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
 * DynamoDB 접속 설정이다.
 * local에서는 endpoint와 dummy credential을 사용하고 production에서는 기본 AWS credential 흐름을 사용한다.
 */
@ConfigurationProperties(prefix = "app.dynamodb")
data class DynamoDbProperties(
    val region: String = "ap-northeast-2",
    val tableName: String = "theory_03_access_patterns",
    val endpoint: String = "",
    val useDummyCredentials: Boolean = false,
)

/** Spring이 DynamoDbClient를 관리하므로 service는 접속 설정을 직접 만들지 않는다. */
@Configuration
class DynamoDbConfig {
    @Bean
    fun dynamoDbClient(properties: DynamoDbProperties): DynamoDbClient {
        val builder = DynamoDbClient.builder()
            // 모든 DynamoDB 요청은 설정된 AWS region을 기준으로 서명된다.
            .region(Region.of(properties.region))

        if (properties.endpoint.isNotBlank()) {
            // local profile에서는 AWS endpoint 대신 DynamoDB Local로 요청한다.
            builder.endpointOverride(URI.create(properties.endpoint))
        }

        if (properties.useDummyCredentials) {
            builder.credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")),
            )
        } else {
            // production에서는 IAM role이나 환경 변수 등 AWS 기본 credential 흐름을 사용한다.
            builder.credentialsProvider(DefaultCredentialsProvider.builder().build())
        }

        return builder.build()
    }
}

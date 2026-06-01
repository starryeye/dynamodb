package com.example.dynamodb.theory.overview

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class OverviewController {
    // This first endpoint fixes the learning baseline for every theory project.
    // See docs/theory/00-overview.md for the full curriculum map.
    @GetMapping("/overview")
    fun overview(): OverviewResponse =
        OverviewResponse(
            defaultStack = "Spring MVC Servlet stack",
            dynamoDbClientPath = "Start with blocking DynamoDbClient, compare async client in Stage 3",
            modelingStart = "API/use case -> access pattern -> key design -> item shape",
            productionRule = "Use DynamoDB Local only in local profile; use IAM/default credentials and IaC in production",
            nextTopic = "01-table-item-key",
        )
}

data class OverviewResponse(
    val defaultStack: String,
    val dynamoDbClientPath: String,
    val modelingStart: String,
    val productionRule: String,
    val nextTopic: String,
)

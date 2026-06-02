package com.example.dynamodb.theory.tableitemkey

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class TableItemKeyApplication

fun main(args: Array<String>) {
    runApplication<TableItemKeyApplication>(*args)
}

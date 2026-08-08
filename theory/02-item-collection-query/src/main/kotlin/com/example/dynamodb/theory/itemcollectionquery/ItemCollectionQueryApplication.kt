package com.example.dynamodb.theory.itemcollectionquery

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ItemCollectionQueryApplication

fun main(args: Array<String>) {
    runApplication<ItemCollectionQueryApplication>(*args)
}

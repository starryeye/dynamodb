package com.example.dynamodb.theory.tableitemkey

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/demo")
class TableItemKeyController(
    private val service: TableItemKeyService,
) {
    @PostMapping("/setup")
    fun setup(): SetupResponse =
        service.setup()

    @GetMapping("/items")
    fun getItem(
        @RequestParam ownerId: String,
        @RequestParam itemKey: String,
    ): Map<String, Any> =
        service.getItem(ownerId, itemKey)
}

data class SetupResponse(
    val tableName: String,
    val itemCount: Int,
)

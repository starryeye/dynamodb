package com.example.dynamodb.theory.tableitemkey

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

// Spring Boot 애플리케이션의 시작점임을 표시한다.
@SpringBootApplication
// @ConfigurationProperties 클래스를 찾아 application.yml 값을 바인딩하게 한다.
@ConfigurationPropertiesScan
class TableItemKeyApplication

fun main(args: Array<String>) {
    // Spring Boot 애플리케이션을 실행한다. theory에서는 보통 test가 이 context를 로딩한다.
    runApplication<TableItemKeyApplication>(*args)
}

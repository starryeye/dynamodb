package com.example.dynamodb.theory.sortkeyprefixes

import org.springframework.stereotype.Component

/** item type을 구분하는 sort key 문자열을 한 곳에서 만든다. */
@Component
class ItemKeyFactory {
    fun task(taskId: String): String =
        // TASK# prefix 뒤에 taskId를 붙이는 규칙은 application이 정한 convention이다.
        "${taskPrefix()}$taskId"

    fun taskPrefix(): String =
        // 저장과 Query가 같은 prefix를 사용하도록 TASK# 값을 한 곳에서 제공한다.
        "TASK#"

    fun stats(): String =
        // STATS는 TASK#로 시작하지 않으므로 task prefix Query 결과에서 제외된다.
        "STATS"
}

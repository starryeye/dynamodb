package com.example.dynamodb.theory.overview

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class OverviewApplicationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `overview endpoint describes learning direction`() {
        val response = mockMvc.get("/overview")
            .andExpect {
                status { isOk() }
            }
            .andReturn()
            .response
            .contentAsString

        assertThat(response).contains("Spring MVC Servlet stack")
        assertThat(response).contains("01-table-item-key")
    }
}

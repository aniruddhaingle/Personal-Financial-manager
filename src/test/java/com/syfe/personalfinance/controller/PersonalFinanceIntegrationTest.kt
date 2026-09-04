package com.syfe.personalfinance.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.syfe.personalfinance.dto.AuthDto
import com.syfe.personalfinance.dto.TransactionDto
import com.syfe.personalfinance.entity.Category
import com.syfe.personalfinance.enums.CategoryType
import com.syfe.personalfinance.repository.CategoryRepository
import com.syfe.personalfinance.repository.TransactionRepository
import jakarta.servlet.http.HttpSession
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersonalFinanceIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var seededFoodCategory: Category

    @BeforeEach
    fun setUp() {
        transactionRepository.deleteAll()
        seededFoodCategory = categoryRepository.findAllAvailableToUser(9999L)
            .firstOrNull { "Food".equals(it.name, ignoreCase = true) }
            ?: categoryRepository.save(
                Category(
                    name = "Food",
                    type = CategoryType.EXPENSE,
                    isDefault = true
                )
            )
    }

    @Test
    fun testAuthenticationAndUserResourceIsolation() {
        // 1. Unauthenticated Request: Try accessing transactions -> Assert 401 JSON
        mockMvc.perform(get("/api/transactions"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error", `is`("Unauthorized")))
            .andExpect(jsonPath("$.message", containsString("Unauthorized")))

        // 2. Register User A
        val userARegister = AuthDto.RegisterRequest(
            username = "usera@syfe.com",
            password = "SecurePassword123!",
            fullName = "User A"
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userARegister))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.message", `is`("User registered successfully")))
            .andExpect(jsonPath("$.userId", notNullValue()))

        // 3. Login User A -> Extract Session Cookie
        val userALogin = AuthDto.LoginRequest(
            username = "usera@syfe.com",
            password = "SecurePassword123!"
        )

        val loginAResult: MvcResult = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userALogin))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message", `is`("Login successful")))
            .andReturn()

        val sessionA: HttpSession? = loginAResult.request.getSession(false)
        assertNotNull(sessionA)
        val mockSessionA = sessionA as MockHttpSession

        // 4. Register User B
        val userBRegister = AuthDto.RegisterRequest(
            username = "userb@syfe.com",
            password = "SecurePassword456!",
            fullName = "User B"
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userBRegister))
        )
            .andExpect(status().isCreated)

        // 5. Login User B -> Extract Session Cookie
        val userBLogin = AuthDto.LoginRequest(
            username = "userb@syfe.com",
            password = "SecurePassword456!"
        )

        val loginBResult: MvcResult = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userBLogin))
        )
            .andExpect(status().isOk)
            .andReturn()

        val sessionB: HttpSession? = loginBResult.request.getSession(false)
        assertNotNull(sessionB)
        val mockSessionB = sessionB as MockHttpSession

        // 6. User A Creates Transaction
        val transactionRequest = TransactionDto.CreateTransactionRequest(
            amount = BigDecimal("99.90"),
            date = LocalDate.now(),
            category = "Food",
            description = "User A Dinner"
        )

        val transResult = mockMvc.perform(
            post("/api/transactions")
                .session(mockSessionA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.description", `is`("User A Dinner")))
            .andReturn()

        val responseContent = transResult.response.contentAsString
        val transactionId = objectMapper.readTree(responseContent).path("id").asLong()

        // 7. Verify Data Isolation: User B lists transactions -> Should be empty
        mockMvc.perform(
            get("/api/transactions")
                .session(mockSessionB)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.transactions", hasSize<Any>(0)))

        // 8. Verify Data Isolation: User B attempts to delete User A's transaction -> Should fail with 404
        mockMvc.perform(
            delete("/api/transactions/$transactionId")
                .session(mockSessionB)
        )
            .andExpect(status().isNotFound)

        // 9. User A lists transactions -> Should return 1 element
        mockMvc.perform(
            get("/api/transactions")
                .session(mockSessionA)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.transactions", hasSize<Any>(1)))
            .andExpect(jsonPath("$.transactions[0].description", `is`("User A Dinner")))

        // 10. Logout User A
        mockMvc.perform(
            post("/api/auth/logout")
                .session(mockSessionA)
        )
            .andExpect(status().isOk)

        // 11. Assert Session invalidated for User A
        mockMvc.perform(
            get("/api/transactions")
                .session(mockSessionA)
        )
            .andExpect(status().isUnauthorized)
    }
}

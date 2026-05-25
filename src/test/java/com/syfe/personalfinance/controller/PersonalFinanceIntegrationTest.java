package com.syfe.personalfinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syfe.personalfinance.dto.AuthDto;
import com.syfe.personalfinance.dto.TransactionDto;
import com.syfe.personalfinance.entity.Category;
import com.syfe.personalfinance.enums.CategoryType;
import com.syfe.personalfinance.repository.CategoryRepository;
import com.syfe.personalfinance.repository.TransactionRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Run against the isolated H2 test profile
class PersonalFinanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Category seededFoodCategory;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        // Resolve seeded default categories or add if missing in clean slate
        seededFoodCategory = categoryRepository.findAllAvailableToUser(9999L)
                .stream()
                .filter(c -> "Food".equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name("Food")
                        .type(CategoryType.EXPENSE)
                        .isDefault(true)
                        .build()));
    }

    @Test
    void testAuthenticationAndUserResourceIsolation() throws Exception {
        // 1. Unauthenticated Request: Try accessing transactions -> Assert 401 JSON
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Unauthorized")));

        // 2. Register User A
        AuthDto.RegisterRequest userARegister = AuthDto.RegisterRequest.builder()
                .username("usera@syfe.com")
                .password("SecurePassword123!")
                .fullName("User A")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userARegister)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", is("usera@syfe.com")));

        // 3. Login User A -> Extract Session Cookie
        AuthDto.LoginRequest userALogin = AuthDto.LoginRequest.builder()
                .username("usera@syfe.com")
                .password("SecurePassword123!")
                .build();

        MvcResult loginAResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userALogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andReturn();

        HttpSession sessionA = loginAResult.getRequest().getSession(false);
        assertNotNull(sessionA);
        MockHttpSession mockSessionA = (MockHttpSession) sessionA;

        // 4. Register User B
        AuthDto.RegisterRequest userBRegister = AuthDto.RegisterRequest.builder()
                .username("userb@syfe.com")
                .password("SecurePassword456!")
                .fullName("User B")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBRegister)))
                .andExpect(status().isCreated());

        // 5. Login User B -> Extract Session Cookie
        AuthDto.LoginRequest userBLogin = AuthDto.LoginRequest.builder()
                .username("userb@syfe.com")
                .password("SecurePassword456!")
                .build();

        MvcResult loginBResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBLogin)))
                .andExpect(status().isOk())
                .andReturn();

        HttpSession sessionB = loginBResult.getRequest().getSession(false);
        assertNotNull(sessionB);
        MockHttpSession mockSessionB = (MockHttpSession) sessionB;

        // 6. User A Creates Transaction
        TransactionDto.CreateTransactionRequest transactionRequest = TransactionDto.CreateTransactionRequest.builder()
                .amount(new BigDecimal("99.90"))
                .date(LocalDate.now())
                .categoryId(seededFoodCategory.getId())
                .description("User A Dinner")
                .build();

        MvcResult transResult = mockMvc.perform(post("/api/transactions")
                        .session(mockSessionA) // Using User A Session
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.description", is("User A Dinner")))
                .andReturn();

        // Retrieve created transaction ID
        String responseContent = transResult.getResponse().getContentAsString();
        Long transactionId = objectMapper.readTree(responseContent).path("data").path("id").asLong();

        // 7. Verify Data Isolation: User B lists transactions -> Should be empty page (0 elements)
        mockMvc.perform(get("/api/transactions")
                        .session(mockSessionB)) // Using User B Session
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));

        // 8. Verify Data Isolation: User B attempts to delete User A's transaction -> Should fail with 404/403
        mockMvc.perform(delete("/api/transactions/" + transactionId)
                        .session(mockSessionB)) // Using User B Session
                .andExpect(status().isNotFound()) // Returns 404 because isolated query findByIdAndUserId returns Empty
                .andExpect(jsonPath("$.success", is(false)));

        // 9. User A lists transactions -> Should return 1 element
        mockMvc.perform(get("/api/transactions")
                        .session(mockSessionA)) // Using User A Session
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].description", is("User A Dinner")));

        // 10. Logout User A
        mockMvc.perform(post("/api/auth/logout")
                        .session(mockSessionA))
                .andExpect(status().isOk());

        // 11. Assert Session invalidated for User A
        mockMvc.perform(get("/api/transactions")
                        .session(mockSessionA))
                .andExpect(status().isUnauthorized());
    }
}

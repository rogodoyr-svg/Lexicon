package com.lexicon.prestamo.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexicon.prestamo.dto.LoginRequest;
import com.lexicon.prestamo.dto.RegisterRequest;
import com.lexicon.prestamo.test.TestDataConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestDataConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Integration Tests - Full Auth Flow")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should login with default user claudio/1234")
    void testLoginWithDefaultUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest("claudio", "1234");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", equalTo("claudio")))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.expiresIn", is(3600000)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).path("data").path("token").asText();
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("Should fail login with wrong password for default user")
    void testLoginWithWrongPasswordForDefaultUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest("claudio", "wrong");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("AUTHENTICATION_ERROR")));
    }

    @Test
    @DisplayName("Complete flow: Register new user -> Login -> Validate")
    void testCompleteAuthenticationFlow() throws Exception {
        String newUsername = "newuser_" + System.currentTimeMillis();
        RegisterRequest registerRequest = new RegisterRequest(newUsername, "password123");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", equalTo(newUsername)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andReturn();

        String registerToken = objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .path("data").path("token").asText();

        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + registerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(true)));

        LoginRequest loginRequest = new LoginRequest(newUsername, "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", equalTo(newUsername)))
                .andReturn();

        String loginToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("token").asText();

        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(true)));
    }

    @Test
    @DisplayName("Should prevent duplicate username registration")
    void testPreventDuplicateRegistration() throws Exception {
        String uniqueUsername = "user_" + System.currentTimeMillis();
        RegisterRequest firstRequest = new RegisterRequest(uniqueUsername, "pass1");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        RegisterRequest secondRequest = new RegisterRequest(uniqueUsername, "pass2");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.error.message", containsString("El nombre de usuario ya existe")));
    }
}

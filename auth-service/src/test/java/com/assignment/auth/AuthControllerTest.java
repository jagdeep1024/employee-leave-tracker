package com.assignment.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {
    @Test
    void loginReturnsJwtForSeededUser() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new AuthController("assignment-secret-key-assignment-secret-key"))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();

        String response = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"employee1@company.com\",\"password\":\"password\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).contains("token").contains("EMPLOYEE");
    }
}

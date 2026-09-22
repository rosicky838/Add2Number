package com.add2numweb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class Add2NumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testIndexPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testCalculateSuccess() throws Exception {
        mockMvc.perform(post("/calculate")
                        .param("num1", "1234")
                        .param("num2", "897"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("result", "2131"))
                .andExpect(model().attribute("num1", "1234"))
                .andExpect(model().attribute("num2", "897"))
                .andExpect(model().attribute("steps", hasSize(4)));
    }

    @Test
    void testCalculateInvalidCharacters() throws Exception {
        mockMvc.perform(post("/calculate")
                        .param("num1", "abc")
                        .param("num2", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("num1", "abc"))
                .andExpect(model().attribute("num2", "123"));
    }

    @Test
    void testCalculateNullParams() throws Exception {
        mockMvc.perform(post("/calculate"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("num1", ""))
                .andExpect(model().attribute("num2", ""));
    }

    @Test
    void testCalculateApiSuccess() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .param("num1", "999")
                        .param("num2", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.result", is("1000")))
                .andExpect(jsonPath("$.steps", hasSize(4)));
    }

    @Test
    void testCalculateApiError() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .param("num1", "abc")
                        .param("num2", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", notNullValue()));
    }
}

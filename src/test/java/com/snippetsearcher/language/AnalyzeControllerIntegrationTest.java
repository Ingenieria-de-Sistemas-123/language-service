package com.snippetsearcher.language;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AnalyzeControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void analyzeReturnsRuleAndPositions() throws Exception {
    var body =
        Map.of(
            "language", "printscript",
            "content", "let a: number = 1;\nlet a: number = 2;\n",
            "version", "1.0");

    mockMvc
        .perform(
            post("/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.issues.length()").value(greaterThan(0)))
        .andExpect(jsonPath("$.issues[0].rule").value("no-duplicate-var"))
        .andExpect(jsonPath("$.issues[0].severity").value("ERROR"))
        .andExpect(jsonPath("$.issues[0].startLine").value(greaterThan(0)))
        .andExpect(jsonPath("$.issues[0].startCol").value(greaterThan(0)))
        .andExpect(jsonPath("$.issues[0].endLine").value(greaterThan(0)))
        .andExpect(jsonPath("$.issues[0].endCol").value(greaterThan(0)));
  }
}

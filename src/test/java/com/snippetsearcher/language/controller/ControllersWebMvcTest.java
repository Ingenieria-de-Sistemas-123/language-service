package com.snippetsearcher.language.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.snippetsearcher.language.service.ParserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = {
      ValidateController.class,
      FormatController.class,
      ExecuteController.class,
      AnalyzeController.class
    })
class ControllersWebMvcTest {

  @Autowired MockMvc mvc;

  @MockitoBean ParserService parserService;

  @Test
  void validate_returns200_andBody() throws Exception {
    var resp = new ValidateController.ValidateResponse(true, List.of());
    when(parserService.validate(anyString(), anyString())).thenReturn(resp);

    mvc.perform(
            post("/validate")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                                        {"language":"printscript","content":"x=1","version":"1.0"}
                                        """))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
        .andExpect(jsonPath("$.valid").value(true))
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void format_returns200_andBody() throws Exception {
    var resp = new FormatController.FormatResponse(false, "x=1", "");
    when(parserService.format(anyString(), anyString(), anyBoolean(), any())).thenReturn(resp);

    mvc.perform(
            post("/format")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                                        {"language":"printscript","content":"x=1","version":"1.0","check":false,"configJson":null}
                                        """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.changed").value(false))
        .andExpect(jsonPath("$.formatted").value("x=1"));
  }

  @Test
  void execute_returns200_andBody() throws Exception {
    var resp = new ExecuteController.ExecuteResponse(0, "out", "");
    when(parserService.execute(anyString(), anyString(), anyString())).thenReturn(resp);

    mvc.perform(
            post("/execute")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                                        {"language":"printscript","content":"print(1)","version":"1.0","input":"abc"}
                                        """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.exitCode").value(0))
        .andExpect(jsonPath("$.stdout").value("out"));
  }

  @Test
  void analyze_returns200_andBody() throws Exception {
    var issue = new AnalyzeController.AnalyzeIssue("rule-x", "msg", "WARN", 1, 1, 1, 2);
    var resp = new AnalyzeController.AnalyzeResponse(List.of(issue), null);
    when(parserService.analyze(anyString(), anyString())).thenReturn(resp);

    mvc.perform(
            post("/analyze")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                                        {"language":"printscript","content":"x=1","version":"1.0"}
                                        """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.issues[0].rule").value("rule-x"))
        .andExpect(jsonPath("$.issues[0].severity").value("WARN"));
  }
}

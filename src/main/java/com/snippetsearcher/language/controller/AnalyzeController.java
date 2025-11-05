package com.snippetsearcher.language.controller;

import com.snippetsearcher.language.service.ParserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analyze")
public class AnalyzeController {

  public record AnalyzeRequest(String language, String content, String version) {}

  public record Issue(String message) {}

  public record AnalyzeResponse(List<Issue> issues, String raw) {}

  private final ParserService parserService;

  public AnalyzeController(ParserService parserService) {
    this.parserService = parserService;
  }

  @PostMapping
  public ResponseEntity<AnalyzeResponse> analyze(@RequestBody AnalyzeRequest req) throws Exception {
    var res = parserService.analyze(req.content(), req.version());
    return ResponseEntity.ok(res);
  }
}

package com.snippetsearcher.language.controller;

import com.snippetsearcher.language.service.ParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/execute")
public class ExecuteController {

  public record ExecuteRequest(String language, String content, String version, String input) {}

  public record ExecuteResponse(int exitCode, String stdout, String stderr) {}

  private final ParserService parserService;

  public ExecuteController(ParserService parserService) {
    this.parserService = parserService;
  }

  @PostMapping
  public ResponseEntity<ExecuteResponse> execute(@RequestBody ExecuteRequest req) throws Exception {
    var res = parserService.execute(req.content(), req.version(), req.input());
    return ResponseEntity.ok(res);
  }
}

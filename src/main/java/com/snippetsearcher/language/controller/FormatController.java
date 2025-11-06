package com.snippetsearcher.language.controller;

import com.snippetsearcher.language.service.ParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/format")
public class FormatController {

  public record FormatRequest(String language, String content, String version, Boolean check) {}

  public record FormatResponse(boolean changed, String formatted, String diagnostics) {}

  private final ParserService parserService;

  public FormatController(ParserService parserService) {
    this.parserService = parserService;
  }

  @PostMapping
  public ResponseEntity<FormatResponse> format(@RequestBody FormatRequest req) throws Exception {
    var res = parserService.format(req.content(), req.version(), Boolean.TRUE.equals(req.check()));
    return ResponseEntity.ok(res);
  }
}

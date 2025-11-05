package com.snippetsearcher.language.controller;

import com.snippetsearcher.language.service.ParserService;
import java.io.IOException;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/validate")
public class ValidateController {
  public record ValidateRequest(String language, String content) {}

  public record ValidationError(String rule, int line, int col, String message) {}

  public record ValidateResponse(boolean valid, List<ValidationError> errors) {}

  @PostMapping
  public ResponseEntity<ValidateResponse> validate(@RequestBody ValidateRequest req)
      throws IOException {
    return ResponseEntity.ok(parserService.validate(req.content));
  }

  private ParserService parserService;

  public ValidateController(ParserService parserService) {
    this.parserService = parserService;
  }
}

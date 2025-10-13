package com.snippetsearcher.language.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/validate")
public class ValidateController {
    public record ValidateRequest(String language, String content) {}
    public record ValidationError(String rule, int line, int col, String message) {}
    public record ValidateResponse(boolean valid, List<ValidationError> errors) {}

    @PostMapping
    public ResponseEntity<ValidateResponse> validate(@RequestBody ValidateRequest req) {
        if (req.content() != null && req.content().contains("ERROR")) {
            var errors = List.of(new ValidationError("ParseError", 1, 1, "Found 'ERROR' token"));
            return ResponseEntity.ok(new ValidateResponse(false, errors));
        }
        return ResponseEntity.ok(new ValidateResponse(true, List.of()));
    }
}

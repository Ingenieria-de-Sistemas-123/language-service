package com.snippetsearcher.language.service;

import com.snippetsearcher.language.controller.AnalyzeController;
import com.snippetsearcher.language.controller.ExecuteController;
import com.snippetsearcher.language.controller.FormatController;
import com.snippetsearcher.language.controller.ValidateController;
import com.snippetsearcher.language.helpers.CLIHelper;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ParserService {
  private final CLIService cliService;
  private final CLIHelper helper;
  private final LinterBridge linterBridge;

  public ParserService(final CLIService cliService, final CLIHelper helper) {
    this.cliService = cliService;
    this.helper = helper;
    this.linterBridge = new LinterBridge();
  }

  public ValidateController.ValidateResponse validate(String content, String version)
          throws IOException {
    var run =
            helper.runCliWithFile(
                    cliService,
                    content,
                    helper.args("validate", "--file", "%FILE%", "--version", helper.safe(version, "1.0")));

    if (run.exitCode() == 0) {
      return new ValidateController.ValidateResponse(true, List.of());
    }

    var errorDetails = parseCliValidationError(helper.prefer(run.stderr(), run.stdout()));
    var err =
            new ValidateController.ValidationError(
                    "parse", errorDetails.line(), errorDetails.col(), errorDetails.message());
    return new ValidateController.ValidateResponse(false, List.of(err));
  }

  public FormatController.FormatResponse format(
          String content, String version, boolean check, String configJson // puede venir null
  ) throws IOException {

    // Armamos args base
    var argsList =
            new java.util.ArrayList<String>(
                    java.util.List.of(
                            "format", "--file", "%FILE%", "--version", helper.safe(version, "1.0")));

    if (check) {
      argsList.add("--check");
    }

    java.nio.file.Path configFile = null;

    try {
      // Si viene un JSON de config, lo escribimos a un archivo temporal
      if (configJson != null && !configJson.isBlank()) {
        configFile = java.nio.file.Files.createTempFile("ps-formatter-", ".json");
        java.nio.file.Files.writeString(
                configFile, configJson, java.nio.charset.StandardCharsets.UTF_8);

        // Flag que soporta el CLI: --config (o -c)
        argsList.add("--config");
        argsList.add(configFile.toString());
      }

      String[] base = argsList.toArray(String[]::new);

      var run = helper.runCliWithFile(cliService, content, base);
      boolean changed;

      if (check) {
        changed = (run.exitCode() != 0);
        return new FormatController.FormatResponse(changed, run.stdout(), run.stderr());
      } else {
        changed = !content.equals(run.stdout());
        return new FormatController.FormatResponse(changed, run.stdout(), run.stderr());
      }

    } finally {
      // Limpieza del archivo temporal
      if (configFile != null) {
        try {
          java.nio.file.Files.deleteIfExists(configFile);
        } catch (IOException ignore) {
        }
      }
    }
  }

  public ExecuteController.ExecuteResponse execute(String content, String version, String input)
          throws IOException {
    var run =
            helper.runCliWithFileAndInput(
                    cliService,
                    content,
                    input,
                    helper.args("execute", "--file", "%FILE%", "--version", helper.safe(version, "1.0")));
    return new ExecuteController.ExecuteResponse(run.exitCode(), run.stdout(), run.stderr());
  }

  public AnalyzeController.AnalyzeResponse analyze(String content, String version)
          throws IOException {
    var outcome = linterBridge.analyze(content == null ? "" : content, helper.safe(version, "1.0"));

    var issues =
            outcome.getIssues().stream()
                    .map(
                            issue ->
                                    new AnalyzeController.AnalyzeIssue(
                                            issue.getRuleId(),
                                            issue.getMessage(),
                                            issue.getSeverity() == null ? null : issue.getSeverity().name(),
                                            issue.getStartLine(),
                                            issue.getStartCol(),
                                            issue.getEndLine(),
                                            issue.getEndCol()))
                    .toList();

    if (outcome.getError() == null && outcome.getFailure() == null) {
      return new AnalyzeController.AnalyzeResponse(issues, null);
    }

    if (outcome.getError() != null) {
      var err = outcome.getError().getError();
      var span = err.getSpan();
      var start = span.getStart();
      var end = span.getEnd();

      var issue =
              new AnalyzeController.AnalyzeIssue(
                      "parse-error",
                      err.getMessage(),
                      "ERROR",
                      start.getLine(),
                      start.getColumn(),
                      end.getLine(),
                      end.getColumn());
      return new AnalyzeController.AnalyzeResponse(java.util.List.of(issue), err.toString());
    }

    var failure = outcome.getFailure();
    var issue =
            new AnalyzeController.AnalyzeIssue(
                    "analyze-error", failure.getMessage(), "ERROR", 1, 1, 1, 1);
    return new AnalyzeController.AnalyzeResponse(java.util.List.of(issue), null);
  }

  private static final Pattern VALIDATION_ERROR_PATTERN =
          Pattern.compile("^(.+?):(\\d+):(\\d+) - (\\d+):(\\d+): error: (.+)$");

  private CliValidationError parseCliValidationError(String output) {
    if (output == null || output.isBlank()) {
      return new CliValidationError(-1, -1, "");
    }

    for (String line : output.split("\\R")) {
      var matcher = VALIDATION_ERROR_PATTERN.matcher(line.trim());
      if (matcher.matches()) {
        return new CliValidationError(
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                matcher.group(6).trim());
      }
    }

    return new CliValidationError(-1, -1, output.trim());
  }

  private record CliValidationError(int line, int col, String message) {}
}
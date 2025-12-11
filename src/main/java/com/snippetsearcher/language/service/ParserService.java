package com.snippetsearcher.language.service;

import com.snippetsearcher.language.controller.AnalyzeController;
import com.snippetsearcher.language.controller.ExecuteController;
import com.snippetsearcher.language.controller.FormatController;
import com.snippetsearcher.language.controller.ValidateController;
import com.snippetsearcher.language.helpers.CLIHelper;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ParserService {
  private final CLIService cliService;
  private final CLIHelper helper;

  public ParserService(final CLIService cliService, final CLIHelper helper) {
    this.cliService = cliService;
    this.helper = helper;
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
    } else {
      var err =
          new ValidateController.ValidationError(
              "parse", -1, -1, helper.prefer(run.stderr(), run.stdout()));
      return new ValidateController.ValidateResponse(false, List.of(err));
    }
  }

  public FormatController.FormatResponse format(String content, String version, boolean check)
      throws IOException {
    String[] base =
        check
            ? helper.args(
                "format", "--file", "%FILE%", "--version", helper.safe(version, "1.0"), "--check")
            : helper.args("format", "--file", "%FILE%", "--version", helper.safe(version, "1.0"));

    var run = helper.runCliWithFile(cliService, content, base);
    boolean changed;

    if (check) {
      changed = (run.exitCode() != 0);
      return new FormatController.FormatResponse(changed, run.stdout(), run.stderr());
    } else {
      changed = !content.equals(run.stdout());
      return new FormatController.FormatResponse(changed, run.stdout(), run.stderr());
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
    var run =
        helper.runCliWithFile(
            cliService,
            content,
            helper.args("analyze", "--file", "%FILE%", "--version", helper.safe(version, "1.0")));
    var issues =
        run.stdout().lines().filter(s -> !s.isBlank()).map(AnalyzeController.Issue::new).toList();
    return new AnalyzeController.AnalyzeResponse(issues, helper.prefer(run.stdout(), run.stderr()));
  }
}

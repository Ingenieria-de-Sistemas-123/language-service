package com.snippetsearcher.language.service;

import com.snippetsearcher.language.controller.ValidateController;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;
import picocli.CommandLine;

@Service
public class ParserService {
  private final CLIService cliService;

  public ParserService(final CLIService cliService) {
    this.cliService = cliService;
  }

  public ValidateController.ValidateResponse validate(String input) throws IOException {
    File tempFile = File.createTempFile("mi-temp-", ".txt");
    var exit = (new CommandLine(cliService)).execute("validate", "--file", tempFile.getAbsolutePath());
    if (exit != 0) {
      return new ValidateController.ValidateResponse(false, List.of());
    } else {
      return new ValidateController.ValidateResponse(true, List.of());
    }
  }
}

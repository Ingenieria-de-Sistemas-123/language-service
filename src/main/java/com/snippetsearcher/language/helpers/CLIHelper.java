package com.snippetsearcher.language.helpers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class CLIHelper {

  public CliRun runCliWithFile(Object cliService, String content, String[] args)
          throws IOException {
    File temp = Files.createTempFile("printscript-", ".ps").toFile();
    temp.deleteOnExit();

    try (var w = new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8)) {
      w.write(content == null ? "" : content);
    }

    String[] resolved = new String[args.length];
    for (int i = 0; i < args.length; i++) {
      resolved[i] = "%FILE%".equals(args[i]) ? temp.getAbsolutePath() : args[i];
    }

    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
    PrintWriter picocliOut =
            new PrintWriter(new OutputStreamWriter(outBytes, StandardCharsets.UTF_8), true);
    PrintWriter picocliErr =
            new PrintWriter(new OutputStreamWriter(errBytes, StandardCharsets.UTF_8), true);

    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;

    System.setOut(new PrintStream(outBytes, true, StandardCharsets.UTF_8));
    System.setErr(new PrintStream(errBytes, true, StandardCharsets.UTF_8));

    int code;
    try {
      CommandLine cmd = new CommandLine(cliService).setOut(picocliOut).setErr(picocliErr);

      code = cmd.execute(resolved);
    } finally {
      System.setOut(originalOut);
      System.setErr(originalErr);
    }

    String stdout = outBytes.toString(StandardCharsets.UTF_8);
    String stderr = errBytes.toString(StandardCharsets.UTF_8);
    return new CliRun(code, stdout, stderr);
  }

  public CliRun runCliWithFileAndInput(
          Object cliService, String content, String input, String[] args) throws IOException {

    File temp = Files.createTempFile("printscript-", ".ps").toFile();
    temp.deleteOnExit();

    try (var w = new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8)) {
      w.write(content == null ? "" : content);
    }

    String[] resolved = new String[args.length];
    for (int i = 0; i < args.length; i++) {
      resolved[i] = "%FILE%".equals(args[i]) ? temp.getAbsolutePath() : args[i];
    }

    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
    PrintWriter picocliOut =
            new PrintWriter(new OutputStreamWriter(outBytes, StandardCharsets.UTF_8), true);
    PrintWriter picocliErr =
            new PrintWriter(new OutputStreamWriter(errBytes, StandardCharsets.UTF_8), true);

    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;
    InputStream originalIn = System.in;

    System.setOut(new PrintStream(outBytes, true, StandardCharsets.UTF_8));
    System.setErr(new PrintStream(errBytes, true, StandardCharsets.UTF_8));
    byte[] inBytes = (input == null ? "" : input).getBytes(StandardCharsets.UTF_8);
    System.setIn(new ByteArrayInputStream(inBytes));

    int code;
    try {
      CommandLine cmd = new CommandLine(cliService).setOut(picocliOut).setErr(picocliErr);
      code = cmd.execute(resolved);
    } finally {
      System.setOut(originalOut);
      System.setErr(originalErr);
      System.setIn(originalIn);
    }

    String stdout = outBytes.toString(StandardCharsets.UTF_8);
    String stderr = errBytes.toString(StandardCharsets.UTF_8);
    return new CliRun(code, stdout, stderr);
  }

  public String safe(String value, String fallback) {
    return (value == null || value.isBlank()) ? fallback : value;
  }

  public String prefer(String preferred, String fallback) {
    return (preferred == null || preferred.isBlank()) ? fallback : preferred;
  }

  public String[] args(String... arr) {
    return arr;
  }

  public record CliRun(int exitCode, String stdout, String stderr) {}
}
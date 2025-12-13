package com.snippetsearcher.language.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

class CLIHelperTest {

  private final CLIHelper helper = new CLIHelper();

  @Command(name = "echo")
  static class EchoCommand implements Callable<Integer> {
    @Option(names = "--file")
    File file;

    @Option(names = "--version")
    String version;

    @Override
    public Integer call() throws Exception {
      String content = Files.readString(file.toPath());
      System.out.print("file:" + content + "|version:" + version);
      return 0;
    }
  }

  @Command(name = "input")
  static class InputCommand implements Callable<Integer> {
    @Option(names = "--file")
    File file;

    @Override
    public Integer call() throws Exception {
      String content = Files.readString(file.toPath());
      byte[] bytes = System.in.readAllBytes();
      System.out.print("content:" + content + "|in:" + new String(bytes));
      return 0;
    }
  }

  @Test
  void runCliWithFilePassesTempFileAndReturnsOutput() throws IOException {
    CLIHelper.CliRun run =
        helper.runCliWithFile(
            new EchoCommand(), "print('hi');", helper.args("--file", "%FILE%", "--version", "1.1"));

    assertThat(run.exitCode()).isZero();
    assertThat(run.stdout()).isEqualTo("file:print('hi');|version:1.1");
    assertThat(run.stderr()).isEmpty();
  }

  @Test
  void runCliWithFileAndInputRestoresStreams() throws IOException {
    var originalIn = System.in;
    CLIHelper.CliRun run =
        helper.runCliWithFileAndInput(
            new InputCommand(), "input content", "typed", helper.args("--file", "%FILE%"));

    assertThat(run.exitCode()).isZero();
    assertThat(run.stdout()).isEqualTo("content:input content|in:typed");
    assertThat(run.stderr()).isEmpty();
    assertThat(System.in).isSameAs(originalIn);
  }

  @Test
  void safePrefersFallbackForBlankValue() {
    assertThat(helper.safe(" ", "default")).isEqualTo("default");
    assertThat(helper.safe("keep", "default")).isEqualTo("keep");
  }

  @Test
  void preferUsesFallbackWhenPreferredBlank() {
    assertThat(helper.prefer(" ", "other")).isEqualTo("other");
    assertThat(helper.prefer("value", "other")).isEqualTo("value");
  }

  @Test
  void argsReturnsSameArray() {
    String[] arr = new String[] {"a", "b"};
    assertThat(helper.args(arr)).isSameAs(arr);
  }
}

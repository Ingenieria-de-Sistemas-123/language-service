package com.snippetsearcher.language.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.snippetsearcher.language.controller.AnalyzeController;
import com.snippetsearcher.language.controller.ExecuteController;
import com.snippetsearcher.language.controller.FormatController;
import com.snippetsearcher.language.controller.ValidateController;
import com.snippetsearcher.language.helpers.CLIHelper;
import com.snippetsearcher.language.helpers.CLIHelper.CliRun;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParserServiceTest {

  @Test
  void validate_ok_exitCode0_returnsValidTrueNoErrors() throws Exception {
    CLIService cli = mock(CLIService.class);
    CLIHelper helper = spy(new CLIHelper());

    doReturn(new CliRun(0, "ok", ""))
        .when(helper)
        .runCliWithFile(eq(cli), anyString(), any(String[].class));

    ParserService svc = new ParserService(cli, helper);

    ValidateController.ValidateResponse res = svc.validate("let a: number = 1;", "1.0");

    assertTrue(res.valid());
    assertNotNull(res.errors());
    assertEquals(0, res.errors().size());
  }

  @Test
  void validate_error_parsesStdErrPattern_andReturnsSingleParseError() throws Exception {
    CLIService cli = mock(CLIService.class);
    CLIHelper helper = spy(new CLIHelper());

    // Matchea: ^(.+?):(\d+):(\d+) - (\d+):(\d+): error: (.+)$
    String errLine = "snippet.ps:12:34 - 12:35: error: Unexpected token";
    doReturn(new CliRun(1, "", errLine))
        .when(helper)
        .runCliWithFile(eq(cli), anyString(), any(String[].class));

    ParserService svc = new ParserService(cli, helper);

    ValidateController.ValidateResponse res = svc.validate("bad", "1.0");

    assertFalse(res.valid());
    assertEquals(1, res.errors().size());
    ValidateController.ValidationError e = res.errors().get(0);

    assertEquals("parse", e.rule());
    assertEquals(12, e.line());
    assertEquals(34, e.col());
    assertEquals("Unexpected token", e.message());
  }

  @Test
  void validate_error_noPattern_returnsLineColMinus1_andMessageIsWholeOutputTrimmed()
      throws Exception {
    CLIService cli = mock(CLIService.class);
    CLIHelper helper = spy(new CLIHelper());

    String raw = "some weird error output\nsecond line";
    doReturn(new CliRun(2, raw, ""))
        .when(helper)
        .runCliWithFile(eq(cli), anyString(), any(String[].class));

    ParserService svc = new ParserService(cli, helper);

    ValidateController.ValidateResponse res = svc.validate("bad", "1.0");

    assertFalse(res.valid());
    assertEquals(1, res.errors().size());
    var e = res.errors().get(0);

    assertEquals(-1, e.line());
    assertEquals(-1, e.col());
    assertEquals(raw.trim(), e.message());
  }

  @Test
  void execute_callsHelperRunCliWithFileAndInput_andMapsResponse() throws Exception {
    CLIService cli = mock(CLIService.class);
    CLIHelper helper = spy(new CLIHelper());

    doReturn(new CliRun(0, "hello", ""))
        .when(helper)
        .runCliWithFileAndInput(eq(cli), anyString(), anyString(), any(String[].class));

    ParserService svc = new ParserService(cli, helper);

    ExecuteController.ExecuteResponse res = svc.execute("print(\"hi\")", "1.0", "stdin");

    assertEquals(0, res.exitCode());
    assertEquals("hello", res.stdout());
    assertEquals("", res.stderr());
  }

  @Test
  void format_checkTrue_exitCodeNonZero_changedTrue() throws Exception {
    CLIService cli = mock(CLIService.class);
    CLIHelper helper = spy(new CLIHelper());

    doReturn(new CliRun(1, "formatted", "diagnostics"))
        .when(helper)
        .runCliWithFile(eq(cli), anyString(), any(String[].class));

    ParserService svc = new ParserService(cli, helper);

    FormatController.FormatResponse res = svc.format("x=1", "1.0", true, null);

    assertTrue(res.changed());
    assertEquals("formatted", res.formatted());
    assertEquals("diagnostics", res.diagnostics());
  }

  @Test
  void format_checkFalse_comparesContentVsStdout_changedAccordingly() throws Exception {
    CLIService cli = mock(CLIService.class);
    CLIHelper helper = spy(new CLIHelper());

    // stdout distinto => changed = true
    doReturn(new CliRun(0, "x = 1\n", ""))
        .when(helper)
        .runCliWithFile(eq(cli), anyString(), any(String[].class));

    ParserService svc = new ParserService(cli, helper);

    FormatController.FormatResponse res = svc.format("x=1\n", "1.0", false, null);

    assertTrue(res.changed());
    assertEquals("x = 1\n", res.formatted());
    assertEquals("", res.diagnostics());
  }

  @Test
  void format_withConfigJson_passesConfigFlag_andDeletesTempFileAfter() throws Exception {
    CLIService cli = mock(CLIService.class);
    CLIHelper helper = spy(new CLIHelper());

    // Capturamos args para verificar --config + path existente durante la ejecución
    doAnswer(
            inv -> {
              String[] args = inv.getArgument(2, String[].class);

              int idx = -1;
              for (int i = 0; i < args.length; i++) {
                if ("--config".equals(args[i])) {
                  idx = i;
                  break;
                }
              }
              assertTrue(idx >= 0, "Debe incluir flag --config");
              assertTrue(idx + 1 < args.length, "Debe incluir path luego de --config");

              Path p = Path.of(args[idx + 1]);
              assertTrue(Files.exists(p), "El config temp debe existir al momento de ejecutar");

              return new CliRun(0, "ok", "");
            })
        .when(helper)
        .runCliWithFile(eq(cli), anyString(), any(String[].class));

    ParserService svc = new ParserService(cli, helper);

    String configJson = "{\"indentSize\":2}";
    svc.format("x=1", "1.0", false, configJson);

    // Verificamos que el archivo temp ya no exista (borrado en finally)
    // Para obtener el path real, volvemos a capturar args usados:
    ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
    verify(helper, atLeastOnce()).runCliWithFile(eq(cli), anyString(), captor.capture());
    String[] used = captor.getAllValues().get(captor.getAllValues().size() - 1);

    int idx = -1;
    for (int i = 0; i < used.length; i++) {
      if ("--config".equals(used[i])) {
        idx = i;
        break;
      }
    }
    Path p = Path.of(used[idx + 1]);
    assertFalse(Files.exists(p), "El config temp debe borrarse al finalizar");
  }

  @Test
  void analyze_whenBridgeFails_returnsAnalyzeErrorIssue() throws Exception {
    CLIService cli = mock(CLIService.class);
    CLIHelper helper = spy(new CLIHelper());
    ParserService svc = new ParserService(cli, helper);

    // Forzamos que LinterBridge falle para cubrir rama "failure"
    Field f = ParserService.class.getDeclaredField("linterBridge");
    f.setAccessible(true);

    LinterBridge failing =
        new LinterBridge() {
          @Override
          public LintOutcome analyze(String content, String version) {
            return LintOutcome.failure(new RuntimeException("boom-" + UUID.randomUUID()));
          }
        };

    f.set(svc, failing);

    AnalyzeController.AnalyzeResponse res = svc.analyze("x=1", "1.0");

    assertNotNull(res.issues());
    assertEquals(1, res.issues().size());
    AnalyzeController.AnalyzeIssue issue = res.issues().get(0);

    assertEquals("analyze-error", issue.rule());
    assertEquals("ERROR", issue.severity());
    assertEquals(1, issue.startLine());
    assertEquals(1, issue.startCol());
  }
}

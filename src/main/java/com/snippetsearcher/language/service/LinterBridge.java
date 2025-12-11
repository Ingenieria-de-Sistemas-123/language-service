package com.snippetsearcher.language.service;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.printscript.cli.adapters.CliFailure;
import org.printscript.cli.adapters.FrontendAdapter;
import org.printscript.cli.adapters.LinterAdapter;
import org.printscript.linter.issue.Issue;
import org.printscript.parser.node.ASTNode;

public class LinterBridge {

  public static final class LintOutcome {
    private final List<Issue> issues;
    private final CliFailure error;
    private final Throwable failure;

    public LintOutcome(List<Issue> issues, CliFailure error, Throwable failure) {
      this.issues = issues == null ? Collections.emptyList() : issues;
      this.error = error;
      this.failure = failure;
    }

    public static LintOutcome success(List<Issue> issues) {
      return new LintOutcome(issues, null, null);
    }

    public static LintOutcome parseError(CliFailure error) {
      return new LintOutcome(Collections.emptyList(), error, null);
    }

    public static LintOutcome failure(Throwable t) {
      return new LintOutcome(Collections.emptyList(), null, t);
    }

    public List<Issue> getIssues() {
      return issues;
    }

    public CliFailure getError() {
      return error;
    }

    public Throwable getFailure() {
      return failure;
    }
  }

  public LintOutcome analyze(String content, String version) {
    String src = content == null ? "" : content;
    String ver = (version == null || version.isBlank()) ? "1.0" : version;

    // FrontendAdapter has a mangled parseProgram method name; call via reflection.
    try {
      FrontendAdapter frontend = new FrontendAdapter(ver, null);
      Method parse =
          frontend.getClass().getMethod("parseProgram-gIAlu-s", String.class, String.class);
      Object parsed = parse.invoke(frontend, src, "snippet.ps");

      // Kotlin Result helpers via reflection
      Class<?> resultClass = Class.forName("kotlin.Result");
      Method isFailure = resultClass.getMethod("isFailure-impl", Object.class);
      Method exceptionOrNull = resultClass.getMethod("exceptionOrNull-impl", Object.class);

      boolean failed = (Boolean) isFailure.invoke(null, parsed);
      if (!failed) {
        @SuppressWarnings("unchecked")
        List<ASTNode> nodes = (List<ASTNode>) parsed; // success value is the AST list
        var issues = new LinterAdapter().analyze(nodes);
        return LintOutcome.success(issues);
      }

      Throwable failure = (Throwable) exceptionOrNull.invoke(null, parsed);
      if (failure instanceof CliFailure cliFailure) {
        return LintOutcome.parseError(cliFailure);
      }
      return LintOutcome.failure(failure);

    } catch (Exception e) {
      return LintOutcome.failure(e);
    }
  }
}

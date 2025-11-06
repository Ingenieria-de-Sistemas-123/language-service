package com.snippetsearcher.language.service

import org.printscript.cli.commands.AnalyzeCmd
import org.printscript.cli.commands.ExecuteCmd
import org.printscript.cli.commands.FormatCmd
import org.printscript.cli.commands.ValidateCmd
import org.springframework.stereotype.Service
import picocli.CommandLine.Command

@Command(
    name = "printscript",
    mixinStandardHelpOptions = true,
    version = ["PrintScript CLI 1.0"],
    description = ["CLI para validar, ejecutar, formatear y analizar programas PrintScript."],
    subcommands = [ValidateCmd::class, ExecuteCmd::class, FormatCmd::class, AnalyzeCmd::class],
)

@Service
class CLIService {

}
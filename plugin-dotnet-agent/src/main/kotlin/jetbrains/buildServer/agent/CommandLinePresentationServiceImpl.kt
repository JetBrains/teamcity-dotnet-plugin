package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.runner.Color
import jetbrains.buildServer.agent.runner.StdOutText
import java.io.File

class CommandLinePresentationServiceImpl : CommandLinePresentationService {
    override fun buildExecutableFilePresentation(executableFile: File): List<StdOutText> {
        val output = mutableListOf<StdOutText>()
        executableFile.parent?.let { output.add(StdOutText(it + File.separator, Color.Minor)) }
        output.add(StdOutText(executableFile.name, Color.Header))
        return output
    }

    override fun buildArgsPresentation(arguments: List<CommandLineArgument>): List<StdOutText> =
            arguments.map {
                StdOutText(
                        " ${it.value}",
                        when (it.argumentType) {
                            CommandLineArgumentType.Mandatory -> Color.Header
                            CommandLineArgumentType.Custom -> Color.Details
                            CommandLineArgumentType.Infrastructural -> Color.Minor
                            else -> Color.Default
                        }
                )
            }
}
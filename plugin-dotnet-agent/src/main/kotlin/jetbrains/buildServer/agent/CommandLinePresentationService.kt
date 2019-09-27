package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.runner.StdOutText
import java.io.File

interface CommandLinePresentationService {
    fun buildExecutableFilePresentation(executableFile: File): List<StdOutText>

    fun buildArgsPresentation(arguments: List<CommandLineArgument>): List<StdOutText>
}
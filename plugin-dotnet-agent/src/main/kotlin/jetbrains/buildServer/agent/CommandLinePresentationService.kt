

package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.runner.StdOutText

interface CommandLinePresentationService {
    fun buildExecutablePresentation(executableFile: Path): List<StdOutText>

    fun buildArgsPresentation(arguments: List<CommandLineArgument>): List<StdOutText>
}
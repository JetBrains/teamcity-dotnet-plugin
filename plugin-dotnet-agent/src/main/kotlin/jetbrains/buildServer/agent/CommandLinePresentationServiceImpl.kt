package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.runner.Color
import jetbrains.buildServer.agent.runner.StdOutText
import jetbrains.buildServer.util.OSType
import java.io.File

class CommandLinePresentationServiceImpl(
        private val _environment: Environment,
        private val _argumentsService: ArgumentsService,
        private val _virtualContext: VirtualContext)
    : CommandLinePresentationService {
    override fun buildExecutablePresentation(executableFile: Path): List<StdOutText> {
        val output = mutableListOf<StdOutText>()
        val lastSeparatorIndex = executableFile.path.indexOfLast { it == File.separatorChar || it == '\\' || it == '/' }
        if (lastSeparatorIndex >= 0) {
            output.add(StdOutText(executableFile.path.substring(0 .. lastSeparatorIndex - 1) + separatorChar, Color.Minor))
        }

        output.add(StdOutText(executableFile.path.substring(lastSeparatorIndex + 1), Color.Header))
        return output
    }

    override fun buildArgsPresentation(arguments: List<CommandLineArgument>): List<StdOutText> =
            arguments.map {
                StdOutText(
                        " ${_argumentsService.normalize(it.value)}",
                        when (it.argumentType) {
                            CommandLineArgumentType.Mandatory -> Color.Header
                            CommandLineArgumentType.Target -> Color.Header
                            CommandLineArgumentType.Custom -> Color.Details
                            CommandLineArgumentType.Infrastructural -> Color.Minor
                            else -> Color.Default
                        }
                )
            }

    private val separatorChar get() = when {
        _environment.os == _virtualContext.targetOSType -> File.separatorChar
        _virtualContext.targetOSType == OSType.WINDOWS -> '\\'
        else -> '/'
    }
}


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
        val executableFilePath = _argumentsService.normalize(executableFile.path)
        val output = mutableListOf<StdOutText>()
        val lastSeparatorIndex = executableFilePath.indexOfLast { it == File.separatorChar || it == '\\' || it == '/' }
        if (lastSeparatorIndex >= 0) {
            output.add(StdOutText(executableFilePath.substring(0 .. lastSeparatorIndex - 1) + separatorChar))
        }

        output.add(StdOutText(executableFilePath.substring(lastSeparatorIndex + 1)))
        return output
    }

    override fun buildArgsPresentation(arguments: List<CommandLineArgument>): List<StdOutText> =
            arguments.map {
                StdOutText(
                        " ${_argumentsService.normalize(it.value)}",
                        when (it.argumentType) {
                            CommandLineArgumentType.Mandatory -> Color.Default
                            CommandLineArgumentType.Target -> Color.Default
                            CommandLineArgumentType.Custom -> Color.Default
                            CommandLineArgumentType.Infrastructural -> Color.Default
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
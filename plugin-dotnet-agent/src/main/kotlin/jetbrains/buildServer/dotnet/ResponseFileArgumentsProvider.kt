package jetbrains.buildServer.dotnet

import com.intellij.openapi.util.SystemInfo
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.*
import java.io.File
import java.io.OutputStreamWriter
import kotlin.coroutines.experimental.buildSequence

class ResponseFileArgumentsProvider(
        private val _pathsService: PathsService,
        private val _argumentsService: ArgumentsService,
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService,
        private val _loggerService: LoggerService,
        private val _msBuildParameterConverter: MSBuildParameterConverter,
        private val _buildStepContext: BuildStepContext,
        private val _argumentsProviders: List<ArgumentsProvider>,
        private val _parametersProviders: List<MSBuildParametersProvider>)
    : ArgumentsProvider {
    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            val args = _argumentsProviders.flatMap { it.arguments.toList() }
            val params = _parametersProviders.flatMap { it.parameters.toList() }

            if (args.isEmpty() && params.isEmpty()) {
                return@buildSequence
            }

            val argLine = _argumentsService.combine(args.map { it.value }.asSequence(), "\n")
            val paramLines = params.map { _msBuildParameterConverter.convert(it) }
            val lines = listOf(argLine) + paramLines

            _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
                Verbosity.tryParse(it)?.let {
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when(it) {
                        Verbosity.Detailed, Verbosity.Diagnostic -> {
                            _loggerService.onBlock(BlockName).use {
                                for (arg in args) {
                                    _loggerService.onStandardOutput(arg.value, Color.Details)
                                }

                                for (paramLine in paramLines) {
                                    _loggerService.onStandardOutput(paramLine, Color.Details)
                                }
                            }
                        }
                    }
                }
            }

            val tempDirectory = _pathsService.getPath(PathType.AgentTemp)
            val msBuildResponseFile = File(tempDirectory, _pathsService.uniqueName + ResponseFileExtension).absoluteFile
            _fileSystemService.write(msBuildResponseFile) {
                OutputStreamWriter(it).use { writer ->
                    // In Windows docker container paths should be on drive C
                    val windowsPathMappingRequired = SystemInfo.isWindows &&
                            _buildStepContext.runnerContext.isVirtualContext &&
                            !_pathsService.getPath(PathType.Bin).absolutePath.startsWith("C:", true)
                    (if (windowsPathMappingRequired) lines.map {

                        DRIVE_LETTER_PATTERN.replaceFirst(it, "$1C$3")
                    } else lines).forEach {
                        writer.write("$it\n")
                    }
                }
            }

            yield(CommandLineArgument("@${msBuildResponseFile.path}"))
        }

    companion object {
        internal const val ResponseFileExtension = ".rsp"
        internal const val BlockName = "MSBuild Response File"
        private val DRIVE_LETTER_PATTERN = Regex("(.*)([a-z])((:\\\\|%3A%5C).+)", RegexOption.IGNORE_CASE)
    }
}
package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.*
import java.io.File
import java.io.OutputStreamWriter
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class ResponseFileArgumentsProvider(
        private val _pathsService: PathsService,
        private val _argumentsService: ArgumentsService,
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService,
        private val _loggerService: LoggerService,
        private val _msBuildParameterConverter: MSBuildParameterConverter,
        private val _argumentsProviders: List<ArgumentsProvider>,
        private val _parametersProviders: List<MSBuildParametersProvider>)
    : ArgumentsProvider {
    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            var args = _argumentsProviders.flatMap { it.arguments.toList() }
            var params = _parametersProviders.flatMap { it.parameters.toList() }

            if (args.size == 0 && params.size == 0) {
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

            val tempDirectory = _pathsService.getPath(PathType.BuildTemp)
            val msbuildResponseFile = File(tempDirectory, _pathsService.uniqueName + ResponseFileExtension).absoluteFile
            _fileSystemService.write(msbuildResponseFile) {
                OutputStreamWriter(it).use {
                    for(line in lines) {
                        it.write("$line\n")
                    }
                }
            }

            yield(CommandLineArgument("@${msbuildResponseFile.path}"))
        }

    companion object {
        internal const val ResponseFileExtension = ".rsp"
        internal const val BlockName = "MSBuild Response File"
    }
}
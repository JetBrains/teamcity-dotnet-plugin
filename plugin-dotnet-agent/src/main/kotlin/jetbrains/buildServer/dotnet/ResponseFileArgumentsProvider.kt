package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.*
import java.io.File
import java.io.OutputStreamWriter
import kotlin.coroutines.experimental.buildSequence

class ResponseFileArgumentsProvider(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService,
        private val _argumentsService: ArgumentsService,
        private val _loggerService: LoggerService,
        private val _argumentsProviders: List<ArgumentsProvider>)
    : ArgumentsProvider {
    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            val args = _argumentsProviders.asSequence().flatMap { it.arguments }.map { it.value }.toList()
            if (!args.any()) {
                return@buildSequence
            }

            _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
                Verbosity.tryParse(it)?.let {
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when(it) {
                        Verbosity.Detailed, Verbosity.Diagnostic -> {
                            _loggerService.onBlock(BlockName).use {
                                for (arg in args) {
                                    _loggerService.onStandardOutput(arg, Color.Details)
                                }
                            }
                        }
                    }
                }
            }

            val tempDirectory = _pathsService.getPath(PathType.BuildTemp)
            val msBuildResponseFile = File(tempDirectory, _pathsService.uniqueName + ResponseFileExtension).absoluteFile
            val rspContent = _argumentsService.combine(args.asSequence(), "\n")
            _fileSystemService.write(msBuildResponseFile) {
                OutputStreamWriter(it).use {
                    it.write(rspContent)
                }
            }

            yield(CommandLineArgument("@${msBuildResponseFile.path}"))
        }

    companion object {
        internal const val ResponseFileExtension = ".rsp"
        internal const val BlockName = "MSBuild Response File"
    }
}
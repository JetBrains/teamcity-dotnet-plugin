@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

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
        private val _argumentsService: ArgumentsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerService: LoggerService,
        private val _msBuildParameterConverter: MSBuildParameterConverter,
        private val _argumentsProviders: List<ArgumentsProvider>,
        private val _parametersProviders: List<MSBuildParametersProvider>)
    : ArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = buildSequence {
        val args = _argumentsProviders.flatMap { it.getArguments(context).toList() }
        val params = _parametersProviders.flatMap { it.getParameters(context).toList() }

        if (args.isEmpty() && params.isEmpty()) {
            return@buildSequence
        }

        val argLine = _argumentsService.combine(args.map { it.value }.asSequence(), "\n")
        val paramLines = params.map { _msBuildParameterConverter.convert(it) }
        val lines = listOf(argLine) + paramLines

        context.verbosityLevel?.let {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (it) {
                Verbosity.Detailed, Verbosity.Diagnostic -> {
                    _loggerService.writeBlock(BlockName).use {
                        for ((value) in args) {
                            _loggerService.writeStandardOutput(value, Color.Details)
                        }

                        for (paramLine in paramLines) {
                            _loggerService.writeStandardOutput(paramLine, Color.Details)
                        }
                    }
                }
            }
        }

        val tempDirectory = _pathsService.getPath(PathType.AgentTemp)
        val msBuildResponseFile = File(tempDirectory, _pathsService.uniqueName + ResponseFileExtension).absoluteFile
        _fileSystemService.write(msBuildResponseFile) {
            OutputStreamWriter(it).use {
                for (line in lines) {
                    it.write("$line\n")
                }
            }
        }

        yield(CommandLineArgument("@${msBuildResponseFile.path}"))
    }

    companion object {
        internal const val ResponseFileExtension = ".rsp"
        internal const val BlockName = "MSBuild Response File"
    }
}
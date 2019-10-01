package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.DotCoverWorkflowComposer
import jetbrains.buildServer.rx.use
import java.io.File
import java.io.OutputStreamWriter

class ResponseFileArgumentsProvider(
        private val _pathsService: PathsService,
        private val _argumentsService: ArgumentsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerService: LoggerService,
        private val _msBuildParameterConverter: MSBuildParameterConverter,
        private val _argumentsProviders: List<ArgumentsProvider>,
        private val _parametersProviders: List<MSBuildParametersProvider>,
        private val _virtualContext: VirtualContext)
    : ArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        val args = _argumentsProviders.flatMap { it.getArguments(context).toList() }
        val params = _parametersProviders.flatMap { it.getParameters(context).toList() }

        if (args.isEmpty() && params.isEmpty()) {
            return@sequence
        }

        context.verbosityLevel?.let {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (it) {
                Verbosity.Detailed, Verbosity.Diagnostic -> {
                    _loggerService.writeBlock(BlockName).use {
                        for ((value) in args) {
                            _loggerService.writeStandardOutput(value, Color.Details)
                        }

                        for (param in params) {
                            _loggerService.writeStandardOutput("/p:${param.name}=${param.value}", Color.Details)
                        }
                    }
                }
            }
        }

        val lines = args.map { it.value } + params.map { _msBuildParameterConverter.convert(it) }
        val msBuildResponseFile = _pathsService.getTempFileName(ResponseFileExtension)
        _fileSystemService.write(msBuildResponseFile) {
            OutputStreamWriter(it).use {
                for (line in lines) {
                    it.write("$line\n")
                }
            }
        }

        yield(CommandLineArgument("@${_virtualContext.resolvePath(msBuildResponseFile.path)}", CommandLineArgumentType.Infrastructural))
    }

    companion object {
        internal const val ResponseFileExtension = ".rsp"
        internal const val BlockName = "MSBuild Response File"
        val nodeReuseArgument = CommandLineArgument("/nodeReuse:false", CommandLineArgumentType.Infrastructural)
    }
}
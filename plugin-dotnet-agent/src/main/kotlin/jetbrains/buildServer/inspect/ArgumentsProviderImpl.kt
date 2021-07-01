package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class ArgumentsProviderImpl(
        private val _parametersService: ParametersService,
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService)
    : ArgumentsProvider {
    override fun getArguments(tool: InspectionTool): InspectionArguments {
        val customArguments = _parametersService.tryGetParameter(ParameterType.Runner, tool.customArgs)?.let {
            it.lineSequence().filter { it.isNotBlank() }.map { CommandLineArgument(it, CommandLineArgumentType.Custom) }
        }?.toMutableList() ?: mutableListOf()
        val configFileArg = processFileArg(customArguments, ConfigArgRegex, tool.runnerType, ".config")
        val outputFileArg = processFileArg(customArguments, OutputArgRegex, "${tool.toolName}-report", ".xml")
        val logFileArg = processFileArg(customArguments, LogArgRegex, tool.runnerType, ".log")
        val cachesHomeArg = processFileArg(customArguments, CachesHomeArgRegex, tool.runnerType, "").let { if (it.custom) it else null }
        val debug = _parametersService.tryGetParameter(ParameterType.Runner, tool.debugSettings) != null || logFileArg.custom
        return InspectionArguments(
                configFileArg.file,
                outputFileArg.file,
                logFileArg.file,
                cachesHomeArg?.file ?: _pathsService.getPath(PathType.CachePerCheckout),
                debug,
                customArguments)
    }

    private fun processFileArg(customArguments: MutableCollection<CommandLineArgument>, regex: Regex, prefix: String, extension: String): FileArg =
            tryFindArgumentValue(customArguments, regex)
                    ?.let {
                        customArguments.remove(it.arg)
                        it.value
                    }
                    ?.let { File(it) }
                    ?.let {
                        if (!_fileSystemService.isAbsolute(it))
                            FileArg(File(_pathsService.getPath(PathType.Checkout), it.path), true)
                        else
                            FileArg(it, true)
                    }
                    ?: FileArg(
                            _fileSystemService.generateTempFile(
                                    _pathsService.getPath(PathType.AgentTemp),
                                    prefix,
                                    extension),
                            false)

    private fun tryFindArgumentValue(arguments: Collection<CommandLineArgument>, regex: Regex) =
            arguments.mapNotNull { arg ->
                regex
                        .matchEntire(arg.value)
                        ?.groupValues
                        ?.get(2)
                        ?.let { Arg(arg, it) }
            }.firstOrNull()

    private data class FileArg(val file: File, val custom: Boolean)

    private data class Arg(val arg: CommandLineArgument, val value: String)

    companion object {
        private val ConfigArgRegex = Regex("(--config)=(.+)", RegexOption.IGNORE_CASE)
        private val OutputArgRegex = Regex("(--output|[-/]o)=(.+)", RegexOption.IGNORE_CASE)
        private val LogArgRegex = Regex("(--logFile)=(.+)", RegexOption.IGNORE_CASE)
        private val CachesHomeArgRegex = Regex("(--caches-home)=(.+)", RegexOption.IGNORE_CASE)
    }
}
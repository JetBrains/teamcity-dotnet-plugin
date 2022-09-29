package jetbrains.buildServer.inspect

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
    override fun getArguments(tool: InspectionTool) =
        getCustomArguments(tool)
            .let { args ->
                val configFileArg = processFileArg(args, ConfigArgRegex, tool.runnerType, ".config")
                val outputFileArg = processFileArg(args, OutputArgRegex, "${tool.toolName}-report", ".xml")
                val logFileArg = processFileArg(args, LogArgRegex, tool.runnerType, ".log")
                val cachesHomeArg = processFileArg(args, CachesHomeArgRegex, tool.runnerType, "")
                    .let { when {
                        it.custom -> it.file
                        else -> _pathsService.getPath(PathType.CachePerCheckout)
                    } }
                val debug = _parametersService.tryGetParameter(ParameterType.Runner, tool.debugSettings) != null || logFileArg.custom

                InspectionArguments(configFileArg.file, outputFileArg.file, logFileArg.file, cachesHomeArg, debug, args)
            }

    private fun getCustomArguments(tool: InspectionTool) =
        _parametersService.tryGetParameter(ParameterType.Runner, tool.customArgs)
            ?.let {
                it
                    .lineSequence()
                    .filter { it.isNotBlank() }
                    .map { CommandLineArgument(it, CommandLineArgumentType.Custom) }
            }
            ?.toMutableList()
            ?: mutableListOf()

    private fun processFileArg(customArguments: MutableCollection<CommandLineArgument>, regex: Regex, prefix: String, extension: String): FileArg =
        tryFindArgumentValue(customArguments, regex)
            ?.let {
                customArguments.remove(it.arg)
                it.value
            }
            ?.trim('"', '\'')
            ?.let { _fileSystemService.createFile(it) }
            ?.let { when {
                !_fileSystemService.isAbsolute(it) -> _fileSystemService.createFile(_pathsService.getPath(PathType.Checkout), it.path)
                else -> it
            } }
            ?.let { FileArg(it, true) }
            ?: FileArg(_fileSystemService.generateTempFile(_pathsService.getPath(PathType.AgentTemp), prefix, extension), false)

    private fun tryFindArgumentValue(arguments: Collection<CommandLineArgument>, regex: Regex) =
        arguments.firstNotNullOfOrNull { arg ->
            regex
                .matchEntire(arg.value)
                ?.groupValues
                ?.get(2)
                ?.let { Arg(arg, it) }
        }

    private data class FileArg(val file: File, val custom: Boolean)

    private data class Arg(val arg: CommandLineArgument, val value: String)

    companion object {
        private val ConfigArgRegex = Regex("(--config)=(.+)", RegexOption.IGNORE_CASE)
        private val OutputArgRegex = Regex("(--output|[-/]o)=(.+)", RegexOption.IGNORE_CASE)
        private val LogArgRegex = Regex("(--logFile)=(.+)", RegexOption.IGNORE_CASE)
        private val CachesHomeArgRegex = Regex("(--caches-home)=(.+)", RegexOption.IGNORE_CASE)
    }
}
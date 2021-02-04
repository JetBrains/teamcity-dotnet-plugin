package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.CacheCleaner
import jetbrains.buildServer.agent.runner.CleanType
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.Logger
import java.io.File

class DotnetNugetCacheCleaner(
        private val command: String,
        override val name: String,
        override val type: CleanType,
        private val _toolProvider: ToolProvider,
        private val _pathsService: PathsService,
        private val _environmentVariables: EnvironmentVariables,
        private val _commandLineExecutor: CommandLineExecutor): CacheCleaner {

    private val _commandArg = CommandLineArgument(command)

    override val targets: Sequence<File>
        get() = sequence {
            runDotnet(NUGET_ARG, LOCALS_ARG, _commandArg, LIST_ARG)?.let {
                if (it.exitCode == 0) {
                    val pathPattern = Regex("^.*$command:\\s*(.+)\$", RegexOption.IGNORE_CASE)
                    it.standardOutput
                            .map { pathPattern.find(it)?.groups?.get(1)?.value }
                            .filter { it?.isNotBlank() ?: false }
                            .firstOrNull()
                            ?.let { yield(File(it)) }
                }
            }
        }

    override fun clean(target: File) = (runDotnet(NUGET_ARG, LOCALS_ARG, _commandArg, CLEAR_ARG)?.exitCode ?: -1)  == 0

    private fun runDotnet(vararg args: CommandLineArgument) =
            dotnet?.let {
                try {
                    _commandLineExecutor.tryExecute(
                            CommandLine(
                                    null,
                                    TargetType.SystemDiagnostics,
                                    it,
                                    Path(_pathsService.getPath(PathType.WorkingDirectory).path),
                                    args.toList(),
                                    _environmentVariables.getVariables(Version.Empty).toList())
                    )
                }
                catch (ex: Exception) {
                    LOG.debug(ex)
                    null
                }
            }

    private val dotnet: Path? get() {
        try {
            return Path(_toolProvider.getPath(DotnetConstants.EXECUTABLE))
        }
        catch (ex: ToolCannotBeFoundException) { }
        return null
    }

    companion object {
        internal val NUGET_ARG = CommandLineArgument("nuget")
        internal val LOCALS_ARG = CommandLineArgument("locals")
        internal val LIST_ARG = CommandLineArgument("--list")
        internal val CLEAR_ARG = CommandLineArgument("--clear")
        private val LOG = Logger.getLogger(DotnetNugetCacheCleaner::class.java)
    }
}
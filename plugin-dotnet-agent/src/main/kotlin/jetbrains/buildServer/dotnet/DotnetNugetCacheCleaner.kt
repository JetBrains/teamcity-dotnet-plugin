

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.CacheCleaner
import jetbrains.buildServer.agent.runner.CleanType
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_NUGET_CACHE_CLEAN_TIMEOUT
import java.io.File

class DotnetNugetCacheCleaner(
        private val command: String,
        override val name: String,
        override val type: CleanType,
        private val _toolProvider: ToolProvider,
        private val _pathsService: PathsService,
        private val _environmentVariables: EnvironmentVariables,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _parametersService: ParametersService,
): CacheCleaner {

    private val _commandArg = CommandLineArgument(command)

    private val _cleanTimeout
        get() = runCatching {
            _parametersService
                .tryGetParameter(ParameterType.Configuration, PARAM_NUGET_CACHE_CLEAN_TIMEOUT)
                .let { it?.trim()?.toInt() ?: DEFAULT_NUGET_CACHE_CLEAN_TIMEOUT_IN_SECONDS }
        }.getOrDefault(DEFAULT_NUGET_CACHE_CLEAN_TIMEOUT_IN_SECONDS)

    override val targets: Sequence<File>
        get() = sequence {
            runDotnet(
                NUGET_ARG,
                LOCALS_ARG,
                _commandArg,
                LIST_ARG,
                timeoutSeconds = DEFAULT_NUGET_CACHE_LIST_TIMEOUT_IN_SECONDS,
            )?.let {
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

    override fun clean(target: File) = (runDotnet(
        NUGET_ARG,
        LOCALS_ARG,
        _commandArg,
        CLEAR_ARG,
        timeoutSeconds = _cleanTimeout,
    )?.exitCode ?: -1)  == 0

    private fun runDotnet(vararg args: CommandLineArgument, timeoutSeconds: Int) =
            dotnet?.let {
                try {
                    _commandLineExecutor.tryExecute(
                            CommandLine(
                                    null,
                                    TargetType.SystemDiagnostics,
                                    it,
                                    Path(_pathsService.getPath(PathType.WorkingDirectory).path),
                                    args.toList(),
                                    _environmentVariables.getVariables(Version.Empty).toList(),
                            ),
                            executionTimeoutSeconds = timeoutSeconds,
                            // we use the same value for the idle timeout because the cache clean command
                            // might run for a long time without any output
                            idleTimeoutSeconds = timeoutSeconds,
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
        private const val DEFAULT_NUGET_CACHE_LIST_TIMEOUT_IN_SECONDS = 60
        private const val DEFAULT_NUGET_CACHE_CLEAN_TIMEOUT_IN_SECONDS = 600
        private val LOG = Logger.getLogger(DotnetNugetCacheCleaner::class.java)
    }
}
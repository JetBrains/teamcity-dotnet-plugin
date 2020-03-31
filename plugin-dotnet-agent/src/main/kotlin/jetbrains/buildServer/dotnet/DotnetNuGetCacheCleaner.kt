package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.CacheCleaner
import jetbrains.buildServer.agent.runner.CleanType
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class DotnetNuGetCacheCleaner(
        override val name: String,
        override val type: CleanType,
        private val _toolProvider: ToolProvider,
        private val _pathsService: PathsService,
        private val _environmentVariables: EnvironmentVariables,
        private val _commandLineExecutor: CommandLineExecutor): CacheCleaner {

    private val _commandArg = CommandLineArgument(name)

    override val targets: Sequence<File>
        get() = sequence {
            runDotnet(NUGET_ARG, LOCALS_ARG, _commandArg, LIST_ARG)?.let {
                if (it.exitCode == 0) {
                    val pathPattern = Regex("^.*$name:\\s*(?<path>.+)\$", RegexOption.IGNORE_CASE)
                    it.standardOutput
                            .map { pathPattern.find(it) }
                            .map { it?.groups?.get("path")?.value }
                            .filter { it?.isNotBlank() ?: false }
                            .firstOrNull()
                            ?.let { yield(File(it)) }
                }
            }
        }

    override fun clean(target: File) {
        runDotnet(NUGET_ARG, LOCALS_ARG, _commandArg, CLEAR_ARG)
    }

    private fun runDotnet(vararg args: CommandLineArgument) =
            dotnet?.let {
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
    }
}
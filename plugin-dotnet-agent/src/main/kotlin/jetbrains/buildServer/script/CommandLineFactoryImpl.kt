package jetbrains.buildServer.script

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.EnvironmentVariables

class CommandLineFactoryImpl(
        private val _pathsService: PathsService,
        private val _toolResolver: ToolResolver,
        private val _scriptProvider: ScriptResolver,
        private val _nugetEnvironmentVariables: EnvironmentVariables,
        private val _virtualContext: VirtualContext)
    : CommandLineFactory {

    override fun create() =
            CommandLine(
                null,
                TargetType.Tool,
                Path(""),
                Path(_virtualContext.resolvePath(_pathsService.getPath(PathType.WorkingDirectory).path)),
                listOf(
                        CommandLineArgument(_virtualContext.resolvePath(_toolResolver.resolve().path)),
                        CommandLineArgument(_virtualContext.resolvePath(_scriptProvider.resolve().path))
                ),
                _nugetEnvironmentVariables.getVariables(Version.Empty).toList()
            )
}
package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolEnvironment
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.DotnetConstants.DOTNET_DEFAULT_DIRECTORY
import jetbrains.buildServer.dotnet.DotnetConstants.TOOL_HOME
import jetbrains.buildServer.dotnet.DotnetConstants.PROGRAM_FILES_ENV_VAR
import jetbrains.buildServer.util.OSType

class DotnetToolEnvironment(
    private val _buildStepContext: BuildStepContext,
    private val _environment: Environment,
    private val _parametersService: ParametersService,
) : ToolEnvironment {

    override val homePaths: Sequence<Path> get() =
        when (_buildStepContext.isAvailable) {
            true -> _parametersService.tryGetParameter(ParameterType.Environment, TOOL_HOME)?.let { sequenceOf(Path(it)) } ?: emptySequence()
            false -> _environment.tryGetVariable(TOOL_HOME)?.let { sequenceOf(Path(it)) } ?: emptySequence()
        }

    override val defaultPaths: Sequence<Path> get() =
        when(_environment.os) {
            OSType.WINDOWS -> sequenceOf(
                _environment.tryGetVariable(PROGRAM_FILES_ENV_VAR)
                    ?.let { Path("$it\\${DOTNET_DEFAULT_DIRECTORY}") }
                    ?: Path("C:\\Program Files\\${DOTNET_DEFAULT_DIRECTORY}"),
            )
            OSType.UNIX -> sequenceOf(
                Path("/usr/share/${DOTNET_DEFAULT_DIRECTORY}"), // standard installation path
                Path("/usr/lib/${DOTNET_DEFAULT_DIRECTORY}"),   // on Ubuntu starting 22.04
            )
            OSType.MAC -> sequenceOf(
                Path("/usr/local/share/${DOTNET_DEFAULT_DIRECTORY}"),
            )
        }

    override val environmentPaths: Sequence<Path> get() = _environment.paths
}

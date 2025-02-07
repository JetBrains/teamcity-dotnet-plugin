package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolEnvironment
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.util.OSType

class DotnetToolEnvironment(
        private val _buildStepContext: BuildStepContext,
        private val _environment: Environment,
        private val _parametersService: ParametersService)
    : ToolEnvironment {

    override val homePaths get() = when(_buildStepContext.isAvailable) {
        false -> _environment.tryGetVariable(DotnetConstants.TOOL_HOME)?.let { sequenceOf(Path(it)) } ?: emptySequence()
        true -> _parametersService.tryGetParameter(ParameterType.Environment, DotnetConstants.TOOL_HOME)?.let { sequenceOf(Path(it)) } ?: emptySequence()
    }

    override val defaultPaths get() = when(_environment.os) {
        OSType.WINDOWS -> sequenceOf(
            _environment.tryGetVariable(DotnetConstants.PROGRAM_FILES_ENV_VAR)
                ?.let { Path("$it\\${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}") }
                ?: Path("C:\\Program Files\\${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}")
        )
        OSType.UNIX -> sequenceOf(
            Path("/usr/share/${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}"),
            Path("/usr/lib/${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}")
        )
        OSType.MAC -> sequenceOf(
            Path("/usr/local/share/${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}")
        )
    }

    override val environmentPaths get() = _environment.paths
}

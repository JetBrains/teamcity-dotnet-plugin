package jetbrains.buildServer.mono

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolEnvironment
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.MonoConstants
import java.io.File

class MonoToolEnvironment(
        private val _buildStepContext: BuildStepContext,
        private val _environment: Environment,
        private val _parametersService: ParametersService)
    : ToolEnvironment {

    private val _homePaths
        get() = when(_buildStepContext.isAvailable) {
            false -> _environment.tryGetVariable(MonoConstants.TOOL_HOME)?.let { sequenceOf(Path(it)) } ?: emptySequence()
            true -> _parametersService.tryGetParameter(ParameterType.Environment, MonoConstants.TOOL_HOME)?.let { sequenceOf(Path(it)) } ?: emptySequence()
        }

    override val homePaths: Sequence<Path>
        get() = extendByBin(_homePaths)

    override val defaultPaths: Sequence<Path>
        get() = emptySequence()

    override val environmentPaths: Sequence<Path>
        get() = extendByBin(_environment.paths)

    private fun extendByBin(paths: Sequence<Path>) = paths + paths.map { Path("${it.path}${File.separatorChar}bin") }
}
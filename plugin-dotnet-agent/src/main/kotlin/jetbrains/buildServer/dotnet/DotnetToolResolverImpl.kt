package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class DotnetToolResolverImpl(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService)
    : DotnetToolResolver {
    override val executableFile: File
        get() {
            try {
                return _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH)?.let {
                    File(it)
                } ?: _pathsService.getToolPath(DotnetConstants.RUNNER_TYPE)
            } catch (e: ToolCannotBeFoundException) {
                val exception = RunBuildException(e)
                exception.isLogStacktrace = false
                throw exception
            }
        }

    override val isCommandRequired: Boolean
        get() = true
}
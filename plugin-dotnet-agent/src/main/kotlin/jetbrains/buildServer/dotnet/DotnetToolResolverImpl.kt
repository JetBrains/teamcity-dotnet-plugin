package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class DotnetToolResolverImpl(private val _pathsService: PathsService)
    : DotnetToolResolver {
    override val executableFile: File
        get() {
            try {
                return _pathsService.getToolPath(DotnetConstants.RUNNER_TYPE)
            } catch (e: ToolCannotBeFoundException) {
                val exception = RunBuildException(e)
                exception.isLogStacktrace = false
                throw exception
            }
        }

    override val isCommandRequired: Boolean
        get() = true
}
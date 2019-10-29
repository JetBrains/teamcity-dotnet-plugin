package jetbrains.buildServer.mono

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.MonoConstants
import java.io.File

/**
 * Lookups for Mono.
 */
class MonoToolProvider(
        toolProvidersRegistry: ToolProvidersRegistry,
        private val _toolSearchService: ToolSearchService,
        private val _toolEnvironment: ToolEnvironment)
    : ToolProvider {
    init {
        toolProvidersRegistry.registerToolProvider(this)
    }

    override fun supports(toolName: String): Boolean = MonoConstants.RUNNER_TYPE.equals(toolName, ignoreCase = true)

    override fun getPath(toolName: String): String {
        return _toolSearchService.find(MonoConstants.RUNNER_TYPE, _toolEnvironment.homePaths + _toolEnvironment.defaultPaths + _toolEnvironment.environmentPaths)
                .firstOrNull()
                ?.canonicalPath
                ?: throw ToolCannotBeFoundException("""
                        Unable to locate tool $toolName in the system. Please make sure that `PATH` variable contains
                        Mono directory or defined `${MonoConstants.TOOL_HOME}` variable.""".trimIndent())
    }

    @Throws(ToolCannotBeFoundException::class)
    override fun getPath(toolName: String, build: AgentRunningBuild, runner: BuildRunnerContext): String {
        return if (runner.virtualContext.isVirtual) {
            MonoConstants.RUNNER_TYPE
        } else {
            getPath(toolName)
        }
    }
}
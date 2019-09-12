package jetbrains.buildServer.mono

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.MonoConstants
import java.io.File

/**
 * Lookups for Mono.
 */
class MonoToolProvider(
        toolProvidersRegistry: ToolProvidersRegistry,
        private val _toolSearchService: ToolSearchService)
    : ToolProvider {
    init {
        toolProvidersRegistry.registerToolProvider(this)
    }

    override fun supports(toolName: String): Boolean = MonoConstants.RUNNER_TYPE.equals(toolName, ignoreCase = true)

    override fun getPath(toolName: String): String =
            _toolSearchService.find(MonoConstants.RUNNER_TYPE, MonoConstants.TOOL_HOME, emptySequence())
                    .plus(_toolSearchService.find(MonoConstants.RUNNER_TYPE, MonoConstants.TOOL_HOME, emptySequence()) { File(it, "bin") })
                    .firstOrNull()
                    ?.absolutePath
                    ?: throw ToolCannotBeFoundException("""
                    Unable to locate tool $toolName in the system. Please make sure that `PATH` variable contains
                    Mono directory or defined `${MonoConstants.TOOL_HOME}` variable.""".trimIndent())

    @Throws(ToolCannotBeFoundException::class)
    override fun getPath(toolName: String, build: AgentRunningBuild, runner: BuildRunnerContext): String {
        return if (runner.virtualContext.isVirtual) {
            MonoConstants.RUNNER_TYPE
        } else {
            getPath(toolName)
        }
    }
}
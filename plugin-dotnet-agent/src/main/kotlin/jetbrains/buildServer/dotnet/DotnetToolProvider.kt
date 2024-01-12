

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdksProvider
import java.io.File

/**
 * Lookups for .NET CLI utilities.
 */
class DotnetToolProvider(
        toolProvidersRegistry: ToolProvidersRegistry,
        private val _toolSearchService: ToolSearchService,
        private val _toolEnvironment: ToolEnvironment,
        private val _dotnetSdksProviderImpl: DotnetSdksProvider
)
    : ToolProvider {
    init {
        toolProvidersRegistry.registerToolProvider(this)
    }

    override fun supports(toolName: String): Boolean = DotnetConstants.RUNNER_TYPE.equals(toolName, ignoreCase = true)

    override fun getPath(toolName: String): String =
            if(supports(toolName))
                executablePath
                ?.absolutePath
                ?: throw ToolCannotBeFoundException("""
                        Unable to locate tool $toolName in the system. Please make sure that `PATH` variable contains
                        .NET CLI toolchain directory or defined `${DotnetConstants.TOOL_HOME}` variable.""".trimIndent())
            else
                throw ToolCannotBeFoundException("Unsupported tool.")

    private val executablePath: File? by lazy {
        val executables = _toolSearchService.find(DotnetConstants.EXECUTABLE, _toolEnvironment.homePaths + _toolEnvironment.defaultPaths + _toolEnvironment.environmentPaths).toList()
        for (dotnetExecutable in executables) {
            if (_dotnetSdksProviderImpl.getSdks(dotnetExecutable).any()) {
                return@lazy dotnetExecutable
            }
            else {
                LOG.debug("Cannot find .NET Core SDK for <${dotnetExecutable}>.")
            }
        }

        return@lazy executables.firstOrNull()
    }

    @Throws(ToolCannotBeFoundException::class)
    override fun getPath(toolName: String, build: AgentRunningBuild, runner: BuildRunnerContext): String {
        return if (runner.virtualContext.isVirtual) {
            DotnetConstants.EXECUTABLE
        } else {
            getPath(toolName)
        }
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetToolProvider::class.java)
    }
}
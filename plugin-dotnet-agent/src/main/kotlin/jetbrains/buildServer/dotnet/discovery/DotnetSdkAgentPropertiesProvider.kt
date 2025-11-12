

package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdksProvider
import jetbrains.buildServer.dotnet.DotnetVersionProvider
import jetbrains.buildServer.dotnet.VersionEnumerator
import java.io.File

/**`
 * Provides a list of available .NET SDK.
 */

class DotnetSdkAgentPropertiesProvider(
    private val _toolProvider: ToolProvider,
    private val _dotnetVersionProvider: DotnetVersionProvider,
    private val _sdksProvider: DotnetSdksProvider,
    private val _pathsService: PathsService,
    private val _versionEnumerator: VersionEnumerator
)
    : AgentPropertiesProvider {

    override val description = ".NET SDK"

    override val properties: Sequence<AgentProperty>
        get() = sequence {
            // Detect .NET CLI path
            val dotnetPath = File(_toolProvider.getPath(DotnetConstants.EXECUTABLE))
            yield(AgentProperty(ToolInstanceType.DotNetCLI, DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, dotnetPath.canonicalPath))

            // Detect .NET CLI version
            val sdkVersion = _dotnetVersionProvider.getVersion(Path(dotnetPath.path), Path(_pathsService.getPath(PathType.Work).path))
            yield(AgentProperty(ToolInstanceType.DotNetCLI, DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI, sdkVersion.toString()))

            // Detect .NET SDKs
            for ((version, sdk) in _versionEnumerator.enumerate(_sdksProvider.getSdks(dotnetPath))) {
                val paramName = "${DotnetConstants.CONFIG_PREFIX_CORE_SDK}$version${DotnetConstants.CONFIG_SUFFIX_PATH}"
                yield(AgentProperty(ToolInstanceType.DotNetSDK, paramName, sdk.path.absolutePath))
            }
        }

    companion object {
        private val LOG = Logger.getLogger(DotnetSdkAgentPropertiesProvider::class.java)
    }
}
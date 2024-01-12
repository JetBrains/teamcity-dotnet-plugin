

package jetbrains.buildServer.dotnet.discovery.dotnetRuntime

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.Versioned
import java.io.File

data class DotnetRuntime(val path: File, override val version: Version, val runtimeType: String): Versioned
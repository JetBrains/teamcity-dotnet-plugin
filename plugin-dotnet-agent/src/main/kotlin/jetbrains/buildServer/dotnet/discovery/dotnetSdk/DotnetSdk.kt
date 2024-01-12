

package jetbrains.buildServer.dotnet.discovery.dotnetSdk

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.Versioned
import java.io.File

data class DotnetSdk(val path: File, override val version: Version): Versioned
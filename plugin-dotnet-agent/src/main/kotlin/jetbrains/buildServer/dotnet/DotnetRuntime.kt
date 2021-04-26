package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Version
import java.io.File

data class DotnetRuntime(val path: File, override val version: Version, val runtimeType: String): Versioned
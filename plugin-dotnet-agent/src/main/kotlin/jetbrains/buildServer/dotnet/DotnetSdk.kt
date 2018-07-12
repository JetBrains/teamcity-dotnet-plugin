package jetbrains.buildServer.dotnet

import java.io.File

data class DotnetSdk(val path: File, val version: Version)
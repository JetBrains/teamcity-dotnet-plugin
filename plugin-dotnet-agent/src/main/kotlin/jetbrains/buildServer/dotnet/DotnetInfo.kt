package jetbrains.buildServer.dotnet

data class DotnetInfo(val version: Version, val sdks: List<DotnetSdk>)
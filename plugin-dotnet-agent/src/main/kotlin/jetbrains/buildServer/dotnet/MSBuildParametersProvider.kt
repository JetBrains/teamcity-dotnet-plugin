package jetbrains.buildServer.dotnet

interface MSBuildParametersProvider {
    val parameters: Sequence<MSBuildParameter>
}
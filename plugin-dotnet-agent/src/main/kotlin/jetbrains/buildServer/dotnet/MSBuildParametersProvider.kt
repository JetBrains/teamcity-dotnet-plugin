package jetbrains.buildServer.dotnet

interface MSBuildParametersProvider {
    fun getParameters(context: DotnetBuildContext): Sequence<MSBuildParameter>
}
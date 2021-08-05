package jetbrains.buildServer.dotnet

interface MSBuildParameterConverter {
    fun convert(parameters: Sequence<MSBuildParameter>): Sequence<String>
}
package jetbrains.buildServer.dotnet

interface MSBuildParameterConverter {
    fun convert(parameters: Sequence<MSBuildParameter>, isCommandLineParameters: Boolean): Sequence<String>
}
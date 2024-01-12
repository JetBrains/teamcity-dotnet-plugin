

package jetbrains.buildServer.dotnet.commands.msbuild

interface MSBuildParameterConverter {
    fun convert(parameters: Sequence<MSBuildParameter>): Sequence<String>
}
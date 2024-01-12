

package jetbrains.buildServer.dotnet.commands.msbuild

import jetbrains.buildServer.dotnet.DotnetCommandContext

interface MSBuildParametersProvider {
    fun getParameters(context: DotnetCommandContext): Sequence<MSBuildParameter>
}


package jetbrains.buildServer.dotnet.commands.msbuild

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetCommandContext

class MSBuildArgumentsProvider(
    private val _msBuildParameterConverter: MSBuildParameterConverter,
    private val _msBuildParametersProviders: List<MSBuildParametersProvider>)
    : ArgumentsProvider {
    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> =
            _msBuildParametersProviders.asSequence().flatMap {
                _msBuildParameterConverter
                        .convert(it.getParameters(context))
                        .map { CommandLineArgument(it) }
            }

}
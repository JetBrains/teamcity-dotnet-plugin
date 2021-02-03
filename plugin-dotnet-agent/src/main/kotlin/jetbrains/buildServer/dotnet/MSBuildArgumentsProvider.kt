package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument

class MSBuildArgumentsProvider(
        private val _isCommandLineParameters: Boolean,
        private val _msBuildParameterConverter: MSBuildParameterConverter,
        private val _msBuildParametersProviders: List<MSBuildParametersProvider>)
    : ArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> =
            _msBuildParametersProviders.asSequence().flatMap {
                _msBuildParameterConverter
                        .convert(it.getParameters(context), _isCommandLineParameters)
                        .map { CommandLineArgument(it) }
            }

}
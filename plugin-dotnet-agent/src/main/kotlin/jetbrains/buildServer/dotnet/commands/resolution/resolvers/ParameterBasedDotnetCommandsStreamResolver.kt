package jetbrains.buildServer.dotnet.commands.resolution.resolvers

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandStreamResolverBase
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStreamResolvingStage

// Provides an initial command by build step parameter
class ParameterBasedDotnetCommandsStreamResolver(
    private val _dotnetCommands: List<DotnetCommand>,
    private val _parametersService: ParametersService,
) : DotnetCommandStreamResolverBase() {
    private val _allKnownCommands: Map<String, DotnetCommand> =
        _dotnetCommands.associateBy({ it.commandType.id }, { it })

    override val stage = DotnetCommandsStreamResolvingStage.CommandRetrieve

    override fun shouldBeApplied(commands: DotnetCommandsStream) = !commands.any()

    override fun apply(commands: DotnetCommandsStream) =
        _parametersService
            .tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND)
            ?.let { _allKnownCommands[it] }
            ?.let { sequenceOf(it) }
            ?: emptySequence<DotnetCommand>()
}


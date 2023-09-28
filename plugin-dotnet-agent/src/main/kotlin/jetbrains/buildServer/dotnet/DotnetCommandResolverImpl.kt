package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

// Provides an initial command by build step parameter
class DotnetCommandResolverImpl(
    private val _parametersService: ParametersService,
    commands: List<DotnetCommand>
) : DotnetCommandResolver {

    private val _knownCommands: Map<String, DotnetCommand> =
        commands.associateBy({ it.commandType.id }, { it })

    override val command: DotnetCommand?
        get() = _parametersService
            .tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND)
            ?.let { _knownCommands[it] }
}
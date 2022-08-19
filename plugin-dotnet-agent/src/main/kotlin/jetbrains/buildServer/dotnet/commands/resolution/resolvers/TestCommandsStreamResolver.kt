package jetbrains.buildServer.dotnet.commands.resolution.resolvers

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandStreamResolverBase
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStreamResolvingStage

// Transforms a `dotnet test` command to exact match filtered command if needed
class TestCommandsStreamResolver(
    private val _listTestsCommand: DotnetCommand,
    private val _parametersService: ParametersService,
    private val _splittedTestsFilterSettings: SplittedTestsFilterSettings,
) : DotnetCommandStreamResolverBase() {
    private val useExactMatchFilter: Boolean get() =
        _parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_TEST_USE_EXACT_MATCH_FILTER)
            ?.let { it.equals("true", true) }
            ?: false

    override val stage = DotnetCommandsStreamResolvingStage.Transformation

    override fun shouldBeApplied(commands: DotnetCommandsStream) =
        _splittedTestsFilterSettings.IsActive && useExactMatchFilter && commands.any { it is TestCommand }

    override fun apply(commands: DotnetCommandsStream) =
        commands
            .flatMap { command ->
                when {
                    command is TestCommand -> sequence<DotnetCommand> {
                        yield(_listTestsCommand)
                        yield(command)
                        yield(command)
                        yield(command)
                        // TODO make a proper move
                        // ...
                    }
                    else -> sequenceOf(command)
                }
            }
}
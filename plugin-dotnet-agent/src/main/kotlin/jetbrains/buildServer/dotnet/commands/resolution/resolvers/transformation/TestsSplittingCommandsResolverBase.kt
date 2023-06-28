package jetbrains.buildServer.dotnet.commands.resolution.resolvers.transformation

import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandResolverBase
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsResolvingStage
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettings

abstract class TestsSplittingCommandsResolverBase(
    private val _testsSplittingSettings: TestsSplittingSettings,
    private val _loggerService: LoggerService,
) : DotnetCommandResolverBase() {
    override val stage = DotnetCommandsResolvingStage.Transformation

    override fun shouldBeApplied(commands: DotnetCommandsStream) =
        _testsSplittingSettings.mode.isEnabled && commands.any { it.commandType == DotnetCommandType.Test }

    override fun apply(commands: DotnetCommandsStream) =
        commands
            .flatMap {
                when (it.commandType) {
                    DotnetCommandType.Test -> {
                        _loggerService.writeTrace(requirementsMessage)
                        transform(it)
                    }
                    else -> sequenceOf(it)
                }
            }

    protected abstract val requirementsMessage: String

    protected abstract fun transform(testCommand: DotnetCommand): Sequence<DotnetCommand>
}
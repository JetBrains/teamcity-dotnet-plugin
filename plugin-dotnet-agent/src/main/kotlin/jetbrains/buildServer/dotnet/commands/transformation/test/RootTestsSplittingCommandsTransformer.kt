package jetbrains.buildServer.dotnet.commands.transformation.test

import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingModeProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettings
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsTransformationStage
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsTransformer

class RootTestsSplittingCommandsTransformer(
    private val _loggerService: LoggerService,
    private val _testsSplittingSettings: TestsSplittingSettings,
    private val _testsSplittingModeProvider: TestsSplittingModeProvider,
    private val _testsSplittingCommandsTransformers: List<TestsSplittingCommandTransformer>
) : DotnetCommandsTransformer {
    override val stage = DotnetCommandsTransformationStage.Splitting
    override fun shouldBeApplied(context: DotnetCommandContext, commands: DotnetCommandsStream) =
        commands.any { it.commandType == DotnetCommandType.Test }
                && _testsSplittingSettings.testsClassesFilePath != null

    override fun apply(context: DotnetCommandContext, commands: DotnetCommandsStream): DotnetCommandsStream {
        val mode = _testsSplittingModeProvider.getMode(context.toolVersion)
        val testCommandTransformer = _testsSplittingCommandsTransformers
            .firstOrNull { it.mode == mode } ?: return commands
        _loggerService.writeTrace("$mode test split strategy was chosen")

        return commands
            .flatMap {
                when (it.commandType) {
                    DotnetCommandType.Test -> testCommandTransformer.transform(it)
                    else -> sequenceOf(it)
                }
            }
    }
}
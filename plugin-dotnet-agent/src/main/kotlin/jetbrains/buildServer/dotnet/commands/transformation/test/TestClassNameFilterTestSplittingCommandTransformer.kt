

package jetbrains.buildServer.dotnet.commands.transformation.test

import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode

class TestClassNameFilterTestSplittingCommandTransformer(private val _loggerService: LoggerService)
    : TestsSplittingCommandTransformer {
    override val mode = TestsSplittingMode.TestClassNameFilter

    override fun transform(testCommand: DotnetCommand) = sequence {
        _loggerService.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE)
        yield(testCommand)
    }
}
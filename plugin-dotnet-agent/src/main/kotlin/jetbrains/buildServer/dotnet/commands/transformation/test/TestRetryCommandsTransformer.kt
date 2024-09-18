package jetbrains.buildServer.dotnet.commands.transformation.test

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.serviceMessages.TestRetrySupportServiceMessage
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryReportReader
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryFilterProvider
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetrySettings
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsTransformationStage
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsTransformer
import jetbrains.buildServer.rx.*

class TestRetryCommandsTransformer(
    private val _loggerService: LoggerService,
    private val _testRetrySettings: TestRetrySettings,
    private val _testRetryFilter: TestRetryFilterProvider,
    private val _testRetryReportReader: TestRetryReportReader
) : DotnetCommandsTransformer {
    override val stage = DotnetCommandsTransformationStage.Retry

    override fun apply(context: DotnetCommandContext, commands: DotnetCommandsStream): DotnetCommandsStream {
        if (!_testRetrySettings.isEnabled) {
            return commands
        }

        return commands.flatMap {
            when (it.commandType) {
                DotnetCommandType.Test, DotnetCommandType.VSTest -> transform(it)
                else -> sequenceOf(it)
            }
        }
    }

    private fun transform(command: DotnetCommand) = sequence {
        _loggerService.writeMessage(TestRetrySupportServiceMessage(enabled = true))

        var retryNum = 0
        var failedTests: List<String> = emptyList()
        val retryEnvironmentBuilder = object : EnvironmentBuilder {
            override fun build(context: DotnetCommandContext): EnvironmentBuildResult {
                val filter = _testRetryFilter.setTestNames(failedTests)
                _testRetryReportReader.cleanup()

                return EnvironmentBuildResult(
                    variables = sequenceOf(
                        CommandLineEnvironmentVariable(
                            TESTS_RETRY_REPORTING_PATH_ENV_VAR,
                            _testRetrySettings.reportPath
                        )
                    ),
                    disposable = disposableOf {
                        filter.dispose()
                        failedTests = _testRetryReportReader.readFailedTestNames()
                        _testRetryReportReader.cleanup()
                    }
                )
            }
        }

        yield(InitialTestCommand(command, retryEnvironmentBuilder))

        while (shouldRetry(++retryNum, failedTests)) {
            yield(RetryTestCommand(command, retryEnvironmentBuilder, retryNum, failedTests))
        }
    }

    private fun shouldRetry(retryNum: Int, failedTests: List<String>): Boolean {
        if (failedTests.isEmpty()) {
            _loggerService.writeDebug("No tests to retry were found")
            return false
        }

        if (!_testRetrySettings.isEnabled) {
            _loggerService.writeDebug("Test retries are disabled")
            return false
        }

        if (retryNum > _testRetrySettings.maxRetries) {
            _loggerService.writeDebug("Test retry count is exceeded")
            return false
        }

        if (failedTests.size >= _testRetrySettings.maxFailures) {
            _loggerService.writeStandardOutput("Test retry was not performed as the number of failed test cases exceeded the specified limit of ${_testRetrySettings.maxFailures}")
            return false
        }

        _loggerService.writeDebug("Going to retry ${failedTests.size} tests")
        return true
    }

    private class InitialTestCommand(
        testCommand: DotnetCommand,
        retryEnvironmentBuilder: EnvironmentBuilder
    ) : DotnetCommand by testCommand {
        override val environmentBuilders: List<EnvironmentBuilder> =
            testCommand.environmentBuilders + retryEnvironmentBuilder
    }

    private class RetryTestCommand(
        testCommand: DotnetCommand,
        retryEnvironmentBuilder: EnvironmentBuilder,
        retryNum: Int,
        failedTests: List<String>
    ) : DotnetCommand by testCommand {

        override val title: String = "dotnet ${commandType.id} retry #$retryNum (${failedTests.size} tests)"
        override val environmentBuilders: List<EnvironmentBuilder> =
            testCommand.environmentBuilders + retryEnvironmentBuilder
    }

    private companion object {
        const val TESTS_RETRY_REPORTING_PATH_ENV_VAR = "TEAMCITY_FAILED_TESTS_REPORTING_PATH"
    }
}
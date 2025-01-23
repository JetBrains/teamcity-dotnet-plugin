

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.DotnetConstants.PARALLEL_TESTS_FEATURE_NAME
import jetbrains.buildServer.dotnet.DotnetConstants.TEST_CASE_FILTER_REQUIREMENTS_MESSAGE
import jetbrains.buildServer.dotnet.DotnetConstants.TEST_RETRY_FEATURE_NAME
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetArgumentsProvider
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.util.StringUtil

class VSTestCommand(
    _parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    private val _targetService: TargetService,
    private val _vstestLoggerArgumentsProvider: ArgumentsProvider,
    private val _customArgumentsProvider: ArgumentsProvider,
    override val toolResolver: ToolResolver,
    private val _dotnetFilterFactory: DotnetFilterFactory,
    private val _loggerService: LoggerService,
    private val _targetArgumentsProvider: TargetArgumentsProvider,
    override val environmentBuilders: List<EnvironmentBuilder>
) : DotnetCommandBase(_parametersService) {
    override val commandType = DotnetCommandType.VSTest

    override val command = sequenceOf("vstest")

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetArgumentsProvider.getTargetArguments(_targetService.targets)

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        val filter = _dotnetFilterFactory.createFilter(context);
        if (filter.isNotEmpty()) {
            _loggerService.writeStandardOutput(TEST_CASE_FILTER_REQUIREMENTS_MESSAGE)
        }

        if (filter.settingsFile != null) {
            yield(CommandLineArgument("/Settings:${filter.settingsFile.path}"))
        }
        else {
            parameters(DotnetConstants.PARAM_TEST_SETTINGS_FILE)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/Settings:$it"))
                }
            }
        }

        if (parameters(DotnetConstants.PARAM_TEST_FILTER) == "name") {
            if (filter.isNotEmpty()) {
                _loggerService.writeWarning("\"$PARALLEL_TESTS_FEATURE_NAME\" and \"$TEST_RETRY_FEATURE_NAME\" features are not supported together with a test names filter. Please consider using a test case filter.")
            }

            parameters(DotnetConstants.PARAM_TEST_NAMES)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/Tests:${StringUtil.split(it).joinToString(",")}"))
                }
            }
        }
        else {
            if (filter.filter.isNotBlank()) {
                yield(CommandLineArgument("/TestCaseFilter:${filter.filter}"))
            }
        }

        parameters(DotnetConstants.PARAM_PLATFORM)?.trim()?.let {
            if (it.isNotBlank()) {
                VsTestPlatform.tryParse(it)?.let { platform ->
                    if (platform != VsTestPlatform.Default) {
                        yield(CommandLineArgument("/Platform:${platform.id}"))
                    }
                }
            }
        }

        parameters(DotnetConstants.PARAM_FRAMEWORK)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("/Framework:$it"))
            }
        }

        if (parameters(DotnetConstants.PARAM_VSTEST_IN_ISOLATION, "").trim().toBoolean()) {
            yield(CommandLineArgument("/InIsolation"))
        }

        yieldAll(_vstestLoggerArgumentsProvider.getArguments(context))
        yieldAll(_customArgumentsProvider.getArguments(context))
    }
}
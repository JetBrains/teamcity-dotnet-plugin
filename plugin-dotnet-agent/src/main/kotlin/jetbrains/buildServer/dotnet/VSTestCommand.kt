package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.util.StringUtil
import kotlin.coroutines.experimental.buildSequence

class VSTestCommand(
        parametersService: ParametersService,
        private val _resultsAnalyzer: ResultsAnalyzer,
        private val _targetService: TargetService,
        private val _vstestLoggerArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider,
        private val _vstestToolResolver: ToolResolver)
    : DotnetCommandBase(parametersService) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.VSTest

    override val toolResolver: ToolResolver
        get() = _vstestToolResolver

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_TEST_SETTINGS_FILE)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/Settings:$it"))
                }
            }

            when(parameters(DotnetConstants.PARAM_TEST_FILTER)) {
                "filter" -> {
                    parameters(DotnetConstants.PARAM_TEST_CASE_FILTER)?.trim()?.let {
                        if (it.isNotBlank()) {
                            yield(CommandLineArgument("/TestCaseFilter:$it"))
                        }
                    }
                }
                "name" -> {
                    parameters(DotnetConstants.PARAM_TEST_NAMES)?.trim()?.let {
                        if (it.isNotBlank()) {
                            yield(CommandLineArgument("/Tests:${StringUtil.split(it).joinToString(",")}"))
                        }
                    }
                }
            }

            parameters(DotnetConstants.PARAM_PLATFORM)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/Platform:$it"))
                }
            }

            parameters(DotnetConstants.PARAM_FRAMEWORK)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/Framework:$it"))
                }
            }

            yieldAll(_vstestLoggerArgumentsProvider.arguments)
            yieldAll(_customArgumentsProvider.arguments)
        }

    override fun isSuccessful(result: CommandLineResult) =
            _resultsAnalyzer.isSuccessful(result)
}
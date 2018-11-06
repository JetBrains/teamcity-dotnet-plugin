package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.util.StringUtil

class VSTestCommand(
        _parametersService: ParametersService,
        override val resultsAnalyzer: ResultsAnalyzer,
        private val _targetService: TargetService,
        private val _vstestLoggerArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider,
        override val toolResolver: ToolResolver)
    : DotnetCommandBase(_parametersService) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.VSTest

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        parameters(DotnetConstants.PARAM_TEST_SETTINGS_FILE)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("/Settings:$it"))
            }
        }

        when (parameters(DotnetConstants.PARAM_TEST_FILTER)) {
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

        yieldAll(_vstestLoggerArgumentsProvider.getArguments(context))
        yieldAll(_customArgumentsProvider.getArguments(context))
    }
}
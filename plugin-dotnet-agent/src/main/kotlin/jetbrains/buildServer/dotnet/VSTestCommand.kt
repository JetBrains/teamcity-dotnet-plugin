package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class VSTestCommand(
        private val _parametersService: ParametersService,
        private val _targetService: TargetService,
        private val _vstestLoggerArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider,
        private val _vstestToolResolver: ToolResolver)
    : DotnetCommand {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.VSTest

    override val toolResolver: ToolResolver
        get() = _vstestToolResolver

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_VSTEST_CONFIG_FILE)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/Settings:$it"))
                }
            }

            parameters(DotnetConstants.PARAM_VSTEST_TEST_NAMES)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/Tests:$it"))
                }
            }

            parameters(DotnetConstants.PARAM_VSTEST_IN_ISOLATION)?.trim()?.let {
                if (it.isNotBlank() && "true".equals(it, true)) {
                    yield(CommandLineArgument("/InIsolation"))
                }
            }

            parameters(DotnetConstants.PARAM_VSTEST_PLATFORM)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/Platform:$it"))
                }
            }

            parameters(DotnetConstants.PARAM_VSTEST_FRAMEWORK)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/Framework:$it"))
                }
            }

            parameters(DotnetConstants.PARAM_VSTEST_TEST_CASE_FILTER)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/TestCaseFilter:$it"))
                }
            }

            yieldAll(_vstestLoggerArgumentsProvider.arguments)
            yieldAll(_customArgumentsProvider.arguments)
        }

    override fun isSuccessfulExitCode(exitCode: Int): Boolean = exitCode >= 0

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}
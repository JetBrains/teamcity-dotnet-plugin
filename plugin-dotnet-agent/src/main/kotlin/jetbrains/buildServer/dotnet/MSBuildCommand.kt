package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.util.StringUtil
import kotlin.coroutines.experimental.buildSequence

class MSBuildCommand(
        parametersService: ParametersService,
        private val _failedTestDetector: FailedTestDetector,
        private val _targetService: TargetService,
        private val _msbuildResponseFileArgumentsProvider: ArgumentsProvider,
        private val _msbuildToolResolver: ToolResolver)
    : DotnetCommandBase(parametersService) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.MSBuild

    override val toolResolver: ToolResolver
        get() = _msbuildToolResolver

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_TARGETS)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/t:${StringUtil.split(it).joinToString(";")}"))
                }
            }

            parameters(DotnetConstants.PARAM_CONFIG)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/p:Configuration=$it"))
                }
            }

            parameters(DotnetConstants.PARAM_PLATFORM)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/p:Platform=$it"))
                }
            }

            parameters(DotnetConstants.PARAM_RUNTIME)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/p:RuntimeIdentifiers=$it"))
                }
            }

            parameters(DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
                Verbosity.tryParse(it)?.let {
                    yield(CommandLineArgument("/v:${it.id}"))
                }
            }

            yieldAll(_msbuildResponseFileArgumentsProvider.arguments)
        }

    override fun isSuccessful(result: CommandLineResult) =
            result.exitCode == 0 || (result.exitCode > 0 && result.standardOutput.map { _failedTestDetector.hasFailedTest(it) }.filter { it }.any())
}
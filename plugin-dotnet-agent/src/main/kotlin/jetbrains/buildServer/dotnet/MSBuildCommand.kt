package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

class MSBuildCommand(
        parametersService: ParametersService,
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
            parameters(DotnetConstants.PARAM_MSBUILD_TARGETS)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/t:$it"))
                }
            }

            parameters(DotnetConstants.PARAM_MSBUILD_CONFIG)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/p:Configuration=$it"))
                }
            }

            parameters(DotnetConstants.PARAM_MSBUILD_PLATFORM)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/p:Platform=$it"))
                }
            }

            parameters(DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
                Verbosity.tryParse(it)?.let {
                    yield(CommandLineArgument("/v:${it.id}"))
                }
            }

            yieldAll(_msbuildResponseFileArgumentsProvider.arguments)
        }

    override fun isSuccessfulExitCode(exitCode: Int): Boolean = exitCode >= 0
}
@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.util.StringUtil
import kotlin.coroutines.experimental.buildSequence

class MSBuildCommand(
        _parametersService: ParametersService,
        override val resultsAnalyzer: ResultsAnalyzer,
        private val _targetService: TargetService,
        private val _msBuildResponseFileArgumentsProvider: ArgumentsProvider,
        override val toolResolver: ToolResolver,
        private val _vstestLoggerEnvironment: EnvironmentBuilder)
    : DotnetCommandBase(_parametersService) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.MSBuild

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = buildSequence {
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

        context.verbosityLevel?.let {
            yield(CommandLineArgument("/v:${it.id.toLowerCase()}"))
        }

        yieldAll(_msBuildResponseFileArgumentsProvider.getArguments(context))
    }

    override val environmentBuilders: Sequence<EnvironmentBuilder>
        get() = buildSequence { yield(_vstestLoggerEnvironment) }
}
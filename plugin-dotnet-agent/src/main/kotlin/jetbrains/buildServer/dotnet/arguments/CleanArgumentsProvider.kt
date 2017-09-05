package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet clean command.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class CleanArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _projectService: TargetService)
    : DotnetCommandArgumentsProvider {

    override val command: DotnetCommand
        get() = DotnetCommand.Clean

    override val targetArguments: Sequence<TargetArguments>
        get() = _projectService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override fun getArguments(): Sequence<CommandLineArgument> = buildSequence {
        parameters(DotnetConstants.PARAM_CLEAN_FRAMEWORK)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--framework"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_CLEAN_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--configuration"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_CLEAN_RUNTIME)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--runtime"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_CLEAN_OUTPUT)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--output"))
                yield(CommandLineArgument(it))
            }
        }
    }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}
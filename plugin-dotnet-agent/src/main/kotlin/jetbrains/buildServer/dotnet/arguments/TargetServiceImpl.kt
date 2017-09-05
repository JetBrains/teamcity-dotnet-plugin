package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.runners.ArgumentsService
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import java.io.File
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class TargetServiceImpl(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService)
    : TargetService {
    override val targets: Sequence<CommandTarget>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
                yieldAll(_argumentsService.split(it).map { CommandTarget(File(it)) });
            }
        }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}
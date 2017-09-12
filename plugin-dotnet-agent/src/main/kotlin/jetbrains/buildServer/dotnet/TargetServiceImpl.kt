package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.PathMatcher
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class TargetServiceImpl(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _pathMatcher: PathMatcher)
    : TargetService {
    override val targets: Sequence<CommandTarget>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
                val checkoutDirectory = _pathsService.getPath(PathType.Checkout)
                val includeRules = _argumentsService.split(it);
                yieldAll(_pathMatcher.match(checkoutDirectory, includeRules, emptySequence()).map { CommandTarget(it) })
            }
        }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}
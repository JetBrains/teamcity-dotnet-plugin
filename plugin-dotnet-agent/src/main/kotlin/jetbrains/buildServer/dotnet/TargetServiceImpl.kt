package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.PathMatcher
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import kotlin.coroutines.experimental.buildSequence

class TargetServiceImpl(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _pathMatcher: PathMatcher)
    : TargetService {
    override val targets: Sequence<CommandTarget>
        get() = buildSequence {
            _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_PATHS)?.trim()?.let {
                val checkoutDirectory = _pathsService.getPath(PathType.Checkout)
                val includeRulesStr = it.trim()
                if (includeRulesStr.isEmpty()) {
                    return@buildSequence
                }

                val includeRules = _argumentsService.split(includeRulesStr)
                var hasAnyTarget = false
                for(target in _pathMatcher.match(checkoutDirectory, includeRules, emptySequence())) {
                    yield(CommandTarget(target))
                    hasAnyTarget = true
                }

                if (!hasAnyTarget) {
                    throw RunBuildException("Target files were not found for \"$it\"")
                }
            }
        }
}
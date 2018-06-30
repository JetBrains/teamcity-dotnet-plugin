@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.PathMatcher
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File
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

                // We need to resolve paths in the specified sequence where
                // include rules may be mix of regular and wildcard paths
                _argumentsService.split(includeRulesStr).forEach {
                    if (wildCardPattern.matches(it)) {
                        val targets = _pathMatcher.match(checkoutDirectory, listOf(it))
                        if (targets.isEmpty()) {
                            throw RunBuildException("Target files not found for pattern \"$it\"")
                        }

                        targets.forEach { yield(CommandTarget(it)) }
                    } else {
                        yield(CommandTarget(File(it)))
                    }
                }
            }
        }

    companion object {
        val wildCardPattern = Regex(".*[?*].*")
    }
}
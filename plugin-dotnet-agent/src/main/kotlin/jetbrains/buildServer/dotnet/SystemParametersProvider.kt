@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

class SystemParametersProvider(
        private val _parametersService: ParametersService)
    : MSBuildParametersProvider {
    override fun getParameters(context: DotnetBuildContext): Sequence<MSBuildParameter> = buildSequence {
        for (paramName in _parametersService.getParameterNames(ParameterType.System)) {
            _parametersService.tryGetParameter(ParameterType.System, paramName)?.let {
                yield(MSBuildParameter(paramName, it))
            }
        }
    }
}
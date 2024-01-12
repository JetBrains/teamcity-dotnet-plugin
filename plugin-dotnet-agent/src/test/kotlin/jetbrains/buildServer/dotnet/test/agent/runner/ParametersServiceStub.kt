

package jetbrains.buildServer.dotnet.test.agent.runner

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class ParametersServiceStub(
        private val _parameters: Map<String, String>) : ParametersService {
    override fun tryGetParameter(parameterType: ParameterType, parameterName: String): String? = _parameters[parameterName]

    override fun getParameterNames(parameterType: ParameterType): Sequence<String> = _parameters.keys.asSequence()
}
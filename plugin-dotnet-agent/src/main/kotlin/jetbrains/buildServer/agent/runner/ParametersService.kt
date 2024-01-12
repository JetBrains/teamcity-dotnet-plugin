

package jetbrains.buildServer.agent.runner

interface ParametersService {
    fun tryGetParameter(parameterType: ParameterType, parameterName: String): String?

    fun getParameterNames(parameterType: ParameterType): Sequence<String>
}
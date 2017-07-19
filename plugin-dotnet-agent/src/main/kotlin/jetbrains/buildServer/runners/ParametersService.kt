package jetbrains.buildServer.runners

interface ParametersService {
    fun tryGetParameter(parameterType: ParameterType, parameterName: String): String?
}
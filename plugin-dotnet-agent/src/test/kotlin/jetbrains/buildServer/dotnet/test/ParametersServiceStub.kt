package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService

class ParametersServiceStub(
        private val _parameters: Map<String, String>): ParametersService {
    override fun tryGetParameter(parameterType: ParameterType, parameterName: String): String? {
        return _parameters[parameterName];
    }

}
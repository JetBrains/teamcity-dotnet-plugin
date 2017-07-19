package jetbrains.buildServer.runners

import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService

class ParametersServiceImpl(
        private val _buildStepContext: BuildStepContext) : ParametersService {

    override fun tryGetParameter(parameterType: ParameterType, parameterName: String): String? {
        when(parameterType) {
            ParameterType.Runner -> return _buildStepContext.runnerContext.runnerParameters[parameterName]
            ParameterType.Configuration -> return _buildStepContext.runnerContext.configParameters[parameterName]
            ParameterType.Environment -> return _buildStepContext.runnerContext.getBuildParameters().getEnvironmentVariables()[parameterName]
            else -> throw UnsupportedOperationException("Unknown parameterType: $parameterType")
        }
    }
}
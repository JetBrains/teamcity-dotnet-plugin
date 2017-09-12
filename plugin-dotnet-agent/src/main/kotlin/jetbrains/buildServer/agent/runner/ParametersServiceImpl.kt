package jetbrains.buildServer.agent.runner

class ParametersServiceImpl(
        private val _buildStepContext: BuildStepContext) : ParametersService {

    override fun tryGetParameter(parameterType: ParameterType, parameterName: String): String? {
        when(parameterType) {
            ParameterType.Runner -> return _buildStepContext.runnerContext.runnerParameters[parameterName]
            ParameterType.Configuration -> return _buildStepContext.runnerContext.configParameters[parameterName]
            ParameterType.Environment -> return _buildStepContext.runnerContext.getBuildParameters().environmentVariables[parameterName]
            else -> throw UnsupportedOperationException("Unknown parameterType: $parameterType")
        }
    }
}
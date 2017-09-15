package jetbrains.buildServer.agent.runner

class ParametersServiceImpl(
        private val _buildStepContext: BuildStepContext) : ParametersService {

    override fun tryGetParameter(parameterType: ParameterType, parameterName: String): String? {
        when(parameterType) {
            ParameterType.Runner -> return _buildStepContext.runnerContext.runnerParameters[parameterName]
            ParameterType.Configuration -> return _buildStepContext.runnerContext.configParameters[parameterName]
            ParameterType.Environment -> return _buildStepContext.runnerContext.getBuildParameters().environmentVariables[parameterName]
            ParameterType.System -> return _buildStepContext.runnerContext.getBuildParameters().systemProperties[parameterName]
            else -> throw UnsupportedOperationException("Unknown parameterType: $parameterType")
        }
    }

    override fun getParameterNames(parameterType: ParameterType): Sequence<String> {
        when(parameterType) {
            ParameterType.Runner -> return getNames(_buildStepContext.runnerContext.runnerParameters)
            ParameterType.Configuration -> return getNames(_buildStepContext.runnerContext.configParameters)
            ParameterType.Environment -> return getNames(_buildStepContext.runnerContext.getBuildParameters().environmentVariables)
            ParameterType.System -> return getNames(_buildStepContext.runnerContext.getBuildParameters().systemProperties)
            else -> throw UnsupportedOperationException("Unknown parameterType: $parameterType")
        }
    }

    private fun getNames(map: Map<String?, String?>): Sequence<String> =
            map.keys.filter { it != null }.map { it as String }.asSequence()
}
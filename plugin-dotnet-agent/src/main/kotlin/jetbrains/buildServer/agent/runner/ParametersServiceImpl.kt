

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.serverSide.TeamCityProperties

class ParametersServiceImpl(private val _buildStepContext: BuildStepContext) : ParametersService {
    override fun tryGetParameter(parameterType: ParameterType, parameterName: String) = when (parameterType) {
        ParameterType.Runner -> _buildStepContext.runnerContext.runnerParameters[parameterName]
        ParameterType.Configuration -> _buildStepContext.runnerContext.configParameters[parameterName]
        ParameterType.Environment -> _buildStepContext.runnerContext.buildParameters.environmentVariables[parameterName]
        ParameterType.System -> _buildStepContext.runnerContext.buildParameters.systemProperties[parameterName]
        ParameterType.Internal -> TeamCityProperties.getProperty(parameterName)
    }

    override fun getParameterNames(parameterType: ParameterType) = when (parameterType) {
        ParameterType.Runner -> getNames(_buildStepContext.runnerContext.runnerParameters)
        ParameterType.Configuration -> getNames(_buildStepContext.runnerContext.configParameters)
        ParameterType.Environment -> getNames(_buildStepContext.runnerContext.buildParameters.environmentVariables)
        ParameterType.System -> getNames(_buildStepContext.runnerContext.buildParameters.systemProperties)
        ParameterType.Internal -> (
                getNames(TeamCityProperties.getModel().getUserDefinedProperties()) +
                        getNames(TeamCityProperties.getModel().getSystemProperties())).distinct()
    }

    private fun getNames(map: Map<String?, String?>): Sequence<String> =
            map.keys.mapNotNull { it }.asSequence()
}
package jetbrains.buildServer.dotnet.coverage.serviceMessage

import jetbrains.buildServer.agent.BuildRunnerContext
import java.util.TreeMap
import java.util.concurrent.ConcurrentHashMap

class DotnetCoverageParametersImpl(private val _runnerContext: BuildRunnerContext) :
    DotnetCoverageParametersBase(_runnerContext.build) {

    private val myAdditionalRunnerParameters: MutableMap<String, String> = ConcurrentHashMap()

    override fun makeSnapshot(): DotnetCoverageParameters {
        val params: MutableMap<String, String> = TreeMap()
        params.putAll(_runnerContext.runnerParameters)
        params.putAll(myAdditionalRunnerParameters)

        return object : DotnetCoverageParametersBase(runningBuild) {
            override fun getRunnerParameter(key: String): String? {
                synchronized(params) { return params[key] }
            }

            override fun makeSnapshot(): DotnetCoverageParameters {
                return this
            }
        }
    }

    override fun getRunnerParameter(key: String): String? {
        var value = myAdditionalRunnerParameters[key]
        if (value != null) return value

        value = _runnerContext.runnerParameters[key]

        return value
    }

    //Returns old value
    fun setAdditionalRunnerParameter(key: String, value: String): String? {
        val oldValue = getRunnerParameter(key)
        val dynValue = myAdditionalRunnerParameters.put(key, value)

        return dynValue ?: oldValue
    }

    fun copyAdditionalParameters(parameters: DotnetCoverageParametersImpl) {
        myAdditionalRunnerParameters.putAll(parameters.myAdditionalRunnerParameters)
    }
}

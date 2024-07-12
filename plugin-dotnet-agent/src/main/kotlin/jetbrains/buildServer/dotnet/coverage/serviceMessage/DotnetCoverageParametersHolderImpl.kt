package jetbrains.buildServer.dotnet.coverage.serviceMessage

import jetbrains.buildServer.TeamCityRuntimeException
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.coverage.agent.serviceMessage.CoverageServiceMessageSetup
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessagesRegister
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.positioning.PositionAware
import java.util.concurrent.ConcurrentHashMap

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
class DotnetCoverageParametersHolderImpl(
    reg: ServiceMessagesRegister,
    events: EventDispatcher<AgentLifeCycleListener?>
) : CoverageServiceMessageSetup, DotnetCoverageParametersHolder {

    @Volatile
    private var _coverageParameters: DotnetCoverageParametersImpl? = null
    private val _keyTranslation: MutableMap<String, String> = ConcurrentHashMap()

    init {
        reg.registerHandler(CoverageConstants.COVERAGE_TYPE) {
            serviceMessage -> serviceMessageReceived(serviceMessage)
        }
        events.addListener(ParametersAgentLifeCycleAdapter())
    }

    override fun addPropertyMapping(serviceMessageKey: String, runnerParameter: String) {
        _keyTranslation[serviceMessageKey] = runnerParameter
    }

    override fun getCoverageParameters(): DotnetCoverageParameters {
        return _coverageParameters ?: throw TeamCityRuntimeException("No running build")
    }

    private fun serviceMessageReceived(serviceMessage: ServiceMessage) {
        val ps: DotnetCoverageParametersImpl = _coverageParameters ?: return
        val map = serviceMessage.attributes
        for ((key, value) in map) {
            if (!StringUtil.isEmptyOrSpaces(key) && !StringUtil.isEmptyOrSpaces(value)) {
                val actualKey = _keyTranslation[key]
                if (actualKey == null) {
                    ps.getBuildLogger().warning("Key '$key' is not supported by .NET Coverage. Ignored.")
                    continue
                }
                val oldValue: String? = ps.setAdditionalRunnerParameter(actualKey, value)
                if (oldValue != null && oldValue != value) {
                    ps.getBuildLogger().warning("Key '$key' value '$oldValue' was overridden with '$value'")
                }
            }
        }
    }

    private inner class ParametersAgentLifeCycleAdapter : AgentLifeCycleAdapter(), PositionAware {
        @Synchronized
        override fun buildFinished(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
            _coverageParameters = null
        }

        override fun getOrderId(): String {
            return DotnetCoverageParametersHolder.AGENT_LISTENER_ID
        }

        @Synchronized
        override fun beforeRunnerStart(runner: BuildRunnerContext) {
            val cp: DotnetCoverageParametersImpl? = _coverageParameters
            _coverageParameters = DotnetCoverageParametersImpl(runner)
            if (cp != null) {
                _coverageParameters!!.copyAdditionalParameters(cp)
            }
        }
    }
}

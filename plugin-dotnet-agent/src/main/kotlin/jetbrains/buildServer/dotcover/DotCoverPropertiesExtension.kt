package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleEventSources
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_CROSS_PLATFORM_REQUIREMENT
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.util.positioning.PositionAware
import jetbrains.buildServer.util.positioning.PositionConstraint

class DotCoverPropertiesExtension(
        agentLifeCycleEventSources: AgentLifeCycleEventSources,
        environment: Environment)
    : AgentLifeCycleAdapter(), PositionAware {

    private var _subscriptionToken: Disposable

    init {
        _subscriptionToken = agentLifeCycleEventSources.beforeAgentConfigurationLoadedSource.subscribe { event ->
            val configuration = event.agent.configuration
            if (environment.os != OSType.WINDOWS) {
                configuration.addConfigurationParameter(DOTCOVER_CROSS_PLATFORM_REQUIREMENT, environment.os.toString())
            }
            else {
                val dotnetKey = configuration.configurationParameters.keys.firstOrNull { dotnetRegex.matches(it) }
                if (dotnetKey != null) {
                    configuration.addConfigurationParameter("${DOTCOVER_CROSS_PLATFORM_REQUIREMENT}_${dotnetKey}", environment.os.toString())
                }
            }
        }
    }

    override fun getOrderId(): String = ""
    override fun getConstraint(): PositionConstraint = PositionConstraint.last()

    companion object {
        val dotnetRegex = Regex(CoverageConstants.DOTNET_FRAMEWORK_PATTERN_4_6_1)
    }
}
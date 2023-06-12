package jetbrains.buildServer.dotcover.report

import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.config.AgentParametersSupplier
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParametersHolder
import jetbrains.buildServer.tools.ToolVersionReference
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.positioning.PositionAware
import jetbrains.buildServer.util.positioning.PositionConstraint
import java.io.File

class DotCoverPropertiesExtension(private val _bundle: BundledDotCover,
                                  listener: EventDispatcher<AgentLifeCycleListener?>,
                                  extensionHolder: ExtensionHolder) :
    AgentLifeCycleAdapter(), PositionAware, AgentParametersSupplier {

    init {
        listener.addListener(this)
        extensionHolder.registerExtension(AgentParametersSupplier::class.java, javaClass.name, this)
    }

    override fun getOrderId(): String {
        return ORDER_ID
    }

    override fun getConstraint(): PositionConstraint {
        return PositionConstraint.before(DotnetCoverageParametersHolder.AGENT_LISTENER_ID)
    }

    override fun beforeRunnerStart(runner: BuildRunnerContext) {
        val home = runner.runnerParameters[CoverageConstants.PARAM_DOTCOVER_HOME]
        if (home != null) return
        val file: File = _bundle.dotCoverPath ?: return
        runner.addRunnerParameter(CoverageConstants.PARAM_DOTCOVER_HOME, file.path)
    }

    override fun getParameters(): Map<String, String> {
        val parameters: MutableMap<String, String> = HashMap()
        val bundledToolName = ToolVersionReference.TOOL_PARAMETER_PREFIX + CoverageConstants.DOTCOVER_BUNDLED_TOOL_ID

        val dotCoverPath: File? = _bundle.dotCoverPath
        dotCoverPath?.let {
            val dotCoverAbsolutePath = dotCoverPath.absolutePath
            parameters[bundledToolName] = dotCoverAbsolutePath
        }

        val toolRef = "%${bundledToolName}%"
        parameters[CoverageConstants.TEAMCITY_DOTCOVER_HOME] = toolRef
        parameters[ToolVersionReference.TOOL_PARAMETER_PREFIX + "dotCover"] = toolRef

        return parameters
    }

    companion object {
        private const val ORDER_ID = "dotNetCoverageDotnetRunner.dotCoverBundledPathSetter"
    }
}

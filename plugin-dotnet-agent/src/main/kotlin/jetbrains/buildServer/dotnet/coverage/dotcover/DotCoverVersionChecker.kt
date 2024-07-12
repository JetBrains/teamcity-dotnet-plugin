package jetbrains.buildServer.dotnet.coverage.dotcover

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParametersHolder
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.positioning.PositionAware
import jetbrains.buildServer.util.positioning.PositionConstraint
import java.io.IOException

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
class DotCoverVersionChecker(
    events: EventDispatcher<AgentLifeCycleListener?>,
    private val _holder: DotnetCoverageParametersHolder,
    private val _versionFetcher: DotCoverVersionFetcher,
    private val _runnerFactory: DotCoverReportRunnerFactory
) : AgentLifeCycleAdapter(), PositionAware {

    init {
        events.addListener(this)
    }

    override fun getOrderId(): String {
        return ORDER_ID
    }

    override fun getConstraint(): PositionConstraint {
        return PositionConstraint.after(DotnetCoverageParametersHolder.AGENT_LISTENER_ID)
    }

    override fun beforeRunnerStart(ctx: BuildRunnerContext) {
        val ps: DotnetCoverageParameters = _holder.getCoverageParameters()
        if (!CoverageConstants.PARAM_DOTCOVER.equals(ps.getCoverageToolName(), ignoreCase = true)) return
        if (StringUtil.isEmptyOrSpaces(ps.getRunnerParameter(CoverageConstants.PARAM_DOTCOVER_ATTRIBUTE_FILTERS))) return

        val runner = _runnerFactory.getDotCoverReporter(ps) ?: return
        try {
            val versionStr = _versionFetcher.getDotCoverVersionString(ps, runner)
            val version = _versionFetcher.getDotCoverVersion(versionStr, ps)
            if (version.isOlder(DotCoverVersion.DotCover_2_0)) {
                ctx.build.buildLogger.warning("Attribute assembly filter is not supported by specified dotCover.")
                ctx.addRunnerParameter(CoverageConstants.PARAM_DOTCOVER_ATTRIBUTE_FILTERS, "")
            }
        } catch (e: IOException) {
            LOG.warn("Failed to get version of dotCover")
        }
    }

    companion object {
        private val LOG = Logger.getInstance(DotCoverVersionChecker::class.java.name)
        private const val ORDER_ID = "dotNetCoverageDotnetRunner.dotCoverVersionChecker"
    }
}

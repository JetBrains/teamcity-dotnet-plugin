package jetbrains.buildServer.dotnet.coverage.serviceMessage

import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.coverage.utils.PathUtil
import java.io.File

abstract class DotnetCoverageParametersBase(protected val runningBuild: AgentRunningBuild) : DotnetCoverageParameters {

    abstract override fun getRunnerParameter(key: String): String?

    override fun getConfigurationParameter(key: String): String? {
        return runningBuild.sharedConfigParameters[key]
    }

    override fun getConfigurationParameters(): Map<String, String> {
        return runningBuild.sharedConfigParameters
    }

    override fun getBuildEnvironmentVariables(): Map<String, String> {
        return runningBuild.sharedBuildParameters.environmentVariables
    }

    override fun getCoverageToolName(): String? {
        return getRunnerParameter(CoverageConstants.PARAM_TYPE)
    }

    override fun getCheckoutDirectory(): File {
        return runningBuild.checkoutDirectory
    }

    override fun getBuildName(): String {
        return runningBuild.projectName + " / " + runningBuild.buildTypeName
    }

    override fun getTempDirectory(): File {
        val temDirectory = File(runningBuild.agentTempDirectory, "dotNetCoverageResults")
        if (temDirectory.exists() && temDirectory.isFile) {
            temDirectory.delete()
        }
        if (!temDirectory.exists()) {
            temDirectory.mkdirs()
        }
        return temDirectory
    }

    override fun resolvePath(path: String): File? {
        return PathUtil.resolvePath(runningBuild, path)
    }

    override fun resolvePathToTool(path: String, toolName: String): File? {
        return PathUtil.resolvePathToTool(runningBuild, path, toolName)
    }

    override fun getBuildLogger(): BuildProgressLogger {
        return runningBuild.buildLogger
    }
}

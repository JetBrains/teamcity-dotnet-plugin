package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetConstants
import java.nio.file.Path
import java.nio.file.Paths

object TestReportingViaFileStreamingHelper {
    fun shouldFallbackToStdOutTestReporting(parametersService: ParametersService): Boolean {
        val fallbackToStdOutTestReportingValue = parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING)
        return fallbackToStdOutTestReportingValue.toBoolean()
    }

    fun getTestReportsFilesPath(pathsService: PathsService): Path {
        val agentTempPath = pathsService.getPath(PathType.AgentTemp).canonicalPath
        return Paths.get(agentTempPath, "TestReports").toAbsolutePath()
    }
}

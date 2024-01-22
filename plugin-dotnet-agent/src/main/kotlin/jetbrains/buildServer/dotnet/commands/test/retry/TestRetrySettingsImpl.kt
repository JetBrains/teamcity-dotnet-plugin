package jetbrains.buildServer.dotnet.commands.test.retry

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetConstants
import java.nio.file.Paths

class TestRetrySettingsImpl(
    private val _parametersService: ParametersService,
    private val _pathsService: PathsService
) : TestRetrySettings {

    override val isEnabled: Boolean
        get() = maxRetries > 0

    override val maxRetries: Int
        get() = _parametersService
            .tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_TEST_RETRY_MAX_RETRIES)
            .let { runCatching { it?.trim()?.toInt() ?: 0 } }
            .getOrDefault(0)

    override val reportPath: String
        get() = _pathsService.getPath(PathType.AgentTemp).canonicalPath
            .let { Paths.get(it, "TestRetry").toAbsolutePath().toString() }

    override val maxFailures: Int
        get() = _parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_TEST_RETRY_MAX_FAILURES)
            .let { runCatching { it?.trim()?.toInt() ?: DefaultFailedTestsThreshold } }
            .getOrDefault(DefaultFailedTestsThreshold)

    companion object {
        private const val DefaultFailedTestsThreshold = 1_000
    }
}